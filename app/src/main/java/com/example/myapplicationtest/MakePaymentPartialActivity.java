package com.example.myapplicationtest;

import android.content.ContentValues;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakePaymentPartialActivity extends AppCompatActivity implements View.OnClickListener {
    EditText editTextPaymentSum;
    TextView textViewInvoiceSum;
    Button btnMakePaymentPartial;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_AGENT = "agent";
    final String SAVED_INVOICENUMBERTMP = "invoiceNumberTmp";
    final String SAVED_AREADEFAULT = "areaDefault";
    String requestUrlMakePayment = "https://caiman.ru.com/php/makePayment.php", dbName, dbUser,
            dbPassword, invoiceSum, agent, areaDefault, invoiceNumber;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefAgent, sPrefInvoiceNumberTmp,
            sPrefAreaDefault;
    List<DataPay> dataPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment_partial);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        dataPay = new ArrayList<>();

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefAgent = getSharedPreferences(SAVED_AGENT, Context.MODE_PRIVATE);
        sPrefInvoiceNumberTmp = getSharedPreferences(SAVED_INVOICENUMBERTMP, Context.MODE_PRIVATE);
        sPrefAreaDefault = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        agent = sPrefAgent.getString(SAVED_AGENT, "");
        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        invoiceNumber = sPrefInvoiceNumberTmp.getString(SAVED_INVOICENUMBERTMP, "");

        btnMakePaymentPartial = findViewById(R.id.buttonMakePaymentPartial);
        btnMakePaymentPartial.setOnClickListener(this);

        editTextPaymentSum = findViewById(R.id.editTextPaymentSum);
        textViewInvoiceSum = findViewById(R.id.textViewInvoiceSum);

        Intent intent = getIntent();
        invoiceSum = intent.getStringExtra(CreateInvoiceViewTmpItemsListActivity.EXTRA_INVOICESUM);
        textViewInvoiceSum.setText(invoiceSum);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonMakePaymentPartial:
                makePaymentPartialPrompt();
                break;
            default:
                break;
        }
    }

    private void makePaymentPartialPrompt(){
        if (textViewInvoiceSum.getText().toString().equals(editTextPaymentSum.getText().toString())){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("????????????????")
                    .setMessage("?????????? ???????????????? ?????????? ??????????????????")
                    .setCancelable(true)
                    .setPositiveButton("????????????",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    makePaymentPartial();
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            if (Double.parseDouble(textViewInvoiceSum.getText().toString()) > Double.parseDouble(editTextPaymentSum.getText().toString())){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("????????????????")
                        .setMessage("?????????? ???????????????? ???????????? ??????????????????")
                        .setCancelable(true)
                        .setPositiveButton("????????????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        makePaymentPartial();
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("????????????????")
                        .setMessage("?????????? ???????????????? ???????????? ??????????????????")
                        .setCancelable(true)
                        .setPositiveButton("????????????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        makePaymentPartial();
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void makePaymentPartial(){
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy/MM/dd HH:mm:ss" );
//        String output = instant.toString();
        final String output = zdt.format( formatter );

        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, "--- Insert in payments: ---");
        cv.put("DateTimeDoc", output);
        cv.put("InvoiceNumber", invoiceNumber);
        cv.put("??????????_????????????????", editTextPaymentSum.getText().toString());
        cv.put("??????????", agent);
        long rowID = db.insert("payments", null, cv);
        Log.d(LOG_TAG, "row inserted, ID = " + rowID);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("??????????????")
                .setMessage("???????????? ?????????????? ?? ????????????????????????????????")
                .setCancelable(false)
                .setPositiveButton("??????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

//        DataPay dt = new DataPay(Integer.parseInt(invoiceNumber), Double.parseDouble(editTextPaymentSum.getText().toString()));
//        dataPay.add(dt);
//
//        Gson gson = new Gson();
//        final String newDataArray = gson.toJson(dataPay);
//
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        StringRequest request = new StringRequest(Request.Method.POST,
//                requestUrlMakePayment, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.d("response", "result: " + response);
//                dataPay.clear();
//                try{
//                    JSONArray jsonArray = new JSONArray(response);
//                    String[] invoiceNumberFromRequest = new String[jsonArray.length()];
//                    String[] paymentIDFromRequest = new String[jsonArray.length()];
//                    String[] status = new String[jsonArray.length()];
//                    String tmpStatus = "";
//
//                    ContentValues cv = new ContentValues();
//                    Log.d(LOG_TAG, "--- Insert in syncedPayments: ---");
//
//                    if (jsonArray.length() > 0){
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject obj = jsonArray.getJSONObject(i);
//                            invoiceNumberFromRequest[i] = obj.getString("invoiceNumber");
//                            paymentIDFromRequest[i] = obj.getString("paymentID");
//                            status[i] = obj.getString("status");
//                            if (status[i].equals("?????????? ??????????????")) {
//                                tmpStatus = "Yes";
//                            }
//
//                            cv.put("invoiceNumber", Integer.parseInt(invoiceNumberFromRequest[i]));
//                            cv.put("paymentID", paymentIDFromRequest[i]);
//                            cv.put("agentID", areaDefault);
//                            cv.put("dateTimeDoc", output);
//                            long rowID = db.insert("syncedPayments", null, cv);
//                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                        }
//                        if (tmpStatus.equals("Yes")){
//                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
//                            builder.setTitle("??????????????")
//                                    .setMessage("???????????? ?????????????? ?? ????????????????????????????????")
//                                    .setCancelable(false)
//                                    .setPositiveButton("??????????",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    finish();
//                                                    dialog.cancel();
//                                                }
//                                            });
//                            AlertDialog alert = builder.create();
//                            alert.show();
//                        }
//                        Toast.makeText(getApplicationContext(), "<<< ???????????? ?????????????????????????????? >>>", Toast.LENGTH_SHORT).show();
//                    }else{
//                        Toast.makeText(getApplicationContext(), "???????????? ????????????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                catch (JSONException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener(){
//            @Override
//            public void onErrorResponse(VolleyError error){
//                onConnectionFailedPayment();
//                Toast.makeText(getApplicationContext(), "?????? ???????????? ???? ??????????????", Toast.LENGTH_SHORT).show();
//                Log.e("TAG", "Error " + error.getMessage());
//            }
//        }){
//            @Override
//            protected Map<String, String> getParams(){
//                Map<String, String> parameters = new HashMap<>();
//                parameters.put("dbName", dbName);
//                parameters.put("dbUser", dbUser);
//                parameters.put("dbPassword", dbPassword);
//                parameters.put("agent", agent);
//                parameters.put("agentID", areaDefault);
//                parameters.put("array", newDataArray);
//                return parameters;
//            }
//        };
//        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // ?????????????????????? ??????????????????????
            super(context, "myLocalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    boolean tableExists(SQLiteDatabase db, String tableName){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    boolean resultExists(SQLiteDatabase db, String tableName, String selectField){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT COUNT(?) FROM " + tableName;
        Cursor cursor = db.rawQuery(sql, new String[]{selectField});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    private void onConnectionFailedPayment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("???????????? ??????????????")
                .setMessage("?????????????????????????????? ??????????????, ?????????? ???????????????? ??????????")
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
