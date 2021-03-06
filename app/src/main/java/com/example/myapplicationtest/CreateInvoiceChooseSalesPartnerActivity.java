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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateInvoiceChooseSalesPartnerActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnNext;
    ListView listViewSalesPartners, listViewAccountingType;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDBName, sPrefDBPassword, sPrefDBUser,
            sPrefDayOfTheWeek, sPrefSalesPartner, sPrefAreaDefault, sPrefDayOfTheWeekDefault,
            sPrefConnectionStatus, sPrefAccountingTypeDefault, sPrefItemsListSaveStatus, sPrefChangeInvoiceNumberNotSynced,
            sPrefChangeInvoiceNotSynced;
    ArrayAdapter<String> arrayAdapter;
    final String SAVED_AREA = "Area";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_DAYOFTHEWEEK = "DayOfTheWeek";
    final String SAVED_DAYOFTHEWEEKDEFAULT = "DayOfTheWeekDefault";
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_CONNSTATUS = "connectionStatus";
    final String SAVED_ACCOUNTINGTYPEDEFAULT = "AccountingTypeDefault";
    final String SAVED_ItemsListSaveStatus = "itemsListSaveStatus";
    final String SAVED_ChangeInvoiceNotSynced = "changeInvoiceNotSynced";
    final String SAVED_ChangeInvoiceNumberNotSynced = "changeInvoiceNumberNotSynced";
    SharedPreferences.Editor e;
    String requestUrl = "https://caiman.ru.com/php/filter_new.php", salesPartner, dbName, dbUser, dbPassword,
            area, accountingType, dayOfTheWeek, connStatus, spAccType;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_choose_sales_partner);

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();

        EditText search = findViewById(R.id.editTextSearch);

        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);

        listViewSalesPartners = findViewById(R.id.listViewSalesPartners);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefArea= getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DAYOFTHEWEEK, Context.MODE_PRIVATE);
        sPrefDayOfTheWeekDefault = getSharedPreferences(SAVED_DAYOFTHEWEEKDEFAULT, Context.MODE_PRIVATE);
        sPrefAreaDefault= getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
        sPrefAccountingTypeDefault = getSharedPreferences(SAVED_ACCOUNTINGTYPEDEFAULT, Context.MODE_PRIVATE);
        sPrefItemsListSaveStatus = getSharedPreferences(SAVED_ItemsListSaveStatus, Context.MODE_PRIVATE);
        sPrefChangeInvoiceNotSynced = getSharedPreferences(SAVED_ChangeInvoiceNotSynced, Context.MODE_PRIVATE);
        sPrefChangeInvoiceNumberNotSynced = getSharedPreferences(SAVED_ChangeInvoiceNumberNotSynced, Context.MODE_PRIVATE);

        sPrefChangeInvoiceNotSynced.edit().clear().apply();
        sPrefChangeInvoiceNumberNotSynced.edit().clear().apply();

        initialValues();
        onLoadActivity();

        if (sPrefConnectionStatus.contains(SAVED_CONNSTATUS)) {
            connStatus = sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "");
            if (connStatus.equals("success")) {
                receiveDataFromLocalDB();
            } else {
                receiveDataFromLocalDB();
            }
        }

        listViewSalesPartners.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                salesPartner = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "????????????????????: " + salesPartner, Toast.LENGTH_SHORT).show();
                createInvoice();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
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

        e = sPrefItemsListSaveStatus.edit();
        e.putString(SAVED_ItemsListSaveStatus, "notSaved");
        e.apply();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:
                createInvoice();
                break;
            default:
                break;
        }
    }

    private void receiveDataFromServer(){
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrl, new Response.Listener<String>() {
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
                        Toast.makeText(getApplicationContext(), "???????????? ?????????????????? ???? ??????????????????", Toast.LENGTH_SHORT).show();
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
                parameters.put("Area", area);
                if (sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)){
                    parameters.put("AccountingType", accountingType);
                }
                parameters.put("DayOfTheWeek", dayOfTheWeek);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void receiveDataFromLocalDB(){
        db = dbHelper.getReadableDatabase();
        String sql;
        ArrayList<String> salesPartners;
        salesPartners = new ArrayList<>();
        if (sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)){
            sql = "SELECT ???????????????????????? FROM salesPartners WHERE DayOfTheWeek LIKE ? AND ?????????? LIKE ? AND ???????? LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{dayOfTheWeek, area, accountingType});
            if (c.moveToFirst()) {
                int idColIndex = c.getColumnIndex("????????????????????????");
                do {
                    Log.d(LOG_TAG,"ID = " + c.getString(idColIndex));
                    salesPartners.add(c.getString(idColIndex));
                } while (c.moveToNext());
            } else {
                Log.d(LOG_TAG, "0 rows");
                Toast.makeText(getApplicationContext(), "????????????: CreateInvoiceChooseSalesPartner receiveDataFromLocalDB 001",
                        Toast.LENGTH_SHORT).show();
            }
            arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, salesPartners);
            listViewSalesPartners.setAdapter(arrayAdapter);
            c.close();
        } else {
            sql = "SELECT ???????????????????????? FROM salesPartners WHERE DayOfTheWeek LIKE ? AND ?????????? LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{dayOfTheWeek, area});
            if (c.moveToFirst()) {
                int idColIndex = c.getColumnIndex("????????????????????????");
                do {
                    Log.d(LOG_TAG,"ID = " + c.getString(idColIndex));
                    salesPartners.add(c.getString(idColIndex));
                } while (c.moveToNext());
            } else {
                Log.d(LOG_TAG, "0 rows");
                Toast.makeText(getApplicationContext(), "????????????: CreateInvoiceChooseSalesPartner receiveDataFromLocalDB 002",
                        Toast.LENGTH_SHORT).show();
            }
            arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, salesPartners);
            listViewSalesPartners.setAdapter(arrayAdapter);
            c.close();
        }
    }

    private void createInvoice(){
        e = sPrefSalesPartner.edit();
        e.putString(SAVED_SALESPARTNER, salesPartner);
        e.apply();
        if (!sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)){
            String accountingTypeDefault;
            String sql = "SELECT ???????? FROM salesPartners WHERE ???????????????????????? LIKE ? AND DayOfTheWeek LIKE ? AND ?????????? LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{salesPartner, dayOfTheWeek, area});
            if (c.moveToFirst()) {
                int idColIndex = c.getColumnIndex("????????");
                do {
                    Log.d(LOG_TAG,"ID = " + c.getString(idColIndex));
                    accountingTypeDefault = c.getString(idColIndex);
                } while (c.moveToNext());
                e = sPrefAccountingTypeDefault.edit();
                e.putString(SAVED_ACCOUNTINGTYPEDEFAULT, accountingTypeDefault);
                e.apply();
            } else {
                Log.d(LOG_TAG, "0 rows");
                Toast.makeText(getApplicationContext(), "????????????: 003",
                        Toast.LENGTH_SHORT).show();
            }
            c.close();
        }
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceChooseTypeOfInvoiceActivity.class);
        startActivity(intent);
    }

    private void initialValues(){
        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)){
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");

            if (sPrefArea.contains(SAVED_AREA)){
                area = sPrefArea.getString(SAVED_AREA, "");
            } else {
                area = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
                e = sPrefArea.edit();
                e.putString(SAVED_AREA, area);
                e.apply();
            }
            if (sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)){
                accountingType = sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, "");
                e = sPrefAccountingTypeDefault.edit();
                e.putString(SAVED_ACCOUNTINGTYPEDEFAULT, accountingType);
                e.apply();
            } else {

            }
            if (sPrefDayOfTheWeek.contains(SAVED_DAYOFTHEWEEK)){
                dayOfTheWeek = sPrefDayOfTheWeek.getString(SAVED_DAYOFTHEWEEK, "");
            } else {
                dayOfTheWeek = sPrefDayOfTheWeekDefault.getString(SAVED_DAYOFTHEWEEKDEFAULT, "");
                if (dayOfTheWeek.equals("1") || dayOfTheWeek.equals("4")){
                    dayOfTheWeek = "??????????????????????-??????????????";
                }
                if (dayOfTheWeek.equals("2") || dayOfTheWeek.equals("5")){
                    dayOfTheWeek = "??????????????-??????????????";
                }
                if (dayOfTheWeek.equals("3")){
                    dayOfTheWeek = "??????????";
                }
                if (dayOfTheWeek.equals("6") || dayOfTheWeek.equals("7")){
                    dayOfTheWeek = "??????????";
                }
                if (sPrefAreaDefault.getString(SAVED_AREADEFAULT, "").equals("4")){
                    dayOfTheWeek = "??????????";
                }
                if (sPrefAreaDefault.getString(SAVED_AREADEFAULT, "").equals("6")){
                    dayOfTheWeek = "??????????";
                }
            }
        }
    }

    private void setAccountingTypeSP(){
        String sql = "SELECT * FROM salesPartners WHERE ???????????????????????? LIKE ? AND ?????????? LIKE ? AND DayOfTheWeek LIKE ?";
        Cursor c = db.rawQuery(sql, new String[]{salesPartner, area, dayOfTheWeek});
        if (c.moveToFirst()) {
            int spAccTypeTmp = c.getColumnIndex("????????");
            do {
                spAccType = c.getString(spAccTypeTmp);
                Log.d(LOG_TAG, "spAccType: " + spAccType);
            } while (c.moveToNext());
            Toast.makeText(getApplicationContext(), "????????????????????????: " + spAccType,
                    Toast.LENGTH_SHORT).show();
        }
        c.close();
        e = sPrefAccountingTypeDefault.edit();
        e.putString(SAVED_ACCOUNTINGTYPEDEFAULT, spAccType);
        e.apply();
    }

    private void onLoadActivity(){
        String spTmp = "";
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (resultExists(db, "itemsToInvoiceTmp", "salesPartner")){
            Log.d(LOG_TAG, "--- Rows in itemsToInvoiceTmp: ---");
            Cursor c = db.query("itemsToInvoiceTmp", null, null, null, null, null, null);
            if (c.moveToFirst()) {
                int salesPartnerTmp = c.getColumnIndex("????????????????????");
                do {
                    salesPartner = c.getString(salesPartnerTmp);
                    spTmp = salesPartner;
                } while (c.moveToNext());
            }
            if (spTmp != ""){
                builder.setTitle("????????????????")
                        .setMessage("?? ?????? ???????? ?????????????????????????? ???????????????? ???? ??????????????????????: " + salesPartner)
                        .setCancelable(true)
                        .setNegativeButton("????????????????????????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        setAccountingTypeSP();
                                        createInvoice();
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("?????????????? ?? ?????????????? ??????????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
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

    @Override
    public void onResume(){
        super.onResume();
        String itemsListSaveStatus = sPrefItemsListSaveStatus.getString(SAVED_ItemsListSaveStatus, "");
        if (itemsListSaveStatus.equals("saved")){
            finish();
        } else {

        }
    }
}
