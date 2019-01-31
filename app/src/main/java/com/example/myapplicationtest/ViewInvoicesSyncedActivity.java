package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ViewInvoicesSyncedActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    ListView listViewSalesPartners;
    ArrayAdapter<String> arrayAdapter;
    String salesPartner, invoiceNumbers = "";
    ArrayList<Integer> invoiceNumbersList;
    ArrayList<String> salesPartners;
    TextView textViewInvoicesTotal, textViewInvoicesTotalSum, textViewInvoicesTotalTypeOne,
            textViewInvoicesTotalSumTypeOne, textViewInvoicesTotalTypeTwo, textViewInvoicesTotalSumTypeTwo;
    SharedPreferences.Editor e;
    SharedPreferences sPrefSalesPartnerSyncedTmp;
    final String SAVED_SalesPartnerSyncedTmp = "salesPartnerSyncedTmp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invoices_synced);

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();

        invoiceNumbersList = new ArrayList<>();
        salesPartners = new ArrayList<>();

        EditText search = findViewById(R.id.editTextSearch);
        listViewSalesPartners = findViewById(R.id.listViewSalesPartners);
        textViewInvoicesTotal = findViewById(R.id.textViewInvoicesTotal);
        textViewInvoicesTotalSum = findViewById(R.id.textViewInvoicesTotalSum);
        textViewInvoicesTotalTypeOne = findViewById(R.id.textViewInvoicesTotalTypeOne);
        textViewInvoicesTotalSumTypeOne = findViewById(R.id.textViewInvoicesTotalSumTypeOne);
        textViewInvoicesTotalTypeTwo = findViewById(R.id.textViewInvoicesTotalTypeTwo);
        textViewInvoicesTotalSumTypeTwo = findViewById(R.id.textViewInvoicesTotalSumTypeTwo);
        textViewInvoicesTotalSum.setText("0");
        textViewInvoicesTotalSumTypeOne.setText("0");
        textViewInvoicesTotalSumTypeTwo.setText("0");

        sPrefSalesPartnerSyncedTmp = getSharedPreferences(SAVED_SalesPartnerSyncedTmp, Context.MODE_PRIVATE);
        salesPartner = sPrefSalesPartnerSyncedTmp.getString(SAVED_SalesPartnerSyncedTmp, "");


        listViewSalesPartners.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                salesPartner = ((TextView) view).getText().toString();
                showMore();
                Toast.makeText(getApplicationContext(), "Контрагент: " + salesPartner, Toast.LENGTH_SHORT).show();

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

        setListValues();
    }

    private void setListValues(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy/MM/dd HH:mm:ss" );
        final String output = now.with(LocalTime.MIN).format( formatter );
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();

        if (resultExists(db, "syncedInvoice","invoiceNumber")){
            String sql = "SELECT DISTINCT invoiceLocalDB.invoiceNumber FROM invoiceLocalDB " +
                    "WHERE EXISTS (SELECT syncedInvoice.invoiceNumber FROM syncedInvoice " +
                    "WHERE invoiceLocalDB.invoiceNumber LIKE  syncedInvoice.invoiceNumber) " +
                    "AND invoiceLocalDB.dateTimeDocLocal > ? ";
            Cursor c = db.rawQuery(sql, new String[]{output});
            if (c.moveToFirst()) {
                int iNumber = c.getColumnIndex("invoiceNumber");
                do {
                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber) ;
                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
                } while (c.moveToNext());
            } else {
                Toast.makeText(getApplicationContext(), "<<< Не найдено соответствий >>>", Toast.LENGTH_SHORT).show();
            }
            c.close();
            Toast.makeText(getApplicationContext(), invoiceNumbers, Toast.LENGTH_SHORT).show();
        }
//        else {
//            String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ";
//            Cursor c = db.rawQuery(sql, null);
//            if (c.moveToFirst()) {
//                int iNumber = c.getColumnIndex("invoiceNumber");
//                do {
//                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber);
//                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
//                } while (c.moveToNext());
//            }
//            c.close();
//            for (int i = 0; i < invoiceNumbersList.size(); i++){
//                Toast.makeText(getApplicationContext(), "Ничего не синхронизировано: " + invoiceNumbers, Toast.LENGTH_SHORT).show();
//            }
//        }
        if (invoiceNumbersList.size() > 0){
            for (int i = 0; i < invoiceNumbersList.size(); i++){
                String sql = "SELECT DISTINCT salesPartnerName, accountingTypeDoc, invoiceSum" +
                        " FROM invoiceLocalDB WHERE invoiceNumber LIKE ?";
                Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
                if (c.moveToFirst()) {
                    int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                    int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                    int invoiceSumTmp = c.getColumnIndex("invoiceSum");
                    String salesPartnerName = c.getString(salesPartnerNameTmp);
                    String accountingTypeDoc = c.getString(accountingTypeDocTmp);
                    Double invoiceSum = Double.parseDouble(c.getString(invoiceSumTmp));
                    salesPartners.add(salesPartnerName);
                    Double tmpInvTotalSum = (Double.parseDouble(textViewInvoicesTotalSum.getText().toString())
                            + invoiceSum);
                    textViewInvoicesTotalSum.setText(tmpInvTotalSum.toString());
                    if (accountingTypeDoc.equals("провод")){
                        Integer tmpInvTO = 0;
                        textViewInvoicesTotalTypeOne.setText(String.valueOf(tmpInvTO + 1));
                        Double tmpInvTotalSumTO = (Double.parseDouble(textViewInvoicesTotalSumTypeOne.getText().toString())
                                + invoiceSum);
                        textViewInvoicesTotalSumTypeOne.setText(tmpInvTotalSumTO.toString());
                    } else {
                        Integer tmpInvTT = 0;
                        textViewInvoicesTotalTypeTwo.setText(String.valueOf(tmpInvTT + 1));
                        Double tmpInvTotalSumTT = (Double.parseDouble(textViewInvoicesTotalSumTypeTwo.getText().toString())
                                + invoiceSum);
                        textViewInvoicesTotalSumTypeTwo.setText(tmpInvTotalSumTT.toString());
                    }
                }
                c.close();
            }
            textViewInvoicesTotal.setText(String.valueOf(invoiceNumbersList.size()));
            arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, salesPartners);
            listViewSalesPartners.setAdapter(arrayAdapter);
        }
    }

    private void showMore(){
        e = sPrefSalesPartnerSyncedTmp.edit();
        e.putString(SAVED_SalesPartnerSyncedTmp, salesPartner);
        e.apply();

        Intent intent = new Intent(this, ViewInvoicesSyncedShowMoreActivity.class);
        startActivity(intent);
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
