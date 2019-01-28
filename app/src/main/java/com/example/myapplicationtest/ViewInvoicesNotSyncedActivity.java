package com.example.myapplicationtest;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class ViewInvoicesNotSyncedActivity extends AppCompatActivity implements View.OnClickListener {

    List<DataInvoiceLocal> listTmp = new ArrayList<>();
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    Button btnSaveInvoiceToLocalDB;
    Integer invoiceNumberServer, invoiceNumberLocalTmp;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefLogin, sPrefAccountingTypeDefault,
            sPrefArea;
    String paymentStatus, invoiceNumbers = "", dbName, dbUser, dbPassword,
            requestUrlSaveRecord = "https://caiman.ru.com/php/saveNewInvoice_new.php",
            loginSecurity, statusSave = "";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_LOGIN = "Login";
    final String SAVED_ACCOUNTINGTYPEDEFAULT = "AccountingTypeDefault";
    final String SAVED_AREA = "Area";
    ArrayList<String> arrItems, invoiceNumberServerTmp, dateTimeDocServer;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum;
    ArrayList<Integer> arrPriceChanged, invoiceNumbersList;
    List<DataInvoice> dataArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invoices_not_synced);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        dataArray = new ArrayList<>();
        arrItems = new ArrayList<>();
        arrSum = new ArrayList<>();
        arrQuantity = new ArrayList<>();
        arrExchange = new ArrayList<>();
        arrReturn = new ArrayList<>();
        arrPriceChanged = new ArrayList<>();
        invoiceNumbersList = new ArrayList<>();
        invoiceNumberServerTmp = new ArrayList<>();
        dateTimeDocServer = new ArrayList<>();

        btnSaveInvoiceToLocalDB = findViewById(R.id.buttonSyncInvoicesWithServer);
        btnSaveInvoiceToLocalDB.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefAccountingTypeDefault = getSharedPreferences(SAVED_ACCOUNTINGTYPEDEFAULT, Context.MODE_PRIVATE);
        sPrefArea= getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        loginSecurity = sPrefLogin.getString(SAVED_LOGIN, "");

        setInitialData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSyncInvoicesWithServer:
                saveInvoicesToServerDB();
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
        if (statusSave.equals("Сохранено")){

        } else {
            invoiceNumberServerTmp.add(String.valueOf(0));
            dateTimeDocServer.add("");
        }
        if (resultExists(db, "syncedInvoice","invoiceNumber")){
            String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB INNER JOIN syncedInvoice " +
                    "ON invoiceLocalDB.invoiceNumber NOT LIKE syncedInvoice.invoiceNumber ";
            Cursor c = db.rawQuery(sql, null);
            if (c.moveToFirst()) {
                int iNumber = c.getColumnIndex("invoiceNumber");
                do {
                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber) ;
                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
                } while (c.moveToNext());
            }
            c.close();
            Toast.makeText(getApplicationContext(), "Несинхронизированные: " + invoiceNumbers, Toast.LENGTH_SHORT).show();
        } else {
            String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ";
            Cursor c = db.rawQuery(sql, null);
            if (c.moveToFirst()) {
                int iNumber = c.getColumnIndex("invoiceNumber");
                do {
                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber);
                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
                } while (c.moveToNext());
            }
            c.close();
            for (int i = 0; i < invoiceNumbersList.size(); i++){
                Toast.makeText(getApplicationContext(), "Ничего не синхронизировано: " + invoiceNumbersList.get(i), Toast.LENGTH_SHORT).show();
            }
//            Toast.makeText(getApplicationContext(), "Ничего не синхронизировано: " + invoiceNumbers, Toast.LENGTH_SHORT).show();
        }

        for (int i = 0; i < invoiceNumbersList.size(); i++){
            String sql = "SELECT salesPartnerName, accountingType, dateTimeDoc, invoiceSum" +
                    " FROM invoiceLocalDB WHERE invoiceNumber LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
            if (c.moveToFirst()) {
                int salesPartnerName = c.getColumnIndex("salesPartnerName");
                int accountingType = c.getColumnIndex("accountingType");
                int dateTimeDocLocal = c.getColumnIndex("dateTimeDoc");
                int invoiceSum = c.getColumnIndex("invoiceSum");
                String salesPartnerNameTmp = c.getString(salesPartnerName);
                String accountingTypeTmp = c.getString(accountingType);
                String dateTimeDocLocalTmp = c.getString(dateTimeDocLocal);
                Double invoiceSumGTmp = Double.parseDouble(c.getString(invoiceSum));
                paymentStatus = "";
                listTmp.add(new DataInvoiceLocal(salesPartnerNameTmp, accountingTypeTmp,
                        Integer.parseInt(invoiceNumberServerTmp.get(0)), dateTimeDocServer.get(0), dateTimeDocLocalTmp,
                        invoiceSumGTmp, paymentStatus));
                c.moveToNext();
            }
            c.close();
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerViewInvoicesLocal);
        DataAdapterViewInvoicesFromLocalDB adapter = new DataAdapterViewInvoicesFromLocalDB(this, listTmp);
        recyclerView.setAdapter(adapter);
    }

    private void saveInvoicesToServerDB(){
//        for (int i = 0; i < invoiceNumbersList.size(); i++){
//            dataArray.clear();
            String sql = "SELECT * FROM invoiceLocalDB ";
            Cursor c = db.rawQuery(sql, null);
            if (c.moveToFirst()) {
                int invoiceNumberLocal = c.getColumnIndex("invoiceNumber");
                int agentID = c.getColumnIndex("agentID");
                int areaSPTmp = c.getColumnIndex("area");
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                int accountingType = c.getColumnIndex("accountingType");
                int accountingTypeDefault = c.getColumnIndex("accountingTypeDefault");
                int itemName = c.getColumnIndex("itemName");
                int quantity = c.getColumnIndex("quantity");
                int price = c.getColumnIndex("price");
                int total = c.getColumnIndex("total");
                int exchangeQuantity = c.getColumnIndex("exchangeQuantity");
                int returnQuantity = c.getColumnIndex("returnQuantity");
                int dateTimeDocLocal = c.getColumnIndex("dateTimeDoc");
                int invoiceSum = c.getColumnIndex("invoiceSum");
                Integer invoiceNumberLocalTmp = Integer.parseInt(c.getString(invoiceNumberLocal));
                Integer areaSP = c.getInt(areaSPTmp);
                String salesPartnerName = c.getString(salesPartnerNameTmp);
                String accountingTypeTmp = c.getString(accountingType);
                String accountingTypeDefaultTmp = c.getString(accountingTypeDefault);
                Double invoiceSumTmp = Double.parseDouble(c.getString(invoiceSum));
                String dateTimeDocLocalTmp = c.getString(dateTimeDocLocal);

                do {
                    DataInvoice dt = new DataInvoice(salesPartnerNameTmp, accountingTypeTmp, c.getString(itemName),
                            Double.parseDouble(c.getString(price)), Double.parseDouble(c.getString(quantity)),
                            Double.parseDouble(c.getString(total)), Double.parseDouble(c.getString(exchangeQuantity)),
                            Double.parseDouble(c.getString(returnQuantity)), invoiceSumTmp);
                    dataArray.add(dt);

                } while (c.moveToNext());
            }
            c.close();
            Toast.makeText(getApplicationContext(), invoiceNumberLocalTmp.toString(), Toast.LENGTH_SHORT).show();

            sendToServer();
//        }
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
                    Integer[] invoiceNumber = new Integer[jsonArray.length()];
                    String[] dateTimeDoc = new String[jsonArray.length()];
                    if (jsonArray.length() == 1){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dateTimeDoc[i] = obj.getString("dateTimeDoc");
                            invoiceNumber[i] = obj.getInt("invoiceNumber");
                        }
                        invoiceNumberServerTmp.add(String.valueOf(invoiceNumber[0]));
                        dateTimeDocServer.add(dateTimeDoc[0]);
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }

                Log.d("response", "result: " + response);
//                    invoiceNumberServerTmp.add(response);
                Toast.makeText(getApplicationContext(), "Номер накладной: " + invoiceNumberServerTmp.get(0), Toast.LENGTH_SHORT).show();
                dataArray.clear();
                if (invoiceNumberServerTmp.get(0).matches("-?\\d+")) {
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
}
