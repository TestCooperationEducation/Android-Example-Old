package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateInvoiceChooseItemsActivity extends AppCompatActivity implements View.OnClickListener {

    String requestUrl = "https://caiman.ru.com/php/items.php", dbName, dbUser, dbPassword, item, connStatus;
    String[] itemsList;
    ListView listViewItems;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefItemsList, sPrefConnectionStatus;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_ItemsListToInvoice = "itemsToInvoice";
    final String SAVED_CONNSTATUS = "connectionStatus";
    SharedPreferences.Editor e;
    ArrayList<String> tmp;
    Button btnCreateList;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_choose_items);

        dbHelper = new DBHelper(this);

        listViewItems = findViewById(R.id.listViewItemsToSelect);

//        myList = new ArrayList<>();
        btnCreateList = findViewById(R.id.buttonNext);
        btnCreateList.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefItemsList = getSharedPreferences(SAVED_ItemsListToInvoice, Context.MODE_PRIVATE);
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)) {
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        }

        if (sPrefConnectionStatus.contains(SAVED_CONNSTATUS)) {
            connStatus = sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "");
            if (connStatus.equals("success")) {
                receiveItemsListFromServerDB();
            } else {
                receiveItemsListFromLocalDB();
            }
        }

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                item = ((TextView) view).getText().toString();
                addItemsToInvoice();
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:
                Intent intent = new Intent(this, CreateInvoiceSetItemsQuantitiesActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void receiveItemsListFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    itemsList = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemsList[i] = obj.getString("Наименование");
                            Toast.makeText(getApplicationContext(), "Список загружен с сервера", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Что-то пошло не так с запросом к серверу", Toast.LENGTH_SHORT).show();
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, itemsList);
                    listViewItems.setAdapter(arrayAdapter);
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы соединения с сервером", Toast.LENGTH_SHORT).show();
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

    private void receiveItemsListFromLocalDB(){
        db = dbHelper.getReadableDatabase();
        String sql;
        ArrayList<String> itemsList;
        itemsList = new ArrayList<>();
        sql = "SELECT Наименование FROM items";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("Наименование");
            do {
                Log.d(LOG_TAG,"ID = " + c.getString(idColIndex));
                itemsList.add(c.getString(idColIndex));
            } while (c.moveToNext());
        } else {
            Log.d(LOG_TAG, "0 rows");
            Toast.makeText(getApplicationContext(), "Ошибка: CreateInvoiceChooseSalesPartner receiveDataFromLocalDB 001",
                    Toast.LENGTH_SHORT).show();
        }
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, itemsList);
        listViewItems.setAdapter(arrayAdapter);
        c.close();
    }

    private void addItemsToInvoice() {
        tmp = new ArrayList<>();
        SparseBooleanArray chosen = listViewItems.getCheckedItemPositions();
        for (int i = 0; i < listViewItems.getCount(); i++) {
            if (chosen.get(i) == true) {
                tmp.add(listViewItems.getItemAtPosition(i).toString());
            }
        }
        setStringArrayPref(getApplicationContext(), SAVED_ItemsListToInvoice, tmp);
    }
    public static void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
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
