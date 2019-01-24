package com.example.myapplicationtest;

import android.content.ContentValues;
import android.content.Context;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ViewInvoicesNotSyncedActivity extends AppCompatActivity implements View.OnClickListener {

    List<DataInvoiceLocal> listTmp = new ArrayList<>();
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    Button btnSaveInvoiceToLocalDB;
    Integer invoiceNumberServer;
    String dateTimeDocServer, paymentStatus, dateTimeDocLocalGlobal, accountingTypeGlobal,
            salesPartnerNameGlobal;
    ArrayList<String> arrItems;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum;
    ArrayList<Integer> arrPriceChanged;
    Double invoiceSumGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invoices_not_synced);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        arrItems = new ArrayList<>();
        arrSum = new ArrayList<>();
        arrQuantity = new ArrayList<>();
        arrExchange = new ArrayList<>();
        arrReturn = new ArrayList<>();
        arrPriceChanged = new ArrayList<>();

        btnSaveInvoiceToLocalDB = findViewById(R.id.buttonSaveInvoiceToLocalDB);
        btnSaveInvoiceToLocalDB.setOnClickListener(this);
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

        String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB INNER JOIN syncedInvoice " +
                "ON invoiceLocalDB.invoiceNumber NOT LIKE syncedInvoice.invoiceNumber ";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int iNumber = c.getColumnIndex("invoiceNumber");
            do {
                invoiceNumber = iNumber + 1;
            } while (c.moveToNext());
        }

        Cursor c = db.query("invoiceLocalDB", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int salesPartnerName = c.getColumnIndex("salesPartnerName");
            int accountingType = c.getColumnIndex("accountingType");
            int dateTimeDocLocal = c.getColumnIndex("dateTimeDoc");
            int invoiceSum = c.getColumnIndex("invoiceSum");
            int agentID = c.getColumnIndex("agentID");
            int itemName = c.getColumnIndex("itemName");
            int quantity = c.getColumnIndex("quantity");
            int price = c.getColumnIndex("price");
            int total = c.getColumnIndex("total");
            int exchangeQuantity = c.getColumnIndex("exchangeQuantity");
            int returnQuantity = c.getColumnIndex("returnQuantity");
            invoiceSumGlobal = Double.parseDouble(c.getString(invoiceSum));
            do {
                listTmp.add(new DataInvoiceLocal(salesPartnerNameGlobal,
                        accountingTypeGlobal, invoiceNumberServer,
                        dateTimeDocServer, dateTimeDocLocalGlobal,
                        invoiceSumGlobal, paymentStatus));

                arrExchange.add(Double.parseDouble(c.getString(exchangeQuantity)));
                arrItems.add(c.getString(itemName));
                arrPriceChanged.add(Integer.parseInt(c.getString(price)));
                arrQuantity.add(Double.parseDouble(c.getString(quantity)));
                arrSum.add(Double.parseDouble(c.getString(total)));
                arrReturn.add(Double.parseDouble(c.getString(returnQuantity)));

            } while (c.moveToNext());
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewItemsListTmp);
        // создаем адаптер
        DataAdapterViewTmpItemsListToInvoice adapter = new DataAdapterViewTmpItemsListToInvoice(this, listTmp);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
        c.close();

        textViewInvoiceTotal.setText(invoiceSum.toString());

        if (tableExists(db, "invoiceLocalDB")){
            if (resultExists(db, "invoiceLocalDB", "invoiceNumber")){
                sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ORDER BY id DESC LIMIT 1";
                c = db.rawQuery(sql, null);
                if (c.moveToFirst()) {
                    int iNumber = c.getColumnIndex("invoiceNumber");
                    do {
                        invoiceNumber = iNumber + 1;
                    } while (c.moveToNext());
                }
            } else {
                invoiceNumber = 1;

            }
        } else {
            Toast.makeText(getApplicationContext(), "<<< Нет локальной таблицы накладных >>>", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveInvoicesToServerDB(){
        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, "--- Insert in invoiceLocalDB: ---");

        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy/MM/dd HH:mm:ss" );
//        String output = instant.toString();
        String output = zdt.format( formatter );

        Toast.makeText(getApplicationContext(), "<<< Идёт процесс добавления данных >>>", Toast.LENGTH_SHORT).show();
        for (int i = 0; i < arrItems.size(); i++){
            cv.put("invoiceNumber", invoiceNumber);
            cv.put("agentID", areaDefault);
            cv.put("salesPartnerName", salesPartner);
            cv.put("accountingType", accountingType);
            cv.put("itemName", arrItems.get(i));
            cv.put("quantity", arrQuantity.get(i));
            cv.put("price", arrPriceChanged.get(i));
            cv.put("total", arrSum.get(i));
            cv.put("exchangeQuantity", arrExchange.get(i));
            cv.put("returnQuantity", arrReturn.get(i));
            cv.put("dateTimeDoc", output);
            cv.put("invoiceSum", invoiceSum);
            long rowID = db.insert("invoiceLocalDB", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
        }
        Toast.makeText(getApplicationContext(), "<<< Завершено >>>", Toast.LENGTH_SHORT).show();
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
