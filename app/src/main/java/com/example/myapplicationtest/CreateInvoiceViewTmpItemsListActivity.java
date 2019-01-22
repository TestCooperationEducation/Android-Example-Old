package com.example.myapplicationtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CreateInvoiceViewTmpItemsListActivity extends AppCompatActivity {

    List<DataItemsListTmp> listTmp = new ArrayList<>();
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefSalesPartner,
            sPrefAccountingType;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    String requestUrlFinalPrice = "https://caiman.ru.com/php/price.php", dbName, dbUser, dbPassword,
            salesPartner, accountingType, agent;
    TextView textViewSalesPartner, textViewInvoiceTotal, textViewAgent, textViewAccountingType;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_view_tmp_items_list);

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();

        textViewSalesPartner = findViewById(R.id.textViewSalesPartner);
        textViewInvoiceTotal = findViewById(R.id.textViewTotalSum);
        textViewAgent = findViewById(R.id.textViewAgent);
        textViewAccountingType = findViewById(R.id.textViewAccountingType);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);

        salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");
        textViewAccountingType.setText(sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, ""));

        setInitialData();
    }

    private void setInitialData() {
//        String sql = "SELECT COUNT(*) FROM itemsToInvoiceTmp ";
//        Cursor cursor = db.rawQuery(sql, null);
//        int count;
//        if (!cursor.moveToFirst()) {
//            cursor.close();
//            count = 0;
//        } else {
//            count = cursor.getInt(0);
//        }
//        cursor.close();
        Cursor c = db.query("itemsToInvoiceTmp", null, null, null, null, null, null);
        Toast.makeText(getApplicationContext(), "111111111111111", Toast.LENGTH_SHORT).show();
        if (c.moveToFirst()) {Toast.makeText(getApplicationContext(), "2222222222222", Toast.LENGTH_SHORT).show();
            int exchange = c.getColumnIndex("Обмен");
            int itemName = c.getColumnIndex("Наименование");
            int price = c.getColumnIndex("ЦенаИзмененная");
            int quantity = c.getColumnIndex("Количество");
            int total = c.getColumnIndex("Итого");
            int returnQuantity = c.getColumnIndex("Возврат");
            do {Toast.makeText(getApplicationContext(), "3333", Toast.LENGTH_SHORT).show();
                listTmp.add(new DataItemsListTmp(Double.parseDouble(c.getString(exchange)),
                        c.getString(itemName), Integer.parseInt(c.getString(price)),
                        Double.parseDouble(c.getString(quantity)), Double.parseDouble(c.getString(total)),
                        Double.parseDouble(c.getString(returnQuantity))));
            } while (c.moveToNext());
        }
        Toast.makeText(getApplicationContext(), "4", Toast.LENGTH_SHORT).show();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewItemsListTmp);
        // создаем адаптер
        DataAdapter adapter = new DataAdapter(this, listTmp);
        // устанавливаем для списка адаптер
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
            Log.d(LOG_TAG, "--- onCreate database ---");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
