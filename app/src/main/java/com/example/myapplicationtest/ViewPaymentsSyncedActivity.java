package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ViewPaymentsSyncedActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    ListView listViewPayments;
    ArrayAdapter<String> arrayAdapter;
    String paymentsNumbers = "", paymentsSum;
    ArrayList<String> salesPartners, paymentsInfoList;
    TextView textViewPaymentsQuantity, textViewPaymentsTotalSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_payments_synced);

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();

        paymentsInfoList = new ArrayList<>();
        salesPartners = new ArrayList<>();

        EditText search = findViewById(R.id.editTextSearch);
        listViewPayments = findViewById(R.id.listViewPayments);
        textViewPaymentsQuantity = findViewById(R.id.textViewPaymentsQuantity);
        textViewPaymentsTotalSum = findViewById(R.id.textViewPaymentsTotalSum);
        textViewPaymentsQuantity.setText("0");
        textViewPaymentsTotalSum.setText("0");


        listViewPayments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String choice = ((TextView) view).getText().toString();
                SparseBooleanArray chosen = listViewPayments.getCheckedItemPositions();
                for (int i = 0; i < paymentsInfoList.size(); i++) {
                    if (paymentsInfoList.get(i).equals(choice)) {
                        Toast.makeText(getApplicationContext(), "Контрагент: " + salesPartners.get(i), Toast.LENGTH_SHORT).show();
                        showMore();
                    }
                }
            }
        });

//        search.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                arrayAdapter.getFilter().filter(charSequence);
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

        setListValues();
    }

    private void setListValues(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
        final String output = now.with(LocalTime.MIN).format( formatter );
        Toast.makeText(getApplicationContext(), "Начало: " + output, Toast.LENGTH_SHORT).show();
        Double tmpSum = 0d;

        if (resultExists(db, "paymentsServer","InvoiceNumber")){
            String sql = "SELECT DISTINCT paymentsServer.serverDB_ID, paymentsServer.InvoiceNumber, " +
                    "paymentsServer.сумма_внесения, salesPartners.Наименование FROM paymentsServer " +
                    "INNER JOIN invoice ON paymentsServer.InvoiceNumber LIKE invoice.InvoiceNumber " +
                    "INNER JOIN salesPartners ON invoice.SalesPartnerID LIKE salesPartners.serverDB_ID " +
                    "WHERE paymentsServer.DateTimeDoc > ?";
            Cursor c = db.rawQuery(sql, new String[]{output});
            if (c.moveToFirst()) {
                int paymentIDTmp = c.getColumnIndex("serverDB_ID");
                int invoiceNumberTmp = c.getColumnIndex("InvoiceNumber");
                int paymentSumTmp = c.getColumnIndex("сумма_внесения");
                int salesPartnerTmp = c.getColumnIndex("Наименование");
                do {
                    paymentsNumbers = paymentsNumbers + "----" + c.getString(paymentIDTmp);
                    String tmp = c.getString(paymentIDTmp) + " " + c.getString(salesPartnerTmp) + " "
                            + c.getString(paymentSumTmp);
                    paymentsInfoList.add(tmp);
                    tmpSum = tmpSum + Double.parseDouble(c.getString(paymentSumTmp));
                    salesPartners.add(c.getString(salesPartnerTmp));
                } while (c.moveToNext());
            } else {
                Toast.makeText(getApplicationContext(), "Пусто", Toast.LENGTH_SHORT).show();
            }
            c.close();

            textViewPaymentsQuantity.setText(String.valueOf(paymentsInfoList.size()));
            textViewPaymentsTotalSum.setText(String.valueOf(roundUp(tmpSum, 2)));
            arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, paymentsInfoList);
            listViewPayments.setAdapter(arrayAdapter);
        }
//        if (invoiceNumbersList.size() > 0){
//            for (int i = 0; i < invoiceNumbersList.size(); i++){
//                String sql = "SELECT DISTINCT invoice.AccountingType, invoice.InvoiceSum, salesPartners.Наименование" +
//                        " FROM invoice INNER JOIN salesPartners ON invoice.SalesPartnerID LIKE salesPartners.serverDB_ID " +
//                        "WHERE invoice.InvoiceNumber LIKE ?";
//                Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
//                if (c.moveToFirst()) {
//                    int salesPartnerNameTmp = c.getColumnIndex("Наименование");
//                    int accountingTypeDocTmp = c.getColumnIndex("AccountingType");
//                    int invoiceSumTmp = c.getColumnIndex("InvoiceSum");
//                    String salesPartnerName = c.getString(salesPartnerNameTmp);
//                    String accountingTypeDoc = c.getString(accountingTypeDocTmp);
//                    Double invoiceSum = c.getDouble(invoiceSumTmp);
//                    salesPartners.add(salesPartnerName);
//                    tmpInvTotalSum = tmpInvTotalSum + invoiceSum;
//                    textViewInvoicesTotalSum.setText(String.valueOf(new DecimalFormat("##.##").format(tmpInvTotalSum)));
//                    if (accountingTypeDoc.equals("провод")){
//                        tmpInvTO = tmpInvTO + 1;
//                        textViewInvoicesTotalTypeOne.setText(String.valueOf(tmpInvTO));
//                        tmpInvTotalSumTO = tmpInvTotalSumTO + invoiceSum;
//                        textViewInvoicesTotalSumTypeOne.setText(String.valueOf(roundUp(tmpInvTotalSumTO, 2)));
//                    } else {
//                        tmpInvTT = tmpInvTT + 1;
//                        textViewInvoicesTotalTypeTwo.setText(String.valueOf(tmpInvTT));
//                        tmpInvTotalSumTT = tmpInvTotalSumTT + invoiceSum;
//                        textViewInvoicesTotalSumTypeTwo.setText(String.valueOf(roundUp(tmpInvTotalSumTT, 2)));
//                    }
//                }
//                c.close();
//            }
//        }
    }

    private void showMore(){
        Toast.makeText(getApplicationContext(), "Сработало", Toast.LENGTH_SHORT).show();
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

    public BigDecimal roundUp(double value, int digits){
        return new BigDecimal(""+value).setScale(digits, BigDecimal.ROUND_HALF_UP);
    }
}
