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
import android.widget.TextView;
import android.widget.Toast;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CreateInvoiceViewTmpItemsListActivity extends AppCompatActivity implements View.OnClickListener {

    List<DataItemsListTmp> listTmp = new ArrayList<>();
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefSalesPartner,
            sPrefAccountingType, sPrefAgent, sPrefAreaDefault;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    String requestUrlFinalPrice = "https://caiman.ru.com/php/price.php", dbName, dbUser, dbPassword,
            salesPartner, accountingType, agent, areaDefault;
    TextView textViewSalesPartner, textViewInvoiceTotal, textViewAgent, textViewAccountingType;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_AGENT = "agent";
    final String SAVED_AREADEFAULT = "areaDefault";
    Double invoiceSum = 0d;
    Button btnSaveInvoiceToLocalDB;
    Integer invoiceNumber, tmpCount;
    ArrayList<String> arrItems;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum;
    ArrayList<Integer> arrPriceChanged;

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

        agent = sPrefAgent.getString(SAVED_AGENT, "");
        salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");
        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        accountingType = sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, "");

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
        Integer count;
        String sql = "SELECT COUNT(*) FROM itemsToInvoiceTmp ";
        Cursor cursor = db.rawQuery(sql, null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            count = 0;
        } else {
            count = cursor.getInt(0);
        }
        cursor.close();
        tmpCount = count;

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
