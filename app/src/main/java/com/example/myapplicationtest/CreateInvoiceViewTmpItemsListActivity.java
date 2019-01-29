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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class CreateInvoiceViewTmpItemsListActivity extends AppCompatActivity implements View.OnClickListener {

    List<DataItemsListTmp> listTmp = new ArrayList<>();
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefSalesPartner, sPrefInvoiceNumberTmp,
            sPrefAccountingType, sPrefAgent, sPrefAreaDefault, sPrefArea, sPrefAccountingTypeDefault, sPrefItemsListSaveStatus;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    SharedPreferences.Editor e;
    String requestUrlFinalPrice = "https://caiman.ru.com/php/price.php", dbName, dbUser, dbPassword,
            requestUrlSaveRecord = "https://caiman.ru.com/php/saveNewInvoice_new.php",
            requestUrlMakePayment = "https://caiman.ru.com/php/makePayment.php",
            salesPartner, accountingType, agent, areaDefault, area, accountingTypeDefault, statusSave;
    TextView textViewSalesPartner, textViewInvoiceTotal, textViewAgent, textViewAccountingType;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_AGENT = "agent";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPEDEFAULT = "AccountingTypeDefault";
    final String SAVED_INVOICENUMBERTMP = "invoiceNumberTmp";
    final String SAVED_ItemsListSaveStatus = "itemsListSaveStatus";
    Double invoiceSum = 0d;
    Button btnSaveInvoiceToLocalDB;
    Integer invoiceNumber, tmpCount;
    ArrayList<String> arrItems;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum, arrPriceChanged;
    List<DataInvoice> dataArray;
    List<DataPay> dataPay;
    ArrayList<Integer> invoiceNumbersList;
    String[] dateTimeDocServerFromRequest, invoiceNumberFromRequest;
    public static final String EXTRA_INVOICESUM = "com.example.myapplicationtest.INVOICESUM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_view_tmp_items_list);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        arrItems = new ArrayList<>();
        arrSum = new ArrayList<>();
        arrQuantity = new ArrayList<>();
        arrExchange = new ArrayList<>();
        arrReturn = new ArrayList<>();
        arrPriceChanged = new ArrayList<>();
        dataArray = new ArrayList<>();
        dataPay = new ArrayList<>();

        btnSaveInvoiceToLocalDB = findViewById(R.id.buttonSaveInvoiceToLocalDB);
        btnSaveInvoiceToLocalDB.setOnClickListener(this);

        textViewSalesPartner = findViewById(R.id.textViewSalesPartner);
        textViewInvoiceTotal = findViewById(R.id.textViewTotalSum);
        textViewAgent = findViewById(R.id.textViewAgent);
        textViewAccountingType = findViewById(R.id.textViewAccountingType);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefAgent = getSharedPreferences(SAVED_AGENT, Context.MODE_PRIVATE);
        sPrefAreaDefault = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefArea= getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingTypeDefault = getSharedPreferences(SAVED_ACCOUNTINGTYPEDEFAULT, Context.MODE_PRIVATE);
        sPrefInvoiceNumberTmp = getSharedPreferences(SAVED_INVOICENUMBERTMP, Context.MODE_PRIVATE);
        sPrefItemsListSaveStatus = getSharedPreferences(SAVED_ItemsListSaveStatus, Context.MODE_PRIVATE);

        sPrefInvoiceNumberTmp.edit().clear().apply();

        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        agent = sPrefAgent.getString(SAVED_AGENT, "");
        salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");
        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        accountingType = sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, "");
        area = sPrefArea.getString(SAVED_AREA, "");
        accountingTypeDefault = sPrefAccountingTypeDefault.getString(SAVED_ACCOUNTINGTYPEDEFAULT, "");

        textViewSalesPartner.setText(salesPartner);
        textViewAccountingType.setText(accountingType);
        textViewAgent.setText(agent);

        setInitialData();

//        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSaveInvoiceToLocalDB:
                saveInvoiceToLocalDB();
                break;
            default:
                break;
        }
    }

    private void setInitialData() {
//        Integer count;
//        String sql = "SELECT COUNT(*) FROM itemsToInvoiceTmp ";
//        Cursor cursor = db.rawQuery(sql, null);
//        if (!cursor.moveToFirst()) {
//            cursor.close();
//            count = 0;
//        } else {
//            count = cursor.getInt(0);
//        }
//        cursor.close();
//        tmpCount = count;

        Cursor c = db.query("itemsToInvoiceTmp", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int exchange = c.getColumnIndex("Обмен");
            int itemName = c.getColumnIndex("Наименование");
            int price = c.getColumnIndex("ЦенаИзмененная");
            int quantity = c.getColumnIndex("Количество");
            int total = c.getColumnIndex("Итого");
            int returnQuantity = c.getColumnIndex("Возврат");
            do {
                listTmp.add(new DataItemsListTmp(Double.parseDouble(c.getString(exchange)),
                        c.getString(itemName), Integer.parseInt(c.getString(price)),
                        Double.parseDouble(c.getString(quantity)), Double.parseDouble(c.getString(total)),
                        Double.parseDouble(c.getString(returnQuantity))));

                invoiceSum = invoiceSum + Double.parseDouble(c.getString(total));
                arrExchange.add(Double.parseDouble(c.getString(exchange)));
                arrItems.add(c.getString(itemName));
                arrPriceChanged.add(Double.parseDouble(c.getString(price)));
                arrQuantity.add(Double.parseDouble(c.getString(quantity)));
                arrSum.add(Double.parseDouble(c.getString(total)));
                arrReturn.add(Double.parseDouble(c.getString(returnQuantity)));

            } while (c.moveToNext());
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewItemsListTmp);
        DataAdapterViewTmpItemsListToInvoice adapter = new DataAdapterViewTmpItemsListToInvoice(this, listTmp);
        recyclerView.setAdapter(adapter);
        c.close();
        textViewInvoiceTotal.setText(invoiceSum.toString());

        if (tableExists(db, "invoiceLocalDB")){
            if (resultExists(db, "invoiceLocalDB", "invoiceNumber")){
                String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ORDER BY id DESC LIMIT 1";
                c = db.rawQuery(sql, null);
                if (c.moveToFirst()) {
                    int iNumber = c.getColumnIndex("invoiceNumber");
                    do {
                        invoiceNumber = Integer.parseInt(c.getString(iNumber)) + 1;
                    } while (c.moveToNext());
                }
            } else {
                invoiceNumber = 1;

            }
        } else {
            Toast.makeText(getApplicationContext(), "<<< Нет локальной таблицы накладных >>>", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveInvoiceToLocalDB(){
        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, "--- Insert in invoiceLocalDB: ---");

        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy/MM/dd HH:mm:ss" );
        String output = zdt.format( formatter );

        if (!valueExists(db, "invoiceLocalDB", "invoiceNumber", "invoiceNumber", invoiceNumber.toString())){
            for (int i = 0; i < arrItems.size(); i++){
                cv.put("invoiceNumber", invoiceNumber);
                cv.put("agentID", areaDefault);
                cv.put("areaSP", area);
                cv.put("salesPartnerName", salesPartner);
                cv.put("accountingTypeDoc", accountingType);
                cv.put("accountingTypeSP", accountingTypeDefault);
                cv.put("itemName", arrItems.get(i));
                cv.put("quantity", arrQuantity.get(i));
                cv.put("price", arrPriceChanged.get(i));
                cv.put("totalCost", arrSum.get(i));
                cv.put("exchangeQuantity", arrExchange.get(i));
                cv.put("returnQuantity", arrReturn.get(i));
                cv.put("dateTimeDocLocal", output);
                cv.put("invoiceSum", invoiceSum);
                long rowID = db.insert("invoiceLocalDB", null, cv);
                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                DataInvoice dt = new DataInvoice(salesPartner, accountingType, accountingTypeDefault,
                        arrItems.get(i), output, invoiceNumber, Integer.parseInt(areaDefault), Integer.parseInt(area), arrPriceChanged.get(i),
                        arrQuantity.get(i), arrSum.get(i), arrExchange.get(i), arrReturn.get(i), invoiceSum);
                dataArray.add(dt);
            }
            e = sPrefItemsListSaveStatus.edit();
            e.putString(SAVED_ItemsListSaveStatus, "saved");
            e.apply();

            sendToServer();
        } else {
            Toast.makeText(getApplicationContext(), "<<< Уже сохранено >>>", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToServer(){
        Gson gson = new Gson();
        final String newDataArray = gson.toJson(dataArray);
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlSaveRecord, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    invoiceNumberFromRequest = new String[jsonArray.length()];
                    dateTimeDocServerFromRequest = new String[jsonArray.length()];

                    ContentValues cv = new ContentValues();
                    Log.d(LOG_TAG, "--- Insert in syncedInvoice: ---");

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            invoiceNumberFromRequest[i] = obj.getString("invoiceNumber");
                            dateTimeDocServerFromRequest[i] = obj.getString("dateTimeDoc");
                        }
                        cv.put("invoiceNumber", Integer.parseInt(invoiceNumberFromRequest[0]));
                        cv.put("agentID", areaDefault);
                        long rowID = db.insert("syncedInvoice", null, cv);
                        Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        paymentPrompt();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
                Log.d("response", "result: " + response);
//                    invoiceNumberServerTmp.add(response);
//                Toast.makeText(getApplicationContext(), "Номер накладной: " + invoiceNumberServerTmp.get(0), Toast.LENGTH_SHORT).show();
//                dataArray.clear();
                if (String.valueOf(invoiceNumberFromRequest[invoiceNumberFromRequest.length - 1]).matches("-?\\d+")) {
                    Toast.makeText(getApplicationContext(), "Документ сохранён", Toast.LENGTH_SHORT).show();
                    statusSave = "Сохранено";

//                        textViewStatusSave.setText(statusSave);
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Сообщите об этой ошибке. Код 001", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("array", newDataArray);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void checkSave(){
        String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ORDER BY id ";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int invoiceNumberTmp = c.getColumnIndex("invoiceNumber");
            do {
                invoiceNumbersList.add(c.getInt(invoiceNumberTmp));
            } while (c.moveToNext());
        }
        c.close();

        if (invoiceNumbersList.size() == invoiceNumberFromRequest.length){
            Log.d(LOG_TAG, "Равны: " + String.valueOf(invoiceNumberFromRequest.length));
            Toast.makeText(getApplicationContext(), "Равны: " + String.valueOf(invoiceNumberFromRequest.length), Toast.LENGTH_SHORT).show();
        }
    }

    private void paymentPrompt(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Внимание")
                .setMessage("Внести Платёж?")
                .setCancelable(false)
                .setNegativeButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                checkPaymentSum();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Нет",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void checkPaymentSum(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Проверьте сумму")
                .setMessage("Внести всю сумму?")
                .setCancelable(false)
                .setNegativeButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                makePayment();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Нет",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                makePaymentPartial();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void makePayment(){
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy/MM/dd HH:mm:ss" );
//        String output = instant.toString();
        String output = zdt.format( formatter );

        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, "--- Insert in payments: ---");
        cv.put("DateTimeDoc", output);
        cv.put("InvoiceNumber", invoiceNumber);
        cv.put("сумма_внесения", invoiceSum);
        cv.put("Автор", agent);
        long rowID = db.insert("payments", null, cv);
        Log.d(LOG_TAG, "row inserted, ID = " + rowID);

        DataPay dt = new DataPay(invoiceSum);
        dataPay.add(dt);

        Gson gson = new Gson();
        final String newDataArray = gson.toJson(dataPay);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlMakePayment, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", "result: " + response);
                dataPay.clear();
                if (response.equals("Бабло внесено")) {
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                    builder.setTitle("Успешно")
                            .setMessage("Деньги внесены")
                            .setCancelable(false)
                            .setPositiveButton("Назад",
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
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Сообщите об этой ошибке. Код 002", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("invoiceNumber", invoiceNumber.toString());
                parameters.put("agent", agent);
                parameters.put("paymentAmount", newDataArray);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void makePaymentPartial(){
        e = sPrefInvoiceNumberTmp.edit();
        e.putString(SAVED_INVOICENUMBERTMP, invoiceNumber.toString());
        e.apply();

        Intent intent = new Intent(this, MakePaymentPartialActivity.class);
        intent.putExtra(EXTRA_INVOICESUM, invoiceSum.toString());
        startActivity(intent);
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
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

    boolean valueExists(SQLiteDatabase db, String tableName, String selectField, String fieldName, String value){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT COUNT(?) FROM " + tableName + " WHERE " + fieldName + " LIKE " + "?";
        Cursor cursor = db.rawQuery(sql, new String[]{selectField, value});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }
}
