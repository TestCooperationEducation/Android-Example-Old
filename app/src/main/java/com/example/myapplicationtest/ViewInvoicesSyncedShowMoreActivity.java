package com.example.myapplicationtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ViewInvoicesSyncedShowMoreActivity extends AppCompatActivity {

    SharedPreferences sPrefSalesPartnerSyncedTmp, sPrefAgent;
    final String SAVED_SalesPartnerSyncedTmp = "salesPartnerSyncedTmp";
    final String SAVED_AGENT = "agent";
    String salesPartner;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    List<DataItemsListTmp> listTmp = new ArrayList<>();
    TextView textViewSalesPartner, textViewAgent, textViewTotalSum, textViewAccountingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invoices_synced_show_more);

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();

        sPrefSalesPartnerSyncedTmp = getSharedPreferences(SAVED_SalesPartnerSyncedTmp, Context.MODE_PRIVATE);
        sPrefAgent = getSharedPreferences(SAVED_AGENT, Context.MODE_PRIVATE);
        salesPartner = sPrefSalesPartnerSyncedTmp.getString(SAVED_SalesPartnerSyncedTmp, "");

        textViewSalesPartner = findViewById(R.id.textViewSalesPartner);
        textViewAgent = findViewById(R.id.textViewAgent);
        textViewAccountingType = findViewById(R.id.textViewAccountingType);
        textViewTotalSum = findViewById(R.id.textViewTotalSum);

        textViewAgent.setText(sPrefAgent.getString(SAVED_AGENT, ""));

        showMore();
    }

    private void showMore(){
        String sql = "SELECT * FROM invoiceLocalDB WHERE salesPartnerName LIKE ?";
        Cursor c = db.rawQuery(sql, new String[]{salesPartner});
        if (c.moveToFirst()) {
            int exchange = c.getColumnIndex("exchangeQuantity");
            int itemName = c.getColumnIndex("itemName");
            int price = c.getColumnIndex("price");
            int quantity = c.getColumnIndex("quantity");
            int total = c.getColumnIndex("totalCost");
            int returnQuantity = c.getColumnIndex("returnQuantity");
            int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
            int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
            int invoiceSumTmp = c.getColumnIndex("invoiceSum");
            textViewSalesPartner.setText(c.getString(salesPartnerNameTmp));
            textViewAccountingType.setText(c.getString(accountingTypeDocTmp));
            textViewTotalSum.setText(c.getString(invoiceSumTmp));
            do {
                listTmp.add(new DataItemsListTmp(Double.parseDouble(c.getString(exchange)),
                        c.getString(itemName), Integer.parseInt(c.getString(price)),
                        Double.parseDouble(c.getString(quantity)), Double.parseDouble(c.getString(total)),
                        Double.parseDouble(c.getString(returnQuantity))));

//                invoiceSum = invoiceSum + Double.parseDouble(c.getString(total));
//                arrExchange.add(Double.parseDouble(c.getString(exchange)));
//                arrItems.add(c.getString(itemName));
//                arrPriceChanged.add(Double.parseDouble(c.getString(price)));
//                arrQuantity.add(Double.parseDouble(c.getString(quantity)));
//                arrSum.add(Double.parseDouble(c.getString(total)));
//                arrReturn.add(Double.parseDouble(c.getString(returnQuantity)));

            } while (c.moveToNext());
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerViewItemsListTmp);
        DataAdapterViewTmpItemsListToInvoice adapter = new DataAdapterViewTmpItemsListToInvoice(this, listTmp);
        recyclerView.setAdapter(adapter);
        c.close();
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
