package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.EditText;

import static android.icu.text.MessagePattern.ArgType.SELECT;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoice, btnPayments, btnSalesAgents;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDayOfTheWeekDefault, sPrefDBName,
            sPrefDBPassword, sPrefDBUser, sPrefDayOfTheWeek, sPrefVisited;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DAYOFTHEWEEKDEFAULT = "DayOfTheWeekDefault";
    final String SAVED_DayOfTheWeek = "DayOfTheWeek";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_VISITED = "visited";
    String loginUrl = "https://caiman.ru.com/php/login.php", dbName, dbUser, dbPassword,
            syncUrl = "https://caiman.ru.com/php/syncDB.php";
    String[] dayOfTheWeek, salesPartnersName, accountingType, author;
    Integer[] area, serverDB_ID;
    SharedPreferences.Editor e;
    DBHelper dbHelper;
    final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        dbHelper = new DBHelper(this);

        btnInvoice = findViewById(R.id.buttonInvoice);
        btnPayments = findViewById(R.id.buttonPayments);
        btnSalesAgents = findViewById(R.id.buttonSalesPartners);
        btnInvoice.setOnClickListener(this);
        btnPayments.setOnClickListener(this);
        btnSalesAgents.setOnClickListener(this);

//        Intent intent = getIntent();
//        String agentName = intent.getStringExtra(Login.EXTRA_AGENTNAME);
//        TextView textView = findViewById(R.id.textViewAgent);
//        textView.setText(agentName);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeekDefault = getSharedPreferences(SAVED_DAYOFTHEWEEKDEFAULT, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DayOfTheWeek, Context.MODE_PRIVATE);
        sPrefVisited = getSharedPreferences(SAVED_VISITED, Context.MODE_PRIVATE);

        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");

        sPrefArea.edit().clear().apply();
        sPrefAccountingType.edit().clear().apply();
        sPrefDayOfTheWeek.edit().clear().apply();
        sPrefVisited.edit().clear().apply();

        loadData();
        loadDB();
//        syncDB();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoice:
                Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
                createInvoice();
                break;
            case R.id.buttonPayments:
                makePayments();
                break;
            case R.id.buttonSalesPartners:
                manageSalesPartners();
                break;
            default:
                break;
        }
    }

    private void createInvoice(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAreaActivity.class);
        startActivity(intent);
    }

    private void makePayments(){
        Intent intent = new Intent(getApplicationContext(), MakePaymentsActivity.class);
        startActivity(intent);
    }

    private void manageSalesPartners(){
        Intent intent = new Intent(getApplicationContext(), ManageSalesPartnersActivity.class);
        startActivity(intent);
    }

    private void loadData(){
        StringRequest request = new StringRequest(Request.Method.POST,
                loginUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    dayOfTheWeek = new String[jsonArray.length()];
                    if (jsonArray.length() == 1){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("dayOfTheWeek");
                        }
                        e = sPrefDayOfTheWeekDefault.edit();
                        e.putString(SAVED_DAYOFTHEWEEKDEFAULT, dayOfTheWeek[0]);
                        e.apply();
                        Toast.makeText(getApplicationContext(), "День недели: " + dayOfTheWeek[0], Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка Входа. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    dayOfTheWeek = new String[jsonArray.length()];
                    salesPartnersName= new String[jsonArray.length()];
                    area= new Integer[jsonArray.length()];
                    accountingType= new String[jsonArray.length()];
                    author= new String[jsonArray.length()];
                    serverDB_ID = new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("DayOfTheWeek");
                            salesPartnersName[i] = obj.getString("Наименование");
                            area[i] = obj.getInt("Район");
                            accountingType[i] = obj.getString("Учет");
                            author[i] = obj.getString("Автор");
                            serverDB_ID[i] = obj.getInt("ID");

                            SQLiteDatabase db = dbHelper.getWritableDatabase();
//                            if (!tableExists(db, "salesPartner"))
                            if (resultExists(db, "salesPartners")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in salesPartners: ---");
                                // подготовим данные для вставки в виде пар: наименование столбца - значение
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("Наименование", salesPartnersName[i]);
                                cv.put("Район", area[i]);
                                cv.put("Учет", accountingType[i]);
                                cv.put("DayOfTheWeek", dayOfTheWeek[i]);
                                cv.put("Автор", author[i]);
                                // вставляем запись и получаем ее ID
                                long rowID = db.insert("salesPartners", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Данные загружены", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

//    private void syncDB(){
//        ContentValues cv = new ContentValues();
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        Log.d(LOG_TAG, "--- Insert in salespartners: ---");
//        // подготовим данные для вставки в виде пар: наименование столбца - значение
//        for (int i = 0; i < serverDB_ID.length; i++){
//            cv.put("serverDB_ID", serverDB_ID[i]);
//            cv.put("Наименование", salesPartnersName[i]);
//            cv.put("Район", area[i]);
//            cv.put("Учет", accountingType[i]);
//            cv.put("DayOfTheWeek", dayOfTheWeek[i]);
//            cv.put("Автор", author[i]);
//            // вставляем запись и получаем ее ID
//            long rowID = db.insert("salespartners", null, cv);
//            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//        }
//    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myLocalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table salesPartners ("
                    + "id integer primary key autoincrement,"
                    + "serverDB_ID integer,"
                    + "Наименование text,"
                    + "Район integer,"
                    + "Учет text,"
                    + "DayOfTheWeek text,"
                    + "Автор text,"
                    + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");

            db.execSQL("create table items ("
                    + "id integer primary key autoincrement,"
                    + "Артикул integer,"
                    + "Наименование text,"
                    + "Цена integer,"
                    + "UNIQUE (Артикул) ON CONFLICT REPLACE" + ");");

            db.execSQL("create table itemsWithDiscount ("
                    + "id integer primary key autoincrement,"
                    + "serverDB_ID integer,"
                    + "Артикул integer,"
                    + "ID_скидки integer,"
                    + "ID_контрагента integer,"
                    + "автор text,"
                    + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");

            db.execSQL("create table discount ("
                    + "id integer primary key autoincrement,"
                    + "serverDB_ID integer,"
                    + "Тип_скидки integer,"
                    + "Скидка integer,"
                    + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");

            db.execSQL("create table invoice ("
                    + "id integer primary key autoincrement,"
                    + "serverDB_ID integer,"
                    + "InvoiceNumber integer,"
                    + "AgentID integer,"
                    + "SalesPartnerID integer,"
                    + "AccountingType text,"
                    + "ItemID integer,"
                    + "Quantity real,"
                    + "Price real,"
                    + "Total real,"
                    + "ExchangeQuantity real,"
                    + "ReturnQuantity  real,"
                    + "DateTimeDoc text,"
                    + "InvoiceSum real,"
                    + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");

            db.execSQL("create table pyments ("
                    + "id integer primary key autoincrement,"
                    + "serverDB_ID integer,"
                    + "InvoiceNumber integer,"
                    + "сумма_внесения real,"
                    + "автор text,"
                    + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");
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

    boolean resultExists(SQLiteDatabase db, String tableName){
        String sql = "SELECT EXISTS(SELECT ReturnQuantity FROM " + tableName + " WHERE ReturnQuantity = 0 LIMIT 1)";
        Cursor cursor = db.rawQuery(sql, null);
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }
}
