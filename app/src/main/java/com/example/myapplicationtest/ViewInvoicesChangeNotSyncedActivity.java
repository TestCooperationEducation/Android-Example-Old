package com.example.myapplicationtest;

import android.content.ContentValues;
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
            sPrefChangeInvoiceNumberNotSynced, sPrefAccountingTypeDoc, sPrefAccountingTypeSP, sPrefAreaSP;
    String paymentStatus, invoiceNumbers = "", dbName, dbUser, dbPassword, accountingTypeDocStr, areaSPStr,
            loginSecurity, areaDefault, invoiceNumberLast, salesPartnerName, accountingTypeSPStr;
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
    final String SAVED_AccountingTypeDoc = "accountingTypeDoc";
    final String SAVED_AccountingTypeSP = "accountingTypeSP";
    final String SAVED_AreaSP = "areaSP";
    ArrayList<String> arrItems, invoiceNumberServerTmp, dateTimeDocServer, itemNameList, priceList,
            quantityList, exchangeQuantityList, returnQuantityList, totalCostList;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum;
    ArrayList<Integer> arrPriceChanged, invoiceNumbersList;
    List<DataInvoice> dataArray;
    String[] sPListTmp, invoiceNumberNotSyncedChange, accountingTypeDoc, accountingTypeSP, areaSP;
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
        itemNameList = new ArrayList<>();
        priceList = new ArrayList<>();
        quantityList = new ArrayList<>();
        exchangeQuantityList = new ArrayList<>();
        returnQuantityList = new ArrayList<>();
        totalCostList = new ArrayList<>();

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
        sPrefAccountingTypeDoc = getSharedPreferences(SAVED_AccountingTypeDoc, Context.MODE_PRIVATE);
        sPrefAccountingTypeSP = getSharedPreferences(SAVED_AccountingTypeSP, Context.MODE_PRIVATE);
        sPrefAreaSP = getSharedPreferences(SAVED_AreaSP, Context.MODE_PRIVATE);

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
                                        e = sPrefAccountingTypeDoc.edit();
                                        e.putString(SAVED_AccountingTypeDoc, accountingTypeDoc[item]);
                                        e.apply();
                                        e = sPrefAccountingTypeSP.edit();
                                        e.putString(SAVED_AccountingTypeSP, accountingTypeSP[item]);
                                        e.apply();
                                        e = sPrefAreaSP.edit();
                                        e.putString(SAVED_AccountingTypeSP, areaSP[item]);
                                        e.apply();
                                        if (resultExists(db, "invoiceLocalDB", "invoiceNumber")){
                                            String sql = "SELECT * FROM invoiceLocalDB WHERE invoiceNumber LIKE ? " +
                                                    "AND salesPartnerName LIKE ?";
                                            Cursor c = db.rawQuery(sql, new String[]{String.valueOf(invoiceNumbersList.get(item)),
                                                    sPListTmp[item]});
                                            if (c.moveToFirst()) {
                                                int itemNameTmp = c.getColumnIndex("itemName");
                                                int priceTmp = c.getColumnIndex("price");
                                                int quantityTmp = c.getColumnIndex("quantity");
                                                int exchangeQuantityTmp = c.getColumnIndex("exchangeQuantity");
                                                int returnQuantityTmp = c.getColumnIndex("returnQuantity");
                                                int totalCostTmp = c.getColumnIndex("totalCost");

                                                do {
//                                                    itemNameList;
//                                                    priceList;
//                                                    quantityList;
//                                                    exchangeQuantityList;
//                                                    returnQuantityList;
//                                                    totalCostList;
//                                                    itemNameList;
                                                    String itemName = c.getString(itemNameTmp);
                                                    String price = c.getString(priceTmp);
                                                    String quantity = c.getString(quantityTmp);
                                                    String exchangeQuantity = c.getString(exchangeQuantityTmp);
                                                    String returnQuantity = c.getString(returnQuantityTmp);
                                                    String totalCost = c.getString(totalCostTmp);
                                                    ContentValues cv = new ContentValues();
                                                    Log.d(LOG_TAG, "--- Insert in itemsToInvoiceTmp: ---");
                                                    cv.put("Контрагент", sPListTmp[item]);
                                                    cv.put("Наименование", itemName);
                                                    cv.put("Цена", price);
                                                    cv.put("ЦенаИзмененная", price);
                                                    cv.put("Количество", quantity);
                                                    cv.put("Обмен", exchangeQuantity);
                                                    cv.put("Возврат", returnQuantity);
                                                    cv.put("Итого", totalCost);
                                                    long rowID = db.insert("itemsToInvoiceTmp", null, cv);
                                                    Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                                                } while (c.moveToNext());
                                            }
                                            c.close();
                                            for (int i = 0; i < invoiceNumbersList.size(); i++){
                                                Toast.makeText(getApplicationContext(), "Ничего не синхронизировано: " + invoiceNumbers, Toast.LENGTH_SHORT).show();
                                            }
                                        }

//                                        ContentValues cv = new ContentValues();
//                                        for (int i = 0; i < invoiceNumbersList.size(); i++){
//                                            Log.d(LOG_TAG, "--- Insert in itemsToInvoiceTmp: ---");
//                                            cv.put("Контрагент", sPListTmp[item]);
//                                            cv.put("Наименование", item);
//                                            cv.put("Цена", finalPrice);
//                                            cv.put("ЦенаИзмененная", priceChanged);
//                                            cv.put("Количество", tmpQuantity);
//                                            cv.put("Обмен", tmpExchange);
//                                            cv.put("Возврат", tmpReturn);
//                                            cv.put("Итого", tmpSum);
//                                            long rowID = db.insert("itemsToInvoiceTmp", null, cv);
//                                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                                        }
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

        sPListTmp = new String[invoiceNumbersList.size()];
        invoiceNumberNotSyncedChange = new String[invoiceNumbersList.size()];
        accountingTypeDoc = new String[invoiceNumbersList.size()];
        accountingTypeSP = new String[invoiceNumbersList.size()];
        areaSP = new String[invoiceNumbersList.size()];

        for (int i = 0; i < invoiceNumbersList.size(); i++){
            String sql = "SELECT DISTINCT areaSP, salesPartnerName, accountingTypeDoc, accountingTypeSP, dateTimeDocLocal, invoiceSum" +
                    " FROM invoiceLocalDB WHERE invoiceNumber LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
            if (c.moveToFirst()) {
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                int accountingTypeSPTmp = c.getColumnIndex("accountingTypeSP");
                int dateTimeDocLocalTmp = c.getColumnIndex("dateTimeDocLocal");
                int invoiceSumTmp = c.getColumnIndex("invoiceSum");
                int areaSPTmp = c.getColumnIndex("areaSP");
                areaSPStr = c.getString(areaSPTmp);
                salesPartnerName = c.getString(salesPartnerNameTmp);
                accountingTypeDocStr = c.getString(accountingTypeDocTmp);
                accountingTypeSPStr = c.getString(accountingTypeSPTmp);
                String dateTimeDocLocal = c.getString(dateTimeDocLocalTmp);
                Double invoiceSum = Double.parseDouble(c.getString(invoiceSumTmp));
                paymentStatus = "";
                listTmp.add(new DataInvoiceLocal(salesPartnerName, accountingTypeDocStr,
                        Integer.parseInt(invoiceNumberServerTmp.get(0)), dateTimeDocServer.get(0), dateTimeDocLocal,
                        invoiceSum, paymentStatus));
                c.moveToNext();
            }
            c.close();
            sPListTmp[i] = salesPartnerName;
            invoiceNumberNotSyncedChange[i] = String.valueOf(invoiceNumbersList.get(i));
            accountingTypeDoc[i] = accountingTypeDocStr;
            accountingTypeSP[i] = accountingTypeSPStr;
            areaSP[i] = areaSPStr;
            Toast.makeText(getApplicationContext(), "Список накладных: " + sPListTmp[0], Toast.LENGTH_SHORT).show();
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

