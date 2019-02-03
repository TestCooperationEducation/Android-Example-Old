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
import com.example.myapplicationtest.DataAdapterViewTmpItemsListToInvoice;
import com.example.myapplicationtest.DataInvoice;
import com.example.myapplicationtest.DataItemsListTmp;
import com.example.myapplicationtest.DataPay;
import com.example.myapplicationtest.MakePaymentPartialActivity;
import com.example.myapplicationtest.R;
import com.example.myapplicationtest.VolleySingleton;
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
            sPrefAccountingType, sPrefAgent, sPrefAreaDefault, sPrefArea, sPrefAccountingTypeDefault,
            sPrefItemsListSaveStatus, sPrefComment, sPrefInvoiceNumberLast;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    SharedPreferences.Editor e;
    String requestUrlFinalPrice = "https://caiman.ru.com/php/price.php", dbName, dbUser, dbPassword,
            requestUrlSaveRecord = "https://caiman.ru.com/php/saveNewInvoice_new.php",
            requestUrlMakePayment = "https://caiman.ru.com/php/makePayment.php", comment = "",
            salesPartner, accountingType, agent, areaDefault, area, accountingTypeDefault, statusSave,
            invoiceNumberLast;
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
    final String SAVED_Comment = "comment";
    final String SAVED_InvoiceNumberLast = "invoiceNumberLast";
    Double invoiceSum = 0d;
    Button btnSaveInvoiceToLocalDB;
    Integer invoiceNumber, tmpCount;
    ArrayList<String> arrItems;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum, arrPriceChanged;
    List<DataInvoice> dataArray;
    List<DataPay> dataPay;
    ArrayList<Integer> invoiceNumbersList;
    String[] dateTimeDocServerFromRequest, invoiceNumberFromRequest, paymentIDFromRequest;
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
        sPrefComment = getSharedPreferences(SAVED_Comment, Context.MODE_PRIVATE);
        sPrefInvoiceNumberLast = getSharedPreferences(SAVED_InvoiceNumberLast, Context.MODE_PRIVATE);

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
        invoiceNumberLast = sPrefInvoiceNumberLast.getString(SAVED_InvoiceNumberLast, "");

        textViewSalesPartner.setText(salesPartner);
        textViewAccountingType.setText(accountingType);
        textViewAgent.setText(agent);

        setInitialData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSaveInvoiceToLocalDB:
                savePrompt();
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

//        if (tableExists(db, "invoiceLocalDB")){
//            if (resultExists(db, "invoiceLocalDB", "invoiceNumber")){
//                String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ORDER BY id DESC LIMIT 1";
//                c = db.rawQuery(sql, null);
//                if (c.moveToFirst()) {
//                    int iNumber = c.getColumnIndex("invoiceNumber");
//                    do {
//                        invoiceNumber = Integer.parseInt(c.getString(iNumber)) + 1;
//                    } while (c.moveToNext());
//                }
//            } else {
//                invoiceNumber = 1;
//            }
//        } else {
//            Toast.makeText(getApplicationContext(), "<<< Нет локальной таблицы накладных >>>", Toast.LENGTH_SHORT).show();
//        }

        invoiceNumber = Integer.parseInt(invoiceNumberLast) + 1;
        Toast.makeText(getApplicationContext(), invoiceNumber.toString(), Toast.LENGTH_SHORT).show();
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
                cv.put("comment", comment);

                long rowID = db.insert("invoiceLocalDB", null, cv);
                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                DataInvoice dt = new DataInvoice(salesPartner, accountingType, accountingTypeDefault,
                        arrItems.get(i), output, comment, invoiceNumber, Integer.parseInt(areaDefault), Integer.parseInt(area), arrPriceChanged.get(i),
                        arrQuantity.get(i), arrSum.get(i), arrExchange.get(i), arrReturn.get(i), invoiceSum);
                dataArray.add(dt);
            }

            e = sPrefItemsListSaveStatus.edit();
            e.putString(SAVED_ItemsListSaveStatus, "saved");
            e.apply();

            if (tableExists(db, "itemsToInvoiceTmp")){
                clearTable("itemsToInvoiceTmp");
            }
            sPrefComment.edit().clear().apply();

//            sendToServer();

            e = sPrefInvoiceNumberLast.edit();
            e.putString(SAVED_InvoiceNumberLast, invoiceNumber.toString());
            e.apply();

            Toast.makeText(getApplicationContext(), invoiceNumber.toString(), Toast.LENGTH_SHORT).show();

            paymentPrompt();

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
                    String tmpStatus = "";

                    ContentValues cv = new ContentValues();
                    Log.d(LOG_TAG, "--- Insert in syncedInvoice: ---");

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            invoiceNumberFromRequest[i] = obj.getString("invoiceNumber");
                            dateTimeDocServerFromRequest[i] = obj.getString("dateTimeDoc");
                            tmpStatus = "Yes";
                        }
                        cv.put("invoiceNumber", Integer.parseInt(invoiceNumberFromRequest[0]));
                        cv.put("agentID", areaDefault);
                        cv.put("dateTimeDoc", dateTimeDocServerFromRequest[0]);
                        long rowID = db.insert("syncedInvoice", null, cv);
                        Log.d(LOG_TAG, "row inserted, ID = " + rowID);

                        if (tmpStatus.equals("Yes")){
                            paymentPrompt();
                            Toast.makeText(getApplicationContext(), "<<< Накладная Синхронизирована >>>", Toast.LENGTH_SHORT).show();
                        }
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
//                if (String.valueOf(invoiceNumberFromRequest[invoiceNumberFromRequest.length - 1]).matches("-?\\d+")) {
//                    Toast.makeText(getApplicationContext(), "Документ сохранён", Toast.LENGTH_SHORT).show();
//                    statusSave = "Сохранено";

//                        textViewStatusSave.setText(statusSave);
//                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                onConnectionFailedInvoice();
                Toast.makeText(getApplicationContext(), "Нет ответа от Сервера", Toast.LENGTH_SHORT).show();
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

    private void savePrompt(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Внимание")
                .setMessage("Хотите сохранить накладную?")
                .setCancelable(false)
                .setNegativeButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (salesPartner.equals("Для Себя Район 1") || salesPartner.equals("Для Себя Район 2")
                                        || salesPartner.equals("Для Себя Район 3") || salesPartner.equals("Для Себя Район 4")
                                        || salesPartner.equals("Для Себя Район 5")) {
                                    makeComment();
                                } else {
                                    saveInvoiceToLocalDB();
                                }
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Нет",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void makeComment(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Вы выбрали <Для Себя>")
                .setMessage("Хотите создать нового Контрагента?")
                .setCancelable(false)
                .setNegativeButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(getApplicationContext(), CreateInvoiceMakeCommentActivity.class);
                                startActivity(intent);
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Нет",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                saveInvoiceToLocalDB();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
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
                                sPrefAccountingType.edit().clear().apply();
                                finish();
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
                .setNegativeButton("Полностью",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                makePayment();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Частично",
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
        final String output = zdt.format( formatter );

        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, "--- Insert in payments: ---");
        cv.put("DateTimeDoc", output);
        cv.put("InvoiceNumber", invoiceNumber);
        cv.put("сумма_внесения", invoiceSum);
        cv.put("Автор", agent);
        long rowID = db.insert("payments", null, cv);
        Log.d(LOG_TAG, "row inserted, ID = " + rowID);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Успешно сохранено")
                .setMessage("Деньги внесены в локальную базу")
                .setCancelable(false)
                .setPositiveButton("Назад",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                sPrefAccountingType.edit().clear().apply();
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

//        DataPay dt = new DataPay(invoiceNumber, invoiceSum);
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
//                    invoiceNumberFromRequest = new String[jsonArray.length()];
//                    paymentIDFromRequest = new String[jsonArray.length()];
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
//                            if (status[i].equals("Бабло внесено")) {
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
//                            builder.setTitle("Успешно синхронизировано")
//                                    .setMessage("Деньги внесены и синхронизированы с сервером")
//                                    .setCancelable(false)
//                                    .setPositiveButton("Назад",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    sPrefAccountingType.edit().clear().apply();
//                                                    finish();
//                                                    dialog.cancel();
//                                                }
//                                            });
//                            AlertDialog alert = builder.create();
//                            alert.show();
//                        }
//                        Toast.makeText(getApplicationContext(), "<<< Платеж Синхронизирован >>>", Toast.LENGTH_SHORT).show();
//                    }else{
//                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(getApplicationContext(), "Сообщите об этой ошибке. Код 002", Toast.LENGTH_SHORT).show();
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

    private void makePaymentPartial(){
        e = sPrefInvoiceNumberTmp.edit();
        e.putString(SAVED_INVOICENUMBERTMP, invoiceNumber.toString());
        e.apply();

        Intent intent = new Intent(this, MakePaymentPartialActivity.class);
        intent.putExtra(EXTRA_INVOICESUM, invoiceSum.toString());
        startActivity(intent);
    }

    private void clearTable(String tableName){
        Log.d(LOG_TAG, "--- Clear: " + tableName + " ---");
        // удаляем все записи
        int clearCount = db.delete(tableName, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
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

    @Override
    public void onResume(){
        super.onResume();
        String itemsListSaveStatus = sPrefItemsListSaveStatus.getString(SAVED_ItemsListSaveStatus, "");
        comment = sPrefComment.getString(SAVED_Comment, "");
        if (itemsListSaveStatus.equals("saved")){
                sPrefAccountingType.edit().clear().apply();
                finish();
        } else {
            if (!comment.equals("")){
                saveInvoiceToLocalDB();
            }
        }
    }

    private void onConnectionFailedInvoice(){
//        e = sPrefConnectionStatus.edit();
//        e.putString(SAVED_CONNSTATUS, "failed");
//        e.apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Накладная сохранена")
                .setMessage("Синхронизируйте вручную, когда появится связь")
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                paymentPrompt();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void onConnectionFailedPayment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Деньги внесены")
                .setMessage("Синхронизируйте вручную, когда появится связь")
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                sPrefAccountingType.edit().clear().apply();
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
