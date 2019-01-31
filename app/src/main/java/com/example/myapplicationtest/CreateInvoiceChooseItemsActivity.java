package com.example.myapplicationtest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
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

import org.json.JSONArray;

import java.util.ArrayList;

public class CreateInvoiceChooseItemsActivity extends AppCompatActivity implements View.OnClickListener {

    String requestUrl = "https://caiman.ru.com/php/items.php", dbName, dbUser, dbPassword, item, connStatus,
            salesPartner, itemsListSaveStatus;
    ArrayList<String> itemsList;
    ListView listViewItems;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefItemsList, sPrefConnectionStatus,
            sPrefItemName, sPrefSalesPartner, sPrefItemsListSaveStatus, sPrefNextInvoiceNumberTmp;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_ItemsListToInvoice = "itemsToInvoice";
    final String SAVED_CONNSTATUS = "connectionStatus";
    final String SAVED_ITEMNAME = "itemName";
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_ItemsListSaveStatus = "itemsListSaveStatus";
    final String SAVED_NextInvoiceNumberTmp = "nextInvoiceNumberTmp";
    SharedPreferences.Editor e;
    ArrayList<String> tmp;
    Button btnCreateList;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    ArrayAdapter<String> arrayAdapter;
    Integer iTmp;
    Boolean bTmp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_choose_items);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        itemsList = new ArrayList<>();

        listViewItems = findViewById(R.id.listViewItemsToSelect);

//        myList = new ArrayList<>();
        btnCreateList = findViewById(R.id.buttonNext);
        btnCreateList.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefItemsList = getSharedPreferences(SAVED_ItemsListToInvoice, Context.MODE_PRIVATE);
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
        sPrefItemName = getSharedPreferences(SAVED_ITEMNAME, Context.MODE_PRIVATE);
        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);
        sPrefItemsListSaveStatus = getSharedPreferences(SAVED_ItemsListSaveStatus, Context.MODE_PRIVATE);

        salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");
        itemsListSaveStatus = sPrefItemsListSaveStatus.getString(SAVED_ItemsListSaveStatus, "");

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)) {
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        }

        if (sPrefConnectionStatus.contains(SAVED_CONNSTATUS)) {
            connStatus = sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "");
            if (connStatus.equals("success")) {
                receiveItemsListFromLocalDB();
            } else {
                receiveItemsListFromLocalDB();
            }
        }

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                item = ((TextView) view).getText().toString();
                SparseBooleanArray chosen = listViewItems.getCheckedItemPositions();
                for (int i = 0; i < listViewItems.getCount(); i++) {
                    if (itemsList.get(i).equals(item) && chosen.get(i) == true) {
                        goToSetQuantities();
                    }
                    if (itemsList.get(i).equals(item) && chosen.get(i) == false) {
                        iTmp = i;
                        onSelectedItem();
                    }
                }
            }
        });

        e = sPrefItemsListSaveStatus.edit();
        e.putString(SAVED_ItemsListSaveStatus, "notSaved");
        e.apply();

//        setNextInvoiceNumber();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:
                Intent intent = new Intent(this, CreateInvoiceViewTmpItemsListActivity.class);
                startActivity(intent);
//                Integer count, tmp;
//                String sql = "SELECT COUNT(*) FROM itemsToInvoiceTmp ";
//                Cursor cursor = db.rawQuery(sql, null);
//                if (!cursor.moveToFirst()) {
//                    cursor.close();
//                    count = 0;
//                } else {
//                    count = cursor.getInt(0);
//                }
//                cursor.close();
//                tmp = count;
//                Toast.makeText(getApplicationContext(), tmp.toString(), Toast.LENGTH_SHORT).show();
//                Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + count);
                break;
            default:
                break;
        }
    }

//    private void receiveItemsListFromServerDB(){
//        StringRequest request = new StringRequest(Request.Method.POST,
//                requestUrl, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try{
//                    JSONArray jsonArray = new JSONArray(response);
//                    itemsList = new String[jsonArray.length()];
//                    if (jsonArray.length() > 0){
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject obj = jsonArray.getJSONObject(i);
//                            itemsList[i] = obj.getString("Наименование");
//                            if (resultExists(db, "itemsToInvoiceTmp", "Наименование", itemsList[i])){
//                                bTmp = true;
//                            }
//                        }
//                        if (bTmp == true){
//                            onLoadActivity();
//                        }
//                    }else{
//                        Toast.makeText(getApplicationContext(), "Что-то пошло не так с запросом к серверу", Toast.LENGTH_SHORT).show();
//                    }
//
//                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, itemsList);
//                    listViewItems.setAdapter(arrayAdapter);
//                    Toast.makeText(getApplicationContext(), "Загружено", Toast.LENGTH_SHORT).show();
//                }
//                catch (JSONException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener(){
//            @Override
//            public void onErrorResponse(VolleyError error){
//                Toast.makeText(getApplicationContext(), "Проблемы соединения с сервером", Toast.LENGTH_SHORT).show();
//                Log.e("TAG", "Error " + error.getMessage());
//            }
//        }){
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> parameters = new HashMap<>();
//                parameters.put("dbName", dbName);
//                parameters.put("dbUser", dbUser);
//                parameters.put("dbPassword", dbPassword);
//                return parameters;
//            }
//        };
//        VolleySingleton.getInstance(this).getRequestQueue().add(request);
//    }

    private void receiveItemsListFromLocalDB(){
        db = dbHelper.getReadableDatabase();
        String sql;
        sql = "SELECT Наименование FROM items";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("Наименование");
            do {
                Log.d(LOG_TAG,"ID = " + c.getString(idColIndex));
                itemsList.add(c.getString(idColIndex));
            } while (c.moveToNext());
            onLoadActivity();
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

    private void goToSetQuantities(){
        e = sPrefItemName.edit();
        e.putString(SAVED_ITEMNAME, item);
        e.apply();
        Intent intent = new Intent(this, CreateInvoiceSetItemsQuantitiesActivity.class);
        startActivity(intent);
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

    boolean valueExists(SQLiteDatabase db, String tableName, String fieldName, String fieldValue){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + fieldName + " LIKE ?";
        Cursor cursor = db.rawQuery(sql, new String[]{fieldValue});
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

    private void deleteRowFromLocalTable(String tableName, String fieldName, String fieldValue){
        db = dbHelper.getWritableDatabase();
        if (tableExists(db, tableName)) {
            if (valueExists(db, tableName, fieldName, fieldValue)) {
                Log.d(LOG_TAG, "--- Clear itemsToInvoiceTmp: ---");
                // удаляем все записи из таблицы
                int clearCount = db.delete(tableName, fieldName + " LIKE ?", new String[]{fieldValue});
                Log.d(LOG_TAG, "deleted rows count = " + clearCount);
                Toast.makeText(getApplicationContext(), "Удалено из списка", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onSelectedItem(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (tableExists(db, "itemsToInvoiceTmp")){
            if (valueExists(db, "itemsToInvoiceTmp", "Наименование", item)){
                builder.setTitle("Номенклатура: " + item)
                        .setMessage("Удалить или редактировать?")
                        .setCancelable(false)
                        .setNegativeButton("Удалить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        deleteRowFromLocalTable("itemsToInvoiceTmp", "Наименование", item);
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("Редактировать",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        goToSetQuantities();
                                        listViewItems.setItemChecked(iTmp, true);
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void onLoadActivity(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (tableExists(db, "itemsToInvoiceTmp")){
            if (valueExists(db, "itemsToInvoiceTmp", "Контрагент", salesPartner)) {
                builder.setTitle("Внимание")
                        .setMessage("У вас остался несохраненный список")
                        .setCancelable(false)
                        .setNegativeButton("Восстановить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (int i = 0; i < listViewItems.getCount(); i++) {
                                            if (valueExists(db, "itemsToInvoiceTmp", "Наименование", itemsList.get(i))) {
                                                listViewItems.setItemChecked(i, true);
                                            }
                                        }
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("Удалить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        clearTable("ItemsToInvoiceTmp");
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void clearTable(String tableName){
        Log.d(LOG_TAG, "--- Clear: " + tableName + " ---");
        // удаляем все записи
        int clearCount = db.delete(tableName, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
    }

    @Override
    public void onResume(){
        super.onResume();
        itemsListSaveStatus = sPrefItemsListSaveStatus.getString(SAVED_ItemsListSaveStatus, "");
        if (itemsListSaveStatus.equals("saved")){
            finish();
            Toast.makeText(getApplicationContext(), "<<< Ayyyyyyyyyyy >>>", Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < listViewItems.getCount(); i++) {
                if (!valueExists(db, "itemsToInvoiceTmp", "Наименование", itemsList.get(i))) {
                    listViewItems.setItemChecked(i, false);
                }
            }
        }
    }

    private void setNextInvoiceNumber(){
        Integer invoiceNumber;
        if (tableExists(db, "invoiceLocalDB")){
            if (resultExists(db, "invoiceLocalDB", "invoiceNumber")){
                String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ORDER BY id DESC LIMIT 1";
                Cursor c = db.rawQuery(sql, null);
                if (c.moveToFirst()) {
                    int iNumber = c.getColumnIndex("invoiceNumber");
                    do {
                        invoiceNumber = Integer.parseInt(c.getString(iNumber)) + 1;
                        e = sPrefNextInvoiceNumberTmp.edit();
                        e.putString(SAVED_NextInvoiceNumberTmp, invoiceNumber.toString());
                        e.apply();
                    } while (c.moveToNext());
                }
            } else {
                invoiceNumber = 1;
                e = sPrefNextInvoiceNumberTmp.edit();
                e.putString(SAVED_NextInvoiceNumberTmp, invoiceNumber.toString());
                e.apply();
            }
        } else {
            Toast.makeText(getApplicationContext(), "<<< Нет локальной таблицы накладных >>>", Toast.LENGTH_SHORT).show();
        }
    }
}
