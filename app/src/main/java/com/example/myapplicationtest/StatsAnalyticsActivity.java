package com.example.myapplicationtest;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jxl.Cell;
import jxl.CellType;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class StatsAnalyticsActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnOptions;
    String dateStart = "", dateEnd = "", csvFileFormCopy, csvFileForm, areaDefault, tempDate = "2019-03-08 08:00:00",
            dbName, dbUser, dbPassword, syncUrl = "https://caiman.ru.com/php/syncDB.php";
    DBHelper dbHelper;
    final String LOG_TAG = "myLogs";
    SQLiteDatabase db;
    String[] reportList;
    ArrayMap<String, Double> arrayMapReceive, arrayMapQuantity, arrayMapExchange, arrayMapReturn,
            arrayMapQuantityReduced, arrayMapExchangeReduced, arrayMapReturnReduced, arrayMapTotal;
    ArrayMap<String, Integer> arrayMapPrice;
    EditText editTextDateStart, editTextDateEnd;
    ArrayList<String> accountingTypeList, accountingTypePaymentsList;
    ArrayList<Integer> invoiceNumberList;
    ArrayList<Double> invoiceSumList, invoiceSumPaymentsList;
    File file;
    SharedPreferences sPrefAreaDefault, sPrefDBName, sPrefDBPassword, sPrefDBUser;;
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    Integer invoiceAccTypeOneCount, invoiceAccTypeTwoCount, invoiceAccTypeOnePaymentsCount, invoiceAccTypeTwoPaymentsCount;
    Double invoiceSumTotal, invoiceSumTypeOneTotal, invoiceSumTypeTwoTotal, invoiceSumTypeOnePaymentsTotal, invoiceSumTypeTwoPaymentsTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_analytics);

        btnOptions = findViewById(R.id.buttonOptions);
        btnOptions.setOnClickListener(this);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        editTextDateEnd = findViewById(R.id.editTextDateEnd);
        editTextDateStart = findViewById(R.id.editTextDateStart);

        sPrefAreaDefault = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        Toast.makeText(getApplicationContext(), areaDefault, Toast.LENGTH_SHORT).show();
        onChangeListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonOptions:
                mainMenu();
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder = new AlertDialog.Builder(this);
//                builder.setTitle("Обновление локальной базы данных")
//                        .setMessage("Все таблицы будут перезаписаны!")
//                        .setCancelable(true)
//                        .setNegativeButton("Да",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        updateLocalDB();
//                                        dialog.cancel();
//                                    }
//                                })
//                        .setPositiveButton("Нет",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        dialog.cancel();
//                                    }
//                                });
//                AlertDialog alert = builder.create();
//                alert.show();
//                break;
            default:
                break;
        }
    }

    private void mainMenu(){
        final String[] choice ={"Посмотреть продажи", "Сформировать", "По умолчанию", "Обновить БД"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Меню");
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("Посмотреть продажи")){

                    dialog.cancel();
                }
                if (choice[item].equals("Обновить БД")){
                    updateLocalDB();
                    dialog.cancel();
                }
                if (choice[item].equals("Сформировать")){
                    executeChoice();
                    makeExcel();
                    try {
                        printReport();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dialog.cancel();
                }
                if (choice[item].equals("По умолчанию")){
                    dateEnd = "";
                    dateStart = "";
                    Toast.makeText(getApplicationContext(), "Сброшено по умолчанию", Toast.LENGTH_SHORT).show();
                    mainMenu();
                }
            }
        });
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void executeChoice(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
        LocalDateTime d = LocalDateTime.parse(tempDate, formatter);
        final String output = d.with(LocalTime.MIN).format( formatter );
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();

        ArrayList<String> itemNameListDefault;
        Double quantity, exchange, returnQuantity, total;
        Integer price;
        arrayMapQuantity = new ArrayMap<>();
        arrayMapExchange = new ArrayMap<>();
        arrayMapReturn = new ArrayMap<>();
        arrayMapTotal = new ArrayMap<>();
        arrayMapPrice = new ArrayMap<>();
        arrayMapQuantityReduced = new ArrayMap<>();
        arrayMapExchangeReduced = new ArrayMap<>();
        arrayMapReturnReduced = new ArrayMap<>();
        invoiceAccTypeTwoCount = 0;
        invoiceAccTypeOneCount = 0;
        invoiceSumTypeOneTotal = 0d;
        invoiceSumTypeTwoTotal = 0d;
        invoiceSumTotal = 0d;
        invoiceNumberList = new ArrayList<>();
        accountingTypeList = new ArrayList<>();
        invoiceSumList = new ArrayList<>();
        invoiceSumTypeOnePaymentsTotal = 0d;
        invoiceSumTypeTwoPaymentsTotal = 0d;
        invoiceAccTypeOnePaymentsCount = 0;
        invoiceAccTypeTwoPaymentsCount = 0;
        accountingTypePaymentsList = new ArrayList<>();
        invoiceSumPaymentsList = new ArrayList<>();

//        String sql = "SELECT items.Наименование FROM items ";
//        Cursor c = db.rawQuery(sql, null);
//        if (c.moveToFirst()) {
//            int itemNameTmp = c.getColumnIndex("Наименование");
//            itemNameListDefault = new ArrayList<>();
//            do {
//                itemNameListDefault.add(c.getString(itemNameTmp));
//            } while (c.moveToNext());
//        }
        String sql, sql2, sql3;
        Cursor c, c2, c3;
        if (dateStart.length() > 0 && dateEnd.length() > 0){
            sql = "SELECT items.Наименование, invoiceAggregate.Quantity, invoiceAggregate.ExchangeQuantity," +
                    "invoiceAggregate.ReturnQuantity, invoiceAggregate.Price, invoiceAggregate.Total FROM invoiceAggregate INNER JOIN items " +
                    "ON invoiceAggregate.ItemID LIKE items.Артикул " +
                    "WHERE invoiceAggregate.DateTimeDoc BETWEEN ? AND ?";
            c = db.rawQuery(sql, new String[]{dateStart, dateEnd});

            sql2 = "SELECT DISTINCT InvoiceNumber, AccountingType, InvoiceSum FROM invoice WHERE DateTimeDoc BETWEEN ? AND ?";
            c2 = db.rawQuery(sql2, new String[]{dateStart, dateEnd});

            sql3 = "SELECT DISTINCT invoice.InvoiceNumber, invoice.AccountingType, invoice.InvoiceSum FROM invoice INNER JOIN " +
                    "paymentsServer ON invoice.InvoiceNumber = paymentsServer.InvoiceNumber " +
                    "WHERE invoice.DateTimeDoc BETWEEN ? AND ?";
            c3 = db.rawQuery(sql3, new String[]{dateStart, dateEnd});
        } else {
            sql = "SELECT items.Наименование, invoiceAggregate.Quantity, invoiceAggregate.ExchangeQuantity," +
                    "invoiceAggregate.ReturnQuantity, invoiceAggregate.Price, invoiceAggregate.Total FROM invoiceAggregate INNER JOIN items " +
                    "ON invoiceAggregate.ItemID LIKE items.Артикул " +
                    "WHERE invoiceAggregate.DateTimeDoc > ?";
            c = db.rawQuery(sql, new String[]{output});

            sql2 = "SELECT DISTINCT InvoiceNumber, AccountingType, InvoiceSum FROM invoice WHERE DateTimeDoc > ?";
            c2 = db.rawQuery(sql2, new String[]{output});

            sql3 = "SELECT DISTINCT invoice.InvoiceNumber, invoice.AccountingType, invoice.InvoiceSum FROM invoice INNER JOIN " +
                    "paymentsServer ON invoice.InvoiceNumber = paymentsServer.InvoiceNumber " +
                    "WHERE invoice.DateTimeDoc > ?";
            c3 = db.rawQuery(sql3, new String[]{output});
        }
        if (c3.moveToFirst()){
            int accountingTypeTmp = c3.getColumnIndex("AccountingType");
            int invoiceSumTmp = c3.getColumnIndex("InvoiceSum");

            do {
                accountingTypePaymentsList.add(c3.getString(accountingTypeTmp));
                invoiceSumPaymentsList.add(c3.getDouble(invoiceSumTmp));
            } while (c3.moveToNext());
        }
        for (int i = 0; i < accountingTypePaymentsList.size(); i++){
            if (accountingTypePaymentsList.get(i).equals("провод")){
                invoiceAccTypeOnePaymentsCount += 1;
                invoiceSumTypeOnePaymentsTotal += invoiceSumPaymentsList.get(i);

            } else {
                invoiceAccTypeTwoPaymentsCount += 1;
                invoiceSumTypeTwoPaymentsTotal += invoiceSumPaymentsList.get(i);
            }
        }

        if (c2.moveToFirst()){
            int invoiceNumberTmp = c2.getColumnIndex("InvoiceNumber");
            int accountingTypeTmp = c2.getColumnIndex("AccountingType");
            int invoiceSumTmp = c2.getColumnIndex("InvoiceSum");

            do {
                invoiceNumberList.add(c2.getInt(invoiceNumberTmp));
                accountingTypeList.add(c2.getString(accountingTypeTmp));
                invoiceSumList.add(c2.getDouble(invoiceSumTmp));
            } while (c2.moveToNext());
        }
        for (int i = 0; i < accountingTypeList.size(); i++){
            if (accountingTypeList.get(i).equals("провод")){
                invoiceAccTypeOneCount += 1;
                invoiceSumTypeOneTotal += invoiceSumList.get(i);

            } else {
                invoiceAccTypeTwoCount += 1;
                invoiceSumTypeTwoTotal += invoiceSumList.get(i);
            }
            invoiceSumTotal += invoiceSumList.get(i);
        }

        if (c.moveToFirst()) {
            int itemNameTmp = c.getColumnIndex("Наименование");
            int quantityInvoiceTmp = c.getColumnIndex("Quantity");
            int exchangeQuantityTmp = c.getColumnIndex("ExchangeQuantity");
            int returnQuantityTmp = c.getColumnIndex("ReturnQuantity");
            int priceTmp = c.getColumnIndex("Price");
            int totalTmp = c.getColumnIndex("Total");
            ArrayList<String> itemNameList = new ArrayList<>();
            ArrayList<Double> totalList = new ArrayList<>();
            ArrayList<Double> quantityInvoiceList = new ArrayList<>();
            ArrayList<Double> exchangeQuantityList = new ArrayList<>();
            ArrayList<Double> returnQuantityList = new ArrayList<>();
            ArrayList<Integer> priceList = new ArrayList<>();
            do {
                itemNameList.add(c.getString(itemNameTmp));
                quantityInvoiceList.add(c.getDouble(quantityInvoiceTmp));
                exchangeQuantityList.add(c.getDouble(exchangeQuantityTmp));
                returnQuantityList.add(c.getDouble(returnQuantityTmp));
                totalList.add(c.getDouble(totalTmp));
                priceList.add(c.getInt(priceTmp));

                if (arrayMapQuantity.containsKey(c.getString(itemNameTmp))) {
                    quantity = arrayMapQuantity.get(c.getString(itemNameTmp)) + c.getDouble((quantityInvoiceTmp));
                } else {
                    quantity =c.getDouble((quantityInvoiceTmp));
                }
                if (arrayMapExchange.containsKey(c.getString(itemNameTmp))) {
                    exchange = arrayMapExchange.get(c.getString(itemNameTmp)) + c.getDouble((exchangeQuantityTmp));
                } else {
                    exchange =c.getDouble((exchangeQuantityTmp));
                }
                if (arrayMapReturn.containsKey(c.getString(itemNameTmp))) {
                    returnQuantity = arrayMapReturn.get(c.getString(itemNameTmp)) + c.getDouble((returnQuantityTmp));
                } else {
                    returnQuantity =c.getDouble((returnQuantityTmp));
                }
                if (arrayMapPrice.containsKey(c.getString(itemNameTmp))) {
                    price = arrayMapPrice.get(c.getString(itemNameTmp)) + c.getInt((priceTmp));
                } else {
                    price =c.getInt((priceTmp));
                }
                if (arrayMapTotal.containsKey(c.getString(itemNameTmp))) {
                    total = arrayMapTotal.get(c.getString(itemNameTmp)) + c.getDouble((totalTmp));
                } else {
                    total =c.getDouble((totalTmp));
                }
                arrayMapQuantity.put(c.getString(itemNameTmp), quantity);
                arrayMapExchange.put(c.getString(itemNameTmp), exchange);
                arrayMapReturn.put(c.getString(itemNameTmp), returnQuantity);
                arrayMapTotal.put(c.getString(itemNameTmp), total);
                arrayMapPrice.put(c.getString(itemNameTmp), price);
            } while (c.moveToNext());

            for (int i = 0; i < arrayMapQuantity.size(); i++){
                if (!arrayMapQuantity.keyAt(i).equals("Ким-ча 700 гр особая цена 1") &&
                        !arrayMapQuantity.keyAt(i).equals("Ким-ча 700 гр особая цена 2") &&
                        !arrayMapQuantity.keyAt(i).equals("Редька по-восточному 500гр особая цена 1") &&
                        !arrayMapQuantity.keyAt(i).equals("Редька по-восточному 500гр особая цена 2")){
                    if (arrayMapQuantity.keyAt(i).equals("Ким-ча весовая")) {
                        if (arrayMapQuantityReduced.containsKey("Ким-ча весовая") &&
                                arrayMapExchangeReduced.containsKey("Ким-ча весовая") &&
                                arrayMapReturnReduced.containsKey("Ким-ча весовая")) {
                            arrayMapQuantityReduced.put("Ким-ча весовая", arrayMapQuantityReduced.get("Ким-ча весовая") + (arrayMapQuantity.valueAt(i)));
                            arrayMapExchangeReduced.put("Ким-ча весовая", arrayMapExchangeReduced.get("Ким-ча весовая") + (arrayMapExchange.valueAt(i)));
                            arrayMapReturnReduced.put("Ким-ча весовая", arrayMapReturnReduced.get("Ким-ча весовая") + (arrayMapReturn.valueAt(i)));
                        } else {
                            arrayMapQuantityReduced.put("Ким-ча весовая", (arrayMapQuantity.valueAt(i)));
                            arrayMapExchangeReduced.put("Ким-ча весовая", (arrayMapExchange.valueAt(i)));
                            arrayMapReturnReduced.put("Ким-ча весовая", (arrayMapReturn.valueAt(i)));
                        }
                    }

                    if (arrayMapQuantity.keyAt(i).equals("Редька по-восточному весовая")){
                        if (arrayMapQuantityReduced.containsKey("Редька по-восточному весовая") &&
                                arrayMapExchangeReduced.containsKey("Редька по-восточному весовая") &&
                                arrayMapReturnReduced.containsKey("Редька по-восточному весовая")) {
                            arrayMapQuantityReduced.put("Редька по-восточному весовая", arrayMapQuantityReduced.get("Редька по-восточному весовая") + (arrayMapQuantity.valueAt(i)));
                            arrayMapExchangeReduced.put("Редька по-восточному весовая", arrayMapExchangeReduced.get("Редька по-восточному весовая") + (arrayMapExchange.valueAt(i)));
                            arrayMapReturnReduced.put("Редька по-восточному весовая", arrayMapReturnReduced.get("Редька по-восточному весовая") + (arrayMapReturn.valueAt(i)));
                        } else {
                            arrayMapQuantityReduced.put("Редька по-восточному весовая", (arrayMapQuantity.valueAt(i)));
                            arrayMapExchangeReduced.put("Редька по-восточному весовая", (arrayMapExchange.valueAt(i)));
                            arrayMapReturnReduced.put("Редька по-восточному весовая", (arrayMapReturn.valueAt(i)));
                        }
                    }

                    if (!arrayMapQuantity.keyAt(i).equals("Ким-ча весовая") &&
                            !arrayMapQuantity.keyAt(i).equals("Редька по-восточному весовая")) {
                        arrayMapQuantityReduced.put(arrayMapQuantity.keyAt(i), arrayMapQuantity.valueAt(i));
                        arrayMapExchangeReduced.put(arrayMapQuantity.keyAt(i), arrayMapExchange.valueAt(i));
                        arrayMapReturnReduced.put(arrayMapQuantity.keyAt(i), arrayMapReturn.valueAt(i));
                    }
                }
                if (arrayMapQuantity.keyAt(i).equals("Ким-ча 700 гр особая цена 1")) {
                    if (arrayMapQuantityReduced.containsKey("Ким-ча весовая") &&
                            arrayMapExchangeReduced.containsKey("Ким-ча весовая") &&
                            arrayMapReturnReduced.containsKey("Ким-ча весовая")) {
                        arrayMapQuantityReduced.put("Ким-ча весовая", arrayMapQuantityReduced.get("Ким-ча весовая") + (arrayMapQuantity.valueAt(i) * 0.7));
                        arrayMapExchangeReduced.put("Ким-ча весовая", arrayMapExchangeReduced.get("Ким-ча весовая") + (arrayMapExchange.valueAt(i) * 0.7));
                        arrayMapReturnReduced.put("Ким-ча весовая", arrayMapReturnReduced.get("Ким-ча весовая") + (arrayMapReturn.valueAt(i) * 0.7));
                    } else {
                        arrayMapQuantityReduced.put("Ким-ча весовая", (arrayMapQuantity.valueAt(i) * 0.7));
                        arrayMapExchangeReduced.put("Ким-ча весовая", (arrayMapExchange.valueAt(i) * 0.7));
                        arrayMapReturnReduced.put("Ким-ча весовая", (arrayMapReturn.valueAt(i) * 0.7));
                    }
                }

                if (arrayMapQuantity.keyAt(i).equals("Ким-ча 700 гр особая цена 2")) {
                    if (arrayMapQuantityReduced.containsKey("Ким-ча весовая") &&
                            arrayMapExchangeReduced.containsKey("Ким-ча весовая") &&
                            arrayMapReturnReduced.containsKey("Ким-ча весовая")) {
                        arrayMapQuantityReduced.put("Ким-ча весовая", arrayMapQuantityReduced.get("Ким-ча весовая") + (arrayMapQuantity.valueAt(i) * 0.7));
                        arrayMapExchangeReduced.put("Ким-ча весовая", arrayMapExchangeReduced.get("Ким-ча весовая") + (arrayMapExchange.valueAt(i) * 0.7));
                        arrayMapReturnReduced.put("Ким-ча весовая", arrayMapReturnReduced.get("Ким-ча весовая") + (arrayMapReturn.valueAt(i) * 0.7));
                    }  else {
                        arrayMapQuantityReduced.put("Ким-ча весовая", (arrayMapQuantity.valueAt(i) * 0.7));
                        arrayMapExchangeReduced.put("Ким-ча весовая", (arrayMapExchange.valueAt(i) * 0.7));
                        arrayMapReturnReduced.put("Ким-ча весовая", (arrayMapReturn.valueAt(i) * 0.7));
                    }
                }

                if (arrayMapQuantity.keyAt(i).equals("Редька по-восточному 500гр особая цена 1")) {
                    if (arrayMapQuantityReduced.containsKey("Редька по-восточному весовая") &&
                            arrayMapExchangeReduced.containsKey("Редька по-восточному весовая") &&
                            arrayMapReturnReduced.containsKey("Редька по-восточному весовая")) {
                        arrayMapQuantityReduced.put("Редька по-восточному весовая", arrayMapQuantityReduced.get("Редька по-восточному весовая") + (arrayMapQuantity.valueAt(i) * 0.5));
                        arrayMapExchangeReduced.put("Редька по-восточному весовая", arrayMapExchangeReduced.get("Редька по-восточному весовая") + (arrayMapExchange.valueAt(i) * 0.5));
                        arrayMapReturnReduced.put("Редька по-восточному весовая", arrayMapReturnReduced.get("Редька по-восточному весовая") + (arrayMapReturn.valueAt(i) * 0.5));
                    } else {
                        arrayMapQuantityReduced.put("Редька по-восточному весовая", (arrayMapQuantity.valueAt(i) * 0.5));
                        arrayMapExchangeReduced.put("Редька по-восточному весовая", (arrayMapExchange.valueAt(i) * 0.5));
                        arrayMapReturnReduced.put("Редька по-восточному весовая", (arrayMapReturn.valueAt(i) * 0.5));
                    }
                }

                if (arrayMapQuantity.keyAt(i).equals("Редька по-восточному 500гр особая цена 2")) {
                    if (arrayMapQuantityReduced.containsKey("Редька по-восточному весовая") &&
                            arrayMapExchangeReduced.containsKey("Редька по-восточному весовая") &&
                            arrayMapReturnReduced.containsKey("Редька по-восточному весовая")) {
                        arrayMapQuantityReduced.put("Редька по-восточному весовая", arrayMapQuantityReduced.get("Редька по-восточному весовая") + (arrayMapQuantity.valueAt(i) * 0.5));
                        arrayMapExchangeReduced.put("Редька по-восточному весовая", arrayMapExchangeReduced.get("Редька по-восточному весовая") + (arrayMapExchange.valueAt(i) * 0.5));
                        arrayMapReturnReduced.put("Редька по-восточному весовая", arrayMapReturnReduced.get("Редька по-восточному весовая") + (arrayMapReturn.valueAt(i) * 0.5));
                    } else {
                        arrayMapQuantityReduced.put("Редька по-восточному весовая", (arrayMapQuantity.valueAt(i) * 0.5));
                        arrayMapExchangeReduced.put("Редька по-восточному весовая", (arrayMapExchange.valueAt(i) * 0.5));
                        arrayMapReturnReduced.put("Редька по-восточному весовая", (arrayMapReturn.valueAt(i) * 0.5));
                    }
                }
            }
//            reportList = new String[arrayMapQuantityReduced.size()];
//            for (int i = 0; i < arrayMapQuantityReduced.size(); i++) {
//                Double tmp = arrayMapReceive.valueAt(i) - arrayMapQuantityReduced.valueAt(i) - arrayMapExchangeReduced.valueAt(i);
//                reportList[i] = "Обмен: " + arrayMapExchangeReduced.valueAt(i).toString() + System.getProperty("line.separator") +
//                        "Наименование: " + System.getProperty("line.separator") +
//                        arrayMapExchangeReduced.keyAt(i) + System.getProperty("line.separator") +
//                        "Остаток: " + roundUp(tmp, 2).toString() + System.getProperty("line.separator") +
//                        "Продажа: " + arrayMapQuantityReduced.valueAt(i).toString() + System.getProperty("line.separator") +
//                        "Загрузка: " + arrayMapReceive.valueAt(i).toString() + System.getProperty("line.separator") +
//                        System.getProperty("line.separator");
//            }
//            showReceiveReport("Конец смены");
        }
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myLocalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");

            if (!tableExists(db, "salesPartners")) {
                db.execSQL("create table salesPartners ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                        + "Наименование text,"
                        + "Район integer,"
                        + "Учет text,"
                        + "DayOfTheWeek text,"
                        + "Автор text" + ");");
            }

            if (!tableExists(db, "items")) {
                db.execSQL("create table items ("
                        + "id integer primary key autoincrement,"
                        + "Артикул integer UNIQUE ON CONFLICT REPLACE,"
                        + "Наименование text,"
                        + "Описание text,"
                        + "Цена integer" + ");");
            }

            if (!tableExists(db, "itemsWithDiscount")) {
                db.execSQL("create table itemsWithDiscount ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                        + "Артикул integer,"
                        + "ID_скидки integer,"
                        + "ID_контрагента integer,"
                        + "Автор text" + ");");
            }

            if (!tableExists(db, "discount")) {
                db.execSQL("create table discount ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                        + "Тип_скидки integer,"
                        + "Скидка integer,"
                        + "Автор текст" + ");");
            }

            if (!tableExists(db, "invoice")) {
                db.execSQL("create table invoice ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                        + "InvoiceNumber integer,"
                        + "AgentID integer,"
                        + "SalesPartnerID integer,"
                        + "AccountingType text,"
                        + "ItemID integer,"
                        + "Quantity real,"
                        + "Price real,"
                        + "Total real,"
                        + "ExchangeQuantity real,"
                        + "ReturnQuantity  real,"
                        + "DateTimeDoc text,"
                        + "InvoiceSum real,"
                        + "Comment text" + ");");
            }

            if (!tableExists(db, "invoiceAggregate")) {
                db.execSQL("create table invoiceAggregate ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer,"
                        + "InvoiceNumber integer,"
                        + "AgentID integer,"
                        + "SalesPartnerID integer,"
                        + "AccountingType text,"
                        + "ItemID integer,"
                        + "Quantity real,"
                        + "Price real,"
                        + "Total real,"
                        + "ExchangeQuantity real,"
                        + "ReturnQuantity  real,"
                        + "DateTimeDoc text,"
                        + "InvoiceSum real,"
                        + "Comment text" + ");");
            }

            if (!tableExists(db, "paymentsServer")) {
                db.execSQL("create table paymentsServer ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer UNIQUE,"
                        + "DateTimeDoc text,"
                        + "InvoiceNumber integer,"
                        + "сумма_внесения real,"
                        + "Автор text" + ");");
            }

            if (!tableExists(db, "payments")) {
                db.execSQL("create table payments ("
                        + "id integer primary key autoincrement,"
                        + "DateTimeDoc text,"
                        + "InvoiceNumber integer,"
                        + "сумма_внесения real,"
                        + "Автор text" + ");");
            }

            if (!tableExists(db, "itemsToInvoiceTmp")) {
                db.execSQL("create table itemsToInvoiceTmp ("
                        + "id integer primary key autoincrement,"
                        + "Контрагент text,"
                        + "Наименование text UNIQUE ON CONFLICT REPLACE,"
                        + "Цена integer,"
                        + "ЦенаИзмененная integer,"
                        + "Количество real,"
                        + "Обмен real,"
                        + "Возврат real,"
                        + "Итого real" + ");");
            }

            if (!tableExists(db, "invoiceLocalDB")) {
                db.execSQL("create table invoiceLocalDB ("
                        + "id integer primary key autoincrement,"
                        + "invoiceNumber integer,"
                        + "agentID integer,"
                        + "areaSP integer,"
                        + "salesPartnerName text,"
                        + "accountingTypeDoc text,"
                        + "accountingTypeSP text,"
                        + "itemName text,"
                        + "quantity real,"
                        + "price integer,"
                        + "totalCost real,"
                        + "exchangeQuantity real,"
                        + "returnQuantity real,"
                        + "comment text DEFAULT 'none',"
                        + "dateTimeDocLocal text,"
                        + "invoiceSum text" + ");");
            }

            if (!tableExists(db, "syncedInvoice")) {
                db.execSQL("create table syncedInvoice ("
                        + "id integer primary key autoincrement,"
                        + "invoiceNumber integer,"
                        + "dateTimeDoc text,"
                        + "agentID integer" + ");");
            }

            if (!tableExists(db, "syncedPayments")) {
                db.execSQL("create table syncedPayments ("
                        + "id integer primary key autoincrement,"
                        + "paymentID integer UNIQUE,"
                        + "invoiceNumber integer,"
                        + "dateTimeDoc text,"
                        + "agentID integer" + ");");
            }

            if (!tableExists(db, "receive")) {
                db.execSQL("create table receive ("
                        + "id integer primary key autoincrement,"
                        + "itemID integer,"
                        + "quantity real,"
                        + "dateTimeDoc text,"
                        + "agentID integer" + ");");
            }

            if (!tableExists(db, "receiveLocal")) {
                db.execSQL("create table receiveLocal ("
                        + "id integer primary key autoincrement,"
                        + "itemID integer,"
                        + "quantity real,"
                        + "dateTimeDoc text,"
                        + "agentID integer" + ");");
            }

            if (!tableExists(db, "agents")) {
                db.execSQL("create table agents ("
                        + "id integer primary key autoincrement,"
                        + "area integer,"
                        + "secondName text,"
                        + "firstName text,"
                        + "middleName text" + ");");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }

    private void onChangeListener() {
        editTextDateStart.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextDateStart.getText().toString().trim().length() > 0) {
                    dateStart = editTextDateStart.getText().toString();
                }
            }
        });

        editTextDateEnd.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextDateEnd.getText().toString().trim().length() > 0) {
                    dateEnd = editTextDateEnd.getText().toString();
                }
            }
        });
    }

    public BigDecimal roundUp(double value, int digits){
        return new BigDecimal(""+value).setScale(digits, BigDecimal.ROUND_HALF_UP);
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

    private void printReport()  throws IOException {
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
        String output = zdt.format( formatter );

        File sd = Environment.getExternalStorageDirectory();
        csvFileFormCopy = "руководитель_отчет_" + output + ".xls";
        File directorySave = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Руководитель_отчет");
        if (!directorySave.isDirectory()) {
            directorySave.mkdirs();
        }
        File fileSave = new File(directorySave, csvFileFormCopy);
        File inputWorkbook = file;
        Workbook w;
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("ru", "Ru"));
        try {
            w = Workbook.getWorkbook(inputWorkbook);
            WritableWorkbook copy = Workbook.createWorkbook(fileSave, w);
            WritableSheet sheet = copy.getSheet(0);

//            for (int j = 0; j < 6; j++){
//                for (int i = 0; i < sheet.getRows(); i++){
//                    WritableCell cell = sheet.getWritableCell(j, i);
//                    CellFormat cfm = cell.getCellFormat();
//                    if (j == 2 && i == 2) {
//                        if (cell.getType() == CellType.LABEL) {
//                            Label l = (Label) cell;
//                            l.setString("Дата: " + output); //Дата
//                        }
//                    }
//                }
//            }

            for (int j = 0; j < 6; j++) {
                for (int i = 0; i < arrayMapQuantityReduced.size(); i++) {
                    Cell readCell = sheet.getCell(j, i);
                    Label label = new Label(j, i, readCell.getContents());
                    CellView cell = sheet.getColumnView(j);
                    cell.setAutosize(true);
                    sheet.setColumnView(j, cell);

                    if (j == 2 && i == 2) {
                        label = new Label(j, i, output); //Текущая дата
                    }
                    if (j == 0) {
                        label = new Label(j, i, String.valueOf(arrayMapQuantityReduced.keyAt(i))); //Наименование
                    }
                    sheet.addCell(label);
                }
            }

            copy.write();
            copy.close();
            w.close();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    private void makeExcel(){
        File sd = Environment.getExternalStorageDirectory();
        csvFileForm = "ceoReport.xls";
        File directory = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Руководитель_отчет_форма");
        file = new File(directory, csvFileForm);

    }

    private void receiveUpdateFromServerPrompt(){
        final boolean[] listChecked = new boolean[7];
        final String[] listCheckTableName = { "Контрагенты", "Номенклатура", "Индивидуальные скидки",
                "Тип скидки", "Накладная", "Платежи", "Агенты" };
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите таблицу(цы) для обновления")
                .setCancelable(false)
                .setMultiChoiceItems(listCheckTableName, listChecked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                listChecked[which] = isChecked;
                            }
                        })
                .setPositiveButton("Обновить",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                for (int i = 0; i < listCheckTableName.length; i++) {
                                    if (listChecked[i] == true){
                                        if (listCheckTableName[i].equals("Контрагенты")){
                                            db.execSQL("DROP TABLE IF EXISTS salesPartners");
                                            dbHelper.onUpgrade(db, 1, 2);
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {

                                                    loadSalesPartnersFromServerDB();
                                                }
                                            };
                                            Thread thread1 = new Thread(runnable);
                                            thread1.start();
                                        }
                                        if (listCheckTableName[i].equals("Номенклатура")){
                                            db.execSQL("DROP TABLE IF EXISTS items");
                                            dbHelper.onUpgrade(db, 1, 2);
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {

                                                    loadItemsFromServerDB();
                                                }
                                            };
                                            Thread thread2 = new Thread(runnable);
                                            thread2.start();
                                        }
                                        if (listCheckTableName[i].equals("Индивидуальные скидки")){
                                            db.execSQL("DROP TABLE IF EXISTS itemsWithDiscount");
                                            dbHelper.onUpgrade(db, 1, 2);
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {

                                                    loadItemsWithDiscountsFromServerDB();
                                                }
                                            };
                                            Thread thread3 = new Thread(runnable);
                                            thread3.start();
                                        }
                                        if (listCheckTableName[i].equals("Тип скидки")){
                                            db.execSQL("DROP TABLE IF EXISTS discount");
                                            dbHelper.onUpgrade(db, 1, 2);
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {

                                                    loadDiscountsFromServerDB();
                                                }
                                            };
                                            Thread thread4 = new Thread(runnable);
                                            thread4.start();
                                        }
                                        if (listCheckTableName[i].equals("Накладная")){
                                            db.execSQL("DROP TABLE IF EXISTS invoiceAggregate");
                                            db.execSQL("DROP TABLE IF EXISTS invoice");
                                            dbHelper.onUpgrade(db, 1, 2);
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    for (int g = 1; g < 2; g++) {
                                                        loadInvoicesFromServerDB(g);
                                                    }
                                                }
                                            };
                                            Thread thread5 = new Thread(runnable);
                                            thread5.start();
                                        }
                                        if (listCheckTableName[i].equals("Платежи")){
                                            db.execSQL("DROP TABLE IF EXISTS paymentsServer");
                                            dbHelper.onUpgrade(db, 1, 2);
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {

                                                    loadPaymentsFromServerDB();
                                                }
                                            };
                                            Thread thread6 = new Thread(runnable);
                                            thread6.start();
                                        }
                                        if (listCheckTableName[i].equals("Агенты")){
                                            db.execSQL("DROP TABLE IF EXISTS agents");
                                            dbHelper.onUpgrade(db, 1, 2);
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {

                                                    loadAgentsFromServerDB();
                                                }
                                            };
                                            Thread thread7 = new Thread(runnable);
                                            thread7.start();
                                        }
                                    }
                                }
                            }
                        })
                .setNegativeButton("Я передумал(а)",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();

                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void loadSalesPartnersFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    String[] dayOfTheWeek = new String[jsonArray.length()];
                    String[] salesPartnersName= new String[jsonArray.length()];
                    Integer[] area= new Integer[jsonArray.length()];
                    String[] accountingType= new String[jsonArray.length()];
                    String[] author= new String[jsonArray.length()];
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("DayOfTheWeek");
                            salesPartnersName[i] = obj.getString("Наименование");
                            area[i] = obj.getInt("Район");
                            accountingType[i] = obj.getString("Учет");
                            author[i] = obj.getString("Автор");
                            serverDB_ID[i] = obj.getInt("ID");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in salesPartners: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("Наименование", salesPartnersName[i]);
                            cv.put("Район", area[i]);
                            cv.put("Учет", accountingType[i]);
                            cv.put("DayOfTheWeek", dayOfTheWeek[i]);
                            cv.put("Автор", author[i]);
                            long rowID = db.insert("salesPartners", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "Контрагенты загружены", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "salesPartners");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadItemsFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] itemNumber = new Integer[jsonArray.length()];
                    String[] itemName = new String[jsonArray.length()];
                    Integer[] itemPrice = new Integer[jsonArray.length()];
                    String[] itemDescription = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemNumber[i] = obj.getInt("Артикул");
                            itemName[i] = obj.getString("Наименование");
                            itemDescription[i] = obj.getString("Описание");
                            itemPrice[i] = obj.getInt("Цена");
                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in items: ---");
                            cv.put("Артикул", itemNumber[i]);
                            cv.put("Наименование", itemName[i]);
                            cv.put("Описание", itemName[i]);
                            cv.put("Цена", itemPrice[i]);
                            long rowID = db.insert("items", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "Номенклатура загружена", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "items");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadItemsWithDiscountsFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];
                    Integer[] itemNumber= new Integer[jsonArray.length()];
                    Integer[] discountID= new Integer[jsonArray.length()];
                    Integer[] spID = new Integer[jsonArray.length()];
                    String[] author = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            itemNumber[i] = obj.getInt("Артикул");
                            discountID[i] = obj.getInt("ID_скидки");
                            spID[i] = obj.getInt("ID_контрагента");
                            author[i] = obj.getString("Автор");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in itemsWithDiscount: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("Артикул", itemNumber[i]);
                            cv.put("ID_скидки", discountID[i]);
                            cv.put("ID_контрагента", spID[i]);
                            cv.put("Автор", author[i]);
                            long rowID = db.insert("itemsWithDiscount", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "Индивидуальные скидки загружены", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "номенклатурасоскидкой");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadDiscountsFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];
                    Integer[] discountType= new Integer[jsonArray.length()];
                    Integer[] discount= new Integer[jsonArray.length()];
                    String[] author = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            discountType[i] = obj.getInt("Тип_скидки");
                            discount[i] = obj.getInt("Скидка");
                            author[i] = obj.getString("Автор");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in discount: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("Тип_скидки", discountType[i]);
                            cv.put("Скидка", discount[i]);
                            cv.put("Автор", author[i]);
                            long rowID = db.insert("discount", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "Типы скидок загружены", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "скидка");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadInvoicesFromServerDB(final Integer g){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];
                    Integer[] invoiceNumber= new Integer[jsonArray.length()];
                    Integer[] agentID= new Integer[jsonArray.length()];
                    Integer[] salesPartnerID = new Integer[jsonArray.length()];
                    String[] accountingType = new String[jsonArray.length()];
                    Integer[] itemNumber = new Integer[jsonArray.length()];
                    Double[] itemQuantity = new Double[jsonArray.length()];
                    Integer[] itemPrice = new Integer[jsonArray.length()];
                    Double[] totalSum = new Double[jsonArray.length()];
                    Double[] exchangeQuantity = new Double[jsonArray.length()];
                    Double[] returnQuantity = new Double[jsonArray.length()];
                    String[] dateTimeDoc = new String[jsonArray.length()];
                    Double[] invoiceSum = new Double[jsonArray.length()];
                    String[] comment = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            invoiceNumber[i] = obj.getInt("InvoiceNumber");
                            agentID[i] = obj.getInt("AgentID");
                            salesPartnerID[i] = obj.getInt("SalesPartnerID");
                            accountingType[i] = obj.getString("AccountingType");
                            itemNumber[i] = obj.getInt("ItemID");
                            itemQuantity[i] = obj.getDouble("Quantity");
                            itemPrice[i] = obj.getInt("Price");
                            totalSum[i] = obj.getDouble("Total");
                            exchangeQuantity[i] = obj.getDouble("ExchangeQuantity");
                            returnQuantity[i] = obj.getDouble("ReturnQuantity");
                            dateTimeDoc[i] = obj.getString("DateTimeDocLocal");
                            invoiceSum[i] = obj.getDouble("InvoiceSum");
                            comment[i] = obj.getString("Comment");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in invoice: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("InvoiceNumber", invoiceNumber[i]);
                            cv.put("AgentID", agentID[i]);
                            cv.put("SalesPartnerID", salesPartnerID[i]);
                            cv.put("AccountingType", accountingType[i]);
                            cv.put("ItemID", itemNumber[i]);
                            cv.put("Quantity", itemQuantity[i]);
                            cv.put("Price", itemPrice[i]);
                            cv.put("Total", totalSum[i]);
                            cv.put("ExchangeQuantity", exchangeQuantity[i]);
                            cv.put("ReturnQuantity", returnQuantity[i]);
                            cv.put("DateTimeDoc", dateTimeDoc[i]);
                            cv.put("InvoiceSum", invoiceSum[i]);
                            cv.put("Comment", comment[i]);
                            long rowID = db.insert("invoiceAggregate", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "Накладные загружены", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Пустой результат", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("agentID", String.valueOf(g));
                parameters.put("tableName", "invoice");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadPaymentsFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];
                    String[] dateTimeDoc = new String[jsonArray.length()];
                    Integer[] invoiceNumber = new Integer[jsonArray.length()];
                    Double[] paymentAmount= new Double[jsonArray.length()];
                    String[] author = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            dateTimeDoc[i] = obj.getString("дата_платежа");
                            invoiceNumber[i] = obj.getInt("№_накладной");
                            paymentAmount[i] = obj.getDouble("сумма_внесения");
                            author[i] = obj.getString("автор");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in paymentsServer: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("DateTimeDoc", dateTimeDoc[i]);
                            cv.put("InvoiceNumber", invoiceNumber[i]);
                            cv.put("сумма_внесения", paymentAmount[i]);
                            cv.put("Автор", author[i]);
                            long rowID = db.insert("paymentsServer", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "Платежи загружены", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("agentID", areaDefault);
                parameters.put("tableName", "платежи");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadAgentsFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] areaAgent = new Integer[jsonArray.length()];
                    String[] secondName = new String[jsonArray.length()];
                    String[] firstName = new String[jsonArray.length()];
                    String [] middleName = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            areaAgent[i] = obj.getInt("Район");
                            secondName[i] = obj.getString("Фамилия");
                            firstName[i] = obj.getString("Имя");
                            middleName[i] = obj.getString("Отчество");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in agents: ---");
                            cv.put("area", areaAgent[i]);
                            cv.put("secondName", secondName[i]);
                            cv.put("firstName", firstName[i]);
                            cv.put("middleName", middleName[i]);
                            long rowID = db.insert("agents", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "Агенты загружены", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("agentID", areaDefault);
                parameters.put("tableName", "agents");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);

    }

    private void updateLocalDB(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Обновление локальной базы данных")
                .setMessage("Все таблицы будут перезаписаны!")
                .setCancelable(true)
                .setPositiveButton("Назад",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mainMenu();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Обновить",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                receiveUpdateFromServerPrompt();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Нет",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
