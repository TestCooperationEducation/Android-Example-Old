package com.example.myapplicationtest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CreateInvoiceViewTmpItemsListActivity extends AppCompatActivity {

    List<DataItemsListTmp> listTmp = new ArrayList<>();
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_view_tmp_items_list);

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();

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
