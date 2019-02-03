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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewInvoicesChangeNotSyncedActivity extends AppCompatActivity implements View.OnClickListener {

    List<DataInvoiceLocal> listTmp = new ArrayList<>();
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    Button btnChangeInvoiceNotSynced;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefLogin, sPrefAccountingTypeDefault,
            sPrefArea, sPrefAreaDefault, sPrefInvoiceNumberLast, sPrefChangeInvoiceNotSynced,
            sPrefChangeInvoiceNumberNotSynced;
    String paymentStatus, invoiceNumbers = "", dbName, dbUser, dbPassword,
            loginSecurity, areaDefault, invoiceNumberLast, salesPartnerName;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_LOGIN = "Login";
    final String SAVED_ACCOUNTINGTYPEDEFAULT = "AccountingTypeDefault";
    final String SAVED_AREA = "Area";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_InvoiceNumberLast = "invoiceNumberLast";
    final String SAVED_ChangeInvoiceNotSynced = "changeInvoiceNotSynced";
    final String SAVED_ChangeInvoiceNumberNotSynced = "changeInvoiceNumberNotSynced";
    ArrayList<String> arrItems, invoiceNumberServerTmp, dateTimeDocServer;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum;
    ArrayList<Integer> arrPriceChanged, invoiceNumbersList;
    List<DataInvoice> dataArray;
    String[] sPListTmp, invoiceNumberNotSyncedChange;
    SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invoices_change_not_synced);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        dataArray = new ArrayList<>();
        arrItems = new ArrayList<>();
        arrSum = new ArrayList<>();
        arrQuantity = new ArrayList<>();
        arrExchange = new ArrayList<>();
        arrReturn = new ArrayList<>();
        arrPriceChanged = new ArrayList<>();
        invoiceNumbersList = new ArrayList<>();
        invoiceNumberServerTmp = new ArrayList<>();
        dateTimeDocServer = new ArrayList<>();

        btnChangeInvoiceNotSynced = findViewById(R.id.buttonChangeInvoiceNotSynced);
        btnChangeInvoiceNotSynced.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefAccountingTypeDefault = getSharedPreferences(SAVED_ACCOUNTINGTYPEDEFAULT, Context.MODE_PRIVATE);
        sPrefArea= getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAreaDefault  = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefInvoiceNumberLast = getSharedPreferences(SAVED_InvoiceNumberLast, Context.MODE_PRIVATE);
        sPrefChangeInvoiceNotSynced = getSharedPreferences(SAVED_ChangeInvoiceNotSynced, Context.MODE_PRIVATE);
        sPrefChangeInvoiceNumberNotSynced = getSharedPreferences(SAVED_ChangeInvoiceNumberNotSynced, Context.MODE_PRIVATE);

        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        loginSecurity = sPrefLogin.getString(SAVED_LOGIN, "");
        invoiceNumberLast = sPrefInvoiceNumberLast.getString(SAVED_InvoiceNumberLast, "");

        setInitialData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonChangeInvoiceNotSynced:
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Выберите для изменения")
                        .setCancelable(false)
                        .setNeutralButton("Назад",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                })
                        // добавляем переключатели
                        .setSingleChoiceItems(sPListTmp, -1,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int item) {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "Вы выбрали: "
                                                        + sPListTmp[item],
                                                Toast.LENGTH_SHORT).show();

                                        e = sPrefChangeInvoiceNotSynced.edit();
                                        e.putString(SAVED_ChangeInvoiceNotSynced, sPListTmp[item]);
                                        e.apply();
                                        e = sPrefChangeInvoiceNumberNotSynced.edit();
                                        e.putString(SAVED_ChangeInvoiceNumberNotSynced, invoiceNumberNotSyncedChange[item]);
                                        e.apply();
                                        Intent intent = new Intent(getApplicationContext(), ChangeInvoiceChooseItemsActivity.class);
                                        startActivity(intent);
                                    }
                                });
                    AlertDialog alert = builder.create();
                    alert.show();
            default:
                break;
        }
    }

    private void setInitialData() {
        invoiceNumberServerTmp.add(String.valueOf(0));
        dateTimeDocServer.add("");

        if (resultExists(db, "invoiceLocalDB", "invoiceNumber")){
            String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ";
            Cursor c = db.rawQuery(sql, null);
            if (c.moveToFirst()) {
                int iNumber = c.getColumnIndex("invoiceNumber");
                do {
                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber);
                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
                } while (c.moveToNext());
            }
            c.close();
            for (int i = 0; i < invoiceNumbersList.size(); i++){
                Toast.makeText(getApplicationContext(), "Ничего не синхронизировано: " + invoiceNumbers, Toast.LENGTH_SHORT).show();
            }
        } else {

        }

        for (int i = 0; i < invoiceNumbersList.size(); i++){
            String sql = "SELECT salesPartnerName, accountingTypeDoc, dateTimeDocLocal, invoiceSum" +
                    " FROM invoiceLocalDB WHERE invoiceNumber LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
            sPListTmp = new String[invoiceNumbersList.size()];
            invoiceNumberNotSyncedChange = new String[invoiceNumbersList.size()];
            if (c.moveToFirst()) {
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                int dateTimeDocLocalTmp = c.getColumnIndex("dateTimeDocLocal");
                int invoiceSumTmp = c.getColumnIndex("invoiceSum");
                do {
                    salesPartnerName = c.getString(salesPartnerNameTmp);
                    String accountingTypeDoc = c.getString(accountingTypeDocTmp);
                    String dateTimeDocLocal = c.getString(dateTimeDocLocalTmp);
                    Double invoiceSum = Double.parseDouble(c.getString(invoiceSumTmp));
                    paymentStatus = "";
                    listTmp.add(new DataInvoiceLocal(salesPartnerName, accountingTypeDoc,
                            Integer.parseInt(invoiceNumberServerTmp.get(0)), dateTimeDocServer.get(0), dateTimeDocLocal,
                            invoiceSum, paymentStatus));

                } while (c.moveToNext());
                sPListTmp[i] = salesPartnerName;
                invoiceNumberNotSyncedChange[i] = String.valueOf(invoiceNumbersList.get(i));
            }
            c.close();
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerViewInvoicesLocal);
        DataAdapterViewInvoicesFromLocalDB adapter = new DataAdapterViewInvoicesFromLocalDB(this, listTmp);
        recyclerView.setAdapter(adapter);
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

