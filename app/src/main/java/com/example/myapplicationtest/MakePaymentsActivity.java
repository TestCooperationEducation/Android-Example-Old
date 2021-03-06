package com.example.myapplicationtest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakePaymentsActivity extends AppCompatActivity implements View.OnClickListener{

    ListView listViewAccountingType, listViewDebts, listViewSalesPartners;
    String[] invoiceNumber, totalPayment;
    String accountingType, dbName, dbUser, dbPassword, debt, loginSecurity, dateStart, dateEnd,
            requestUrlLoadDebts = "https://caiman.ru.com/php/loadDebtors.php", salesPartner,
            requestUrlSalesPartners = "https://caiman.ru.com/php/receiveSalesPartners.php",
            requestUrlMakePayment = "https://caiman.ru.com/php/makePayment.php",
            author, connStatus;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefLogin, sPrefConnectionStatus,
            sPrefAccountingType;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_LOGIN = "Login";
    final String SAVED_CONNSTATUS = "connectionStatus";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    Button btnReceiveList, btnClearList, btnSelectInvoice, btnMakePayment;
    EditText editTextDateStart, editTextDateEnd, editTextSearch, editTextPaymentSum;
    TextView textViewInvoiceDebt, textViewTotalDebt, textViewStatusPay;
    ArrayAdapter<String> arrayAdapter;
    Double tmpTotalDebt = 0d, paymentAmount;
    List<DataPay> dataPay;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payments);

        dataPay = new ArrayList<>();
        dbHelper = new DBHelper(this);

        btnReceiveList = findViewById(R.id.buttonReceiveList);
        btnReceiveList.setOnClickListener(this);
        btnClearList = findViewById(R.id.buttonClearList);
        btnClearList.setOnClickListener(this);
        btnSelectInvoice = findViewById(R.id.buttonSelect);
        btnSelectInvoice.setOnClickListener(this);
        btnMakePayment = findViewById(R.id.buttonMakePayment);
        btnMakePayment.setOnClickListener(this);

        editTextDateStart = findViewById(R.id.editTextDateStart);
        editTextDateEnd = findViewById(R.id.editTextDateEnd);
        editTextSearch = findViewById(R.id.editTextSearch);
        editTextPaymentSum = findViewById(R.id.editTextPaymentSum);
        textViewInvoiceDebt = findViewById(R.id.textViewInvoiceDebt);
        textViewTotalDebt = findViewById(R.id.textViewTotalDebt);
        textViewStatusPay = findViewById(R.id.textViewStatusPay);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);

        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");

        listViewAccountingType = findViewById(R.id.listViewAccountingType);
        listViewDebts  = findViewById(R.id.listViewDebtors);
        listViewSalesPartners = findViewById(R.id.listViewSalesPartners);

        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        loginSecurity = sPrefLogin.getString(SAVED_LOGIN, "");

        loadListAccountingType();
        receiveSalesPartners();

        listViewAccountingType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                accountingType = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "?????? ??????????: " + accountingType, Toast.LENGTH_SHORT).show();
            }
        });

        listViewDebts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                debt = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "???????? ???? ?????????????????? ???: " + debt, Toast.LENGTH_SHORT).show();
//                String[] tmpDebt = new String[1];
//                tmpDebt[0] = debt;
//                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, tmpDebt);
//                listViewAccountingType.setAdapter(arrayAdapter);
            }
        });

        listViewSalesPartners.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                salesPartner = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "????????????????????: " + salesPartner, Toast.LENGTH_SHORT).show();
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                arrayAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonReceiveList:
                tmpTotalDebt = 0d;
                if (sPrefConnectionStatus.contains(SAVED_CONNSTATUS)) {
                    connStatus = sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "");
                    if (!connStatus.equals("failed")) {
                        receiveDebtsFromServerDB();
                    } else {
//                        receiveDebtsFromLocalDB();
                    }
                }
                break;
            case R.id.buttonClearList:
                clearAll();
                break;
            case R.id.buttonSelect:
                selectInvoice();
                break;
            case R.id.buttonMakePayment:
                makePaymentPrompt();
                break;
            case R.id.buttonViewInvoice:
                viewInvoice();
                break;
            default:
                break;
        }
    }

    private void loadListAccountingType(){
        String[] accountingType = new String[2];
        accountingType[0] = "????????????";
        accountingType[1] = "????????????????";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountingType);
        listViewAccountingType.setAdapter(arrayAdapter);
    }

    private void receiveDebtsFromServerDB(){
        dateStart = editTextDateStart.getText().toString();
        dateEnd = editTextDateEnd.getText().toString();

        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlLoadDebts, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", "result: " + response);
                try{
//                    JSONObject jasonObject = new JSONObject(response);
//
//                    invoiceNumber = new Integer[jArray.length()];
//
//                    for (int i = 0; i < jArray.length(); i++) {
//                        JSONArray jArray = jasonObject.getJSONArray();
//                        invoiceNumber[i] = jArray.getInt(i);
//                    }

                    JSONArray jsonArray = new JSONArray(response);
                    Toast.makeText(getApplicationContext(), "???????????? ???????????????? ????????????", Toast.LENGTH_SHORT).show();
                    totalPayment = new String[jsonArray.length()];
                    invoiceNumber = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            invoiceNumber[i] = obj.getString("invoiceNumber");
                            totalPayment[i] = obj.getString("paymentSum");
                            tmpTotalDebt = tmpTotalDebt + Double.parseDouble(totalPayment[i]);
                            textViewTotalDebt.setText(tmpTotalDebt.toString());
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, invoiceNumber);
                    listViewDebts.setAdapter(arrayAdapter);
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Response Error, fuck!", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("loginSecurity", loginSecurity);
                if (!TextUtils.isEmpty(dateStart)) {
                    parameters.put("dateStart", dateStart);
                }
                if (!TextUtils.isEmpty(dateEnd)) {
                    parameters.put("dateEnd", dateEnd);
                }
                if (!TextUtils.isEmpty(accountingType)) {
                    parameters.put("accountingType", accountingType);
                }
                if (!TextUtils.isEmpty(salesPartner)) {
                    parameters.put("salesPartner", salesPartner);
                }
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void receiveSalesPartners() {
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlSalesPartners, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Toast.makeText(getApplicationContext(), "???????????? ??????????????", Toast.LENGTH_SHORT).show();
                    String[] salesPartners = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            salesPartners[i] = obj.getString("????????????????????????");
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "??????-???? ?????????? ???? ??????", Toast.LENGTH_SHORT).show();
                    }

                    arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, salesPartners);
                    listViewSalesPartners.setAdapter(arrayAdapter);
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???? ???????????? ???????????????? ?????????? ???? ??????????????!", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "????????????: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
//                parameters.put("Area", area);
//                parameters.put("AccountingType", accountingType);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void clearAll(){
        invoiceNumber = new String[0];
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, invoiceNumber);
        listViewDebts.setAdapter(arrayAdapter);
        textViewTotalDebt.setText("???????? ????????");
        textViewInvoiceDebt.setText("???????? ???? ??????????????????");
        editTextPaymentSum.setText("");
        editTextDateStart.setText("");
        editTextDateEnd.setText("");
        editTextSearch.setText("");
    }

    private void selectInvoice(){
        if (!TextUtils.isEmpty(debt)){
            textViewInvoiceDebt.setText(totalPayment[Arrays.asList(invoiceNumber).indexOf(debt)]);
        }
    }

    private void makePaymentPrompt(){
        if (textViewInvoiceDebt.getText().toString() == "???????? ???? ??????????????????") {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("????????????????")
                    .setMessage("???? ???? ?????????????? ???????????????? ?????? ????????????")
                    .setCancelable(true)
                    .setNegativeButton("?? ?????? ??????????",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            if (isDouble(textViewInvoiceDebt.getText().toString())){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("???????????????? ??????????")
                        .setMessage("??????????????????, ?????? ???? ?????????? ????????????!")
                        .setCancelable(true)
                        .setNegativeButton("????, ?? ???????? ???????????? ????????????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        checkPaymentPrompt();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void checkPaymentPrompt(){
        if (editTextPaymentSum.getText().toString().trim().length() == 0
                || Double.parseDouble(editTextPaymentSum.getText().toString())
                == Double.parseDouble(textViewInvoiceDebt.getText().toString())) {
            paymentAmount = Double.parseDouble(textViewInvoiceDebt.getText().toString());
            Toast.makeText(getApplicationContext(), paymentAmount.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("?????????????????? ??????????")
                    .setMessage("???????????? ???????????? ?????? ???????????")
                    .setCancelable(true)
                    .setNegativeButton("????, ?? ???????? ???????????? ?????? ??????????",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    makePayment();
//                                dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            paymentAmount = Double.parseDouble(editTextPaymentSum.getText().toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("?????????????????? ??????????")
                    .setMessage("?????????? ???????????????? ???????????????????? ???? ?????????? ??????????!")
                    .setCancelable(true)
                    .setNegativeButton("????, ?????? ?????????? ????????????",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    makePayment();
//                                dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void makePayment(){
//        DataPay dt = new DataPay(paymentAmount);
//        dataPay.add(dt);
//
//        Gson gson = new Gson();
//        final String newDataArray = gson.toJson(dataPay);
//
//        StringRequest request = new StringRequest(Request.Method.POST,
//                requestUrlMakePayment, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.d("response", "result: " + response);
//                dataPay.clear();
//                if (response.equals("?????????? ??????????????")) {
//                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
//                    if (paymentAmount == Double.parseDouble(textViewInvoiceDebt.getText().toString())){
//                        textViewStatusPay.setText("???????????????? ??????????????????");
//                    } else {
//                        textViewStatusPay.setText("???????????????? ????????????????");
//                    }
//                    clearAll();
//                }
//            }
//        }, new Response.ErrorListener(){
//            @Override
//            public void onErrorResponse(VolleyError error){
//                Toast.makeText(getApplicationContext(), "???????????????? ???? ???????? ????????????. ?????? 003", Toast.LENGTH_SHORT).show();
//                Log.e("TAG", "Error " + error.getMessage());
//            }
//        }){
//            @Override
//            protected Map<String, String> getParams(){
//                Map<String, String> parameters = new HashMap<>();
//                parameters.put("dbName", dbName);
//                parameters.put("dbUser", dbUser);
//                parameters.put("dbPassword", dbPassword);
//                parameters.put("invoiceNumber", debt);
//                parameters.put("author", author);
//                parameters.put("paymentAmount", newDataArray);
//                return parameters;
//            }
//        };
//        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void viewInvoice(){
        Toast.makeText(getApplicationContext(), "???????? ???????????????????? ?????? ???? ???????????????? :-(", Toast.LENGTH_SHORT).show();
    }

    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

//    private void receiveDebtsFromLocalDB(){
//        db = dbHelper.getReadableDatabase();
//        String sql;
//        ArrayList<String> salesPartners;
//        salesPartners = new ArrayList<>();
//        if (sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)){
//            sql = "SELECT ???????????????????????? FROM salesPartners WHERE DayOfTheWeek LIKE ? AND ?????????? LIKE ? AND ???????? LIKE ?";
//            Cursor c = db.rawQuery(sql, new String[]{dayOfTheWeek, area, accountingType});
//            if (c.moveToFirst()) {
//                int idColIndex = c.getColumnIndex("????????????????????????");
//                do {
//                    Log.d(LOG_TAG,"ID = " + c.getString(idColIndex));
//                    salesPartners.add(c.getString(idColIndex));
//                } while (c.moveToNext());
//            } else {
//                Log.d(LOG_TAG, "0 rows");
//                Toast.makeText(getApplicationContext(), "????????????: CreateInvoiceChooseSalesPartner receiveDataFromLocalDB 001",
//                        Toast.LENGTH_SHORT).show();
//            }
//            arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, salesPartners);
//            listViewSalesPartners.setAdapter(arrayAdapter);
//            c.close();
//        } else {
//            sql = "SELECT ???????????????????????? FROM salesPartners WHERE DayOfTheWeek LIKE ? AND ?????????? LIKE ?";
//            Cursor c = db.rawQuery(sql, new String[]{dayOfTheWeek, area});
//            if (c.moveToFirst()) {
//                int idColIndex = c.getColumnIndex("????????????????????????");
//                do {
//                    Log.d(LOG_TAG,"ID = " + c.getString(idColIndex));
//                    salesPartners.add(c.getString(idColIndex));
//                } while (c.moveToNext());
//            } else {
//                Log.d(LOG_TAG, "0 rows");
//                Toast.makeText(getApplicationContext(), "????????????: CreateInvoiceChooseSalesPartner receiveDataFromLocalDB 002",
//                        Toast.LENGTH_SHORT).show();
//            }
//            arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, salesPartners);
//            listViewSalesPartners.setAdapter(arrayAdapter);
//            c.close();
//        }
//    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // ?????????????????????? ??????????????????????
            super(context, "myLocalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
