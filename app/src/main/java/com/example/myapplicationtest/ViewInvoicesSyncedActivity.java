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
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
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
    String salesPartner, invoiceNumbers = "", invoiceNumberTmp;
    ArrayList<Integer> invoiceNumbersList;
    ArrayList<String> salesPartners;
    TextView textViewInvoicesTotal, textViewInvoicesTotalSum, textViewInvoicesTotalTypeOne,
            textViewInvoicesTotalSumTypeOne, textViewInvoicesTotalTypeTwo, textViewInvoicesTotalSumTypeTwo;
    SharedPreferences.Editor e;
    SharedPreferences sPrefSalesPartnerSyncedTmp;
    final String SAVED_SalesPartnerSyncedTmp = "salesPartnerSyncedTmp";
    Integer tmpInvTO = 0, tmpInvTT = 0;

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
                SparseBooleanArray chosen = listViewSalesPartners.getCheckedItemPositions();
                for (int i = 0; i < listViewSalesPartners.getCount(); i++) {
                    if (salesPartners.get(i).equals(salesPartner) && chosen.get(i) == true) {
                        invoiceNumberTmp = String.valueOf(invoiceNumbersList.get(i));
                        Toast.makeText(getApplicationContext(), "Контрагент: " + salesPartner, Toast.LENGTH_SHORT).show();
                    }
//                    if (itemsList.get(i).equals(item) && chosen.get(i) == false) {
//                        iTmp = i;
//                        onSelectedItem();
//                    }
                }
                showMore();


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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
        final String output = now.with(LocalTime.MIN).format( formatter );
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();

        if (resultExists(db, "invoice","InvoiceNumber")){
//            String sql = "SELECT DISTINCT invoiceLocalDB.invoiceNumber FROM invoiceLocalDB " +
//                    "WHERE EXISTS (SELECT syncedInvoice.invoiceNumber FROM syncedInvoice " +
//                    "WHERE invoiceLocalDB.invoiceNumber LIKE  syncedInvoice.invoiceNumber) " +
//                    "AND invoiceLocalDB.dateTimeDocLocal > ? ";
            String sql = "SELECT DISTINCT InvoiceNumber FROM invoice WHERE DateTimeDoc > ?";
            Cursor c = db.rawQuery(sql, new String[]{output});
            if (c.moveToFirst()) {
                int iNumber = c.getColumnIndex("InvoiceNumber");
                do {
                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber) ;
                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
                } while (c.moveToNext());
            } else {
//                Toast.makeText(getApplicationContext(), "<<< Не найдено соответствий >>>", Toast.LENGTH_SHORT).show();
            }
            c.close();

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
                String sql = "SELECT DISTINCT invoice.AccountingType, invoice.InvoiceSum, salesPartners.Наименование" +
                        " FROM invoice INNER JOIN salesPartners ON invoice.SalesPartnerID LIKE salesPartners.serverDB_ID " +
                        "WHERE invoice.InvoiceNumber LIKE ?";
                Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
                if (c.moveToFirst()) {
                    int salesPartnerNameTmp = c.getColumnIndex("Наименование");
                    int accountingTypeDocTmp = c.getColumnIndex("AccountingType");
                    int invoiceSumTmp = c.getColumnIndex("InvoiceSum");
                    String salesPartnerName = c.getString(salesPartnerNameTmp);
                    String accountingTypeDoc = c.getString(accountingTypeDocTmp);
                    Double invoiceSum = Double.parseDouble(c.getString(invoiceSumTmp));
                    salesPartners.add(salesPartnerName);
                    Double tmpInvTotalSum = (Double.parseDouble(textViewInvoicesTotalSum.getText().toString())
                            + invoiceSum);
                    textViewInvoicesTotalSum.setText(String.valueOf(new DecimalFormat("##.##").format(tmpInvTotalSum)));
                    if (accountingTypeDoc.equals("провод")){
                        tmpInvTO = tmpInvTO + 1;
                        textViewInvoicesTotalTypeOne.setText(String.valueOf(tmpInvTO));
                        Double tmpInvTotalSumTO = (Double.parseDouble(textViewInvoicesTotalSumTypeOne.getText().toString())
                                + invoiceSum);
                        textViewInvoicesTotalSumTypeOne.setText(String.valueOf(new DecimalFormat("##.##").format(tmpInvTotalSumTO)));
                    } else {
                        tmpInvTT = tmpInvTT + 1;
                        textViewInvoicesTotalTypeTwo.setText(String.valueOf(tmpInvTT));
                        Double tmpInvTotalSumTT = (Double.parseDouble(textViewInvoicesTotalSumTypeTwo.getText().toString())
                                + invoiceSum);
                        textViewInvoicesTotalSumTypeTwo.setText(String.valueOf(new DecimalFormat("##.##").format(tmpInvTotalSumTT)));
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
        e.putString(SAVED_SalesPartnerSyncedTmp, invoiceNumberTmp);
        e.apply();

        Intent intent = new Intent(this, ViewInvoicesSyncedShowMoreActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        for (int i = 0; i < invoiceNumbersList.size(); i++) {
            listViewSalesPartners.setItemChecked(i, false);
        }
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
