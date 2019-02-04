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
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChangeInvoiceChooseItemsActivity extends AppCompatActivity implements View.OnClickListener {

    String item, salesPartner, itemsListSaveStatus;
    ArrayList<String> itemsList;
    ListView listViewItems;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefItemsList, sPrefConnectionStatus,
            sPrefItemName, sPrefSalesPartner, sPrefItemsListSaveStatus,
            sPrefChangeInvoiceNumberNotSynced, sPrefChangeInvoiceNotSynced;
    final String SAVED_ItemsListToInvoice = "itemsToInvoice";
    final String SAVED_CONNSTATUS = "connectionStatus";
    final String SAVED_ITEMNAME = "itemName";
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_ItemsListSaveStatus = "itemsListSaveStatus";
    final String SAVED_ChangeInvoiceNotSynced = "changeInvoiceNotSynced";
    final String SAVED_ChangeInvoiceNumberNotSynced = "changeInvoiceNumberNotSynced";
    SharedPreferences.Editor e;
    Button btnCreateList;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    ArrayAdapter<String> arrayAdapter;
    Integer iTmp;
    ArrayList<String> itemNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_invoice_choose_items);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        itemsList = new ArrayList<>();
        itemNameList = new ArrayList<>();

        listViewItems = findViewById(R.id.listViewItemsToSelect);
        btnCreateList = findViewById(R.id.buttonNext);
        btnCreateList.setOnClickListener(this);

        sPrefItemsList = getSharedPreferences(SAVED_ItemsListToInvoice, Context.MODE_PRIVATE);
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
        sPrefItemName = getSharedPreferences(SAVED_ITEMNAME, Context.MODE_PRIVATE);
        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);
        sPrefItemsListSaveStatus = getSharedPreferences(SAVED_ItemsListSaveStatus, Context.MODE_PRIVATE);
        sPrefChangeInvoiceNotSynced = getSharedPreferences(SAVED_ChangeInvoiceNotSynced, Context.MODE_PRIVATE);
        sPrefChangeInvoiceNumberNotSynced = getSharedPreferences(SAVED_ChangeInvoiceNumberNotSynced, Context.MODE_PRIVATE);

        salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");
        itemsListSaveStatus = sPrefItemsListSaveStatus.getString(SAVED_ItemsListSaveStatus, "");

        receiveItemsListFromLocalDB();

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

//        onLoadInvoiceNotSyncedChange();
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

    private void receiveItemsListFromLocalDB() {
        db = dbHelper.getReadableDatabase();
        String sql;
        sql = "SELECT Наименование FROM items";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("Наименование");
            do {
                Log.d(LOG_TAG, "ID = " + c.getString(idColIndex));
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

    private void goToSetQuantities() {
        e = sPrefItemName.edit();
        e.putString(SAVED_ITEMNAME, item);
        e.apply();
        Intent intent = new Intent(this, CreateInvoiceSetItemsQuantitiesActivity.class);
        startActivity(intent);
    }

    private void onLoadInvoiceNotSyncedChange() {
        if (sPrefChangeInvoiceNotSynced.contains(SAVED_ChangeInvoiceNotSynced) &&
                sPrefChangeInvoiceNumberNotSynced.contains(SAVED_ChangeInvoiceNumberNotSynced)) {
            String sPTmp = sPrefChangeInvoiceNotSynced.getString(SAVED_ChangeInvoiceNotSynced, "");
            String iNTmp = sPrefChangeInvoiceNumberNotSynced.getString(SAVED_ChangeInvoiceNumberNotSynced, "");

            String sql = "SELECT itemName FROM invoiceLocalDB WHERE salesPartnerName LIKE ? AND invoiceNumber LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{sPTmp, iNTmp});
            if (c.moveToFirst()) {
                Toast.makeText(getApplicationContext(), "<<< Готово >>>", Toast.LENGTH_SHORT).show();
                int iNameTmp = c.getColumnIndex("itemName");
                do {
//                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber);
                    itemNameList.add(c.getString(iNameTmp));
                } while (c.moveToNext());
            }
            c.close();
            for (int i = 0; i < listViewItems.getCount(); i++) {
                for (int b = 0; b < itemNameList.size(); b++) {
                    if (itemNameList.get(b).equals(itemsList.get(i))) {
                        listViewItems.setItemChecked(i, true);
                    }
                }
            }
//            sPrefChangeInvoiceNotSynced.edit().clear().apply();
//            sPrefChangeInvoiceNumberNotSynced.edit().clear().apply();
        }
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

    boolean tableExists(SQLiteDatabase db, String tableName) {
        if (tableName == null || db == null || !db.isOpen()) {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    boolean valueExists(SQLiteDatabase db, String tableName, String fieldName, String fieldValue) {
        if (tableName == null || db == null || !db.isOpen()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + fieldName + " LIKE ?";
        Cursor cursor = db.rawQuery(sql, new String[]{fieldValue});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    boolean resultExists(SQLiteDatabase db, String tableName, String selectField) {
        if (tableName == null || db == null || !db.isOpen()) {
            return false;
        }
        String sql = "SELECT COUNT(?) FROM " + tableName;
        Cursor cursor = db.rawQuery(sql, new String[]{selectField});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    private void deleteRowFromLocalTable(String tableName, String fieldName, String fieldValue) {
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

    private void onSelectedItem() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (tableExists(db, "itemsToInvoiceTmp")) {
            if (valueExists(db, "itemsToInvoiceTmp", "Наименование", item)) {
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

    private void onLoadActivity() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (tableExists(db, "itemsToInvoiceTmp")) {
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
                                            if (!valueExists(db, "itemsToInvoiceTmp", "Наименование", itemsList.get(i))) {
                                                listViewItems.setItemChecked(i, false);
                                            }
                                        }
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("Удалить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        clearTable("ItemsToInvoiceTmp");
                                        onLoadInvoiceNotSyncedChange();
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void clearTable(String tableName) {
        Log.d(LOG_TAG, "--- Clear: " + tableName + " ---");
        // удаляем все записи
        int clearCount = db.delete(tableName, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
    }

    @Override
    public void onResume() {
        super.onResume();
        itemsListSaveStatus = sPrefItemsListSaveStatus.getString(SAVED_ItemsListSaveStatus, "");
        if (itemsListSaveStatus.equals("saved")) {
            finish();
            Toast.makeText(getApplicationContext(), "<<< Готово >>>", Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < listViewItems.getCount(); i++) {
                if (!valueExists(db, "itemsToInvoiceTmp", "Наименование", itemsList.get(i))) {
                    listViewItems.setItemChecked(i, false);
                }
            }
            onLoadInvoiceNotSyncedChange();
        }
    }
}
