package com.example.myapplicationtest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Locale;

import jxl.CellType;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class AgentReportActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnOptions;
    String lastReceiveDate = "", dateStart = "", dateEnd = "", receiveDate = "", csvFileFormCopy,
            csvFileForm, areaDefault;
    DBHelper dbHelper;
    final String LOG_TAG = "myLogs";
    SQLiteDatabase db;
    String[] reportList, receiveDateList;
    ArrayMap<String, Double> arrayMapReceive, arrayMapQuantity, arrayMapExchange, arrayMapReturn,
            arrayMapQuantityReduced, arrayMapExchangeReduced, arrayMapReturnReduced;
    EditText editTextDateStart, editTextDateEnd;
    ArrayList<String> receiveDateListTmp, accountingTypeList, accountingTypePaymentsList;
    ArrayList<Integer> invoiceNumberList;
    ArrayList<Double> invoiceSumList, invoiceSumPaymentsList;
    File file;
    SharedPreferences sPrefAreaDefault;
    final String SAVED_AREADEFAULT = "areaDefault";
    Integer invoiceAccTypeOneCount, invoiceAccTypeTwoCount, invoiceAccTypeOnePaymentsCount, invoiceAccTypeTwoPaymentsCount;
    Double invoiceSumTotal, invoiceSumTypeOneTotal, invoiceSumTypeTwoTotal, invoiceSumTypeOnePaymentsTotal, invoiceSumTypeTwoPaymentsTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_report);

        btnOptions = findViewById(R.id.buttonOptions);
        btnOptions.setOnClickListener(this);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        editTextDateEnd = findViewById(R.id.editTextDateEnd);
        editTextDateStart = findViewById(R.id.editTextDateStart);

        sPrefAreaDefault = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");

        onChangeListener();
        getReceiveList();
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
        final String[] choice ={"Выбрать загрузку", "Показать загрузку", "Продажи", "Сформировать", "По умолчанию"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Меню");
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("Выбрать загрузку")){
                    chooseReceive();
                    dialog.cancel();
                }
                if (choice[item].equals("Показать загрузку")){
                    showReceiveReport("Загрузка");
                    dialog.cancel();
                }
                if (choice[item].equals("Посмотреть продажи")){
                    watchSales();
                    dialog.cancel();
                }
                if (choice[item].equals("Сформировать")){
                    executeChoice();
                    dialog.cancel();
                }
                if (choice[item].equals("По умолчанию")){
                    receiveDate = "";
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

    private void getReceiveList(){
        arrayMapReceive = new ArrayMap<>();
        String sql = "SELECT DISTINCT dateTimeDoc FROM receive ORDER BY id DESC LIMIT 1";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int dateTimeDocTmp = c.getColumnIndex("dateTimeDoc");
            lastReceiveDate = c.getString(dateTimeDocTmp);
        }

        sql = "SELECT items.Наименование, receive.quantity FROM receive INNER JOIN items " +
                "ON receive.itemID LIKE items.Артикул " +
                "WHERE receive.dateTimeDoc LIKE ? ";
        if (receiveDate.length() > 0){
            c = db.rawQuery(sql, new String[]{receiveDate});
        } else {
            c = db.rawQuery(sql, new String[]{lastReceiveDate});
        }
        if (c.moveToFirst()) {
            int itemNameTmp = c.getColumnIndex("Наименование");
            int quantityTmp = c.getColumnIndex("quantity");
            ArrayList<String> itemNameList = new ArrayList<>();
            ArrayList<Double> quantityList = new ArrayList<>();
            do {
                itemNameList.add(c.getString(itemNameTmp));
                quantityList.add(c.getDouble(quantityTmp));
            } while (c.moveToNext());
            reportList = new String[itemNameList.size()];
            for (int i = 0; i < itemNameList.size(); i++) {
                arrayMapReceive.put(itemNameList.get(i), quantityList.get(i));
                reportList[i] = "Наименование: " + itemNameList.get(i) + System.lineSeparator() +
                        "Кол-во: " + String.valueOf(quantityList.get(i)) + System.lineSeparator();
            }
        }
        c.close();
    }

    private void showReceiveReport(String reportTitle){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(reportTitle)
                .setCancelable(true)
                .setNeutralButton("Назад",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                mainMenu();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Печатать",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                makeExcel();
                                try {
                                    printReport();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Выйти",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setItems(reportList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void chooseReceive(){
        receiveDateListTmp = new ArrayList<>();
        String receiveDateTmp;
        String receiveMenuTitle;
        if (dateStart.length() > 0 && dateEnd.length() > 0) {
            String sql = "SELECT DISTINCT dateTimeDoc FROM receive WHERE dateTimeDoc BETWEEN ? AND ?";
            Cursor c = db.rawQuery(sql, new String[]{dateStart, dateEnd});
            if (c.moveToFirst()) {
                int dateTimeDocTmp = c.getColumnIndex("dateTimeDoc");
                receiveDateTmp = c.getString(dateTimeDocTmp);
                do {
                    receiveDateListTmp.add(receiveDateTmp);
                } while (c.moveToNext());
                receiveDateList = new String[receiveDateListTmp.size()];
                for (int i = 0; i < receiveDateListTmp.size(); i++){
                    receiveDateList[i] = receiveDateListTmp.get(i);
                }
            }
        }
        if (receiveDateListTmp.size() > 1){
            receiveMenuTitle = "Выберите загрузку:";
        } else {
            receiveMenuTitle = "Загрузка от:";
        }
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(receiveMenuTitle)
                .setCancelable(true)
                .setNeutralButton("Назад",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                mainMenu();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Печатать",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Выйти",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setItems(receiveDateList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        receiveDate = receiveDateList[item];
                        getReceiveList();
                        mainMenu();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void watchSales(){
        Toast.makeText(getApplicationContext(), "Этот пункт в разработке", Toast.LENGTH_SHORT).show();
    }

    private void executeChoice(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
        LocalDateTime d = LocalDateTime.parse(lastReceiveDate, formatter);
        final String output = d.with(LocalTime.MIN).format( formatter );
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();

        ArrayList<String> itemNameListDefault;
        Double quantity;
        Double exchange;
        Double returnQuantity;
        arrayMapQuantity = new ArrayMap<>();
        arrayMapExchange = new ArrayMap<>();
        arrayMapReturn = new ArrayMap<>();
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
            sql = "SELECT items.Наименование, invoice.Quantity, invoice.ExchangeQuantity," +
                    "invoice.ReturnQuantity FROM invoice INNER JOIN items " +
                    "ON invoice.ItemID LIKE items.Артикул " +
                    "WHERE invoice.DateTimeDoc BETWEEN ? AND ?";
            c = db.rawQuery(sql, new String[]{dateStart, dateEnd});

            sql2 = "SELECT DISTINCT InvoiceNumber, AccountingType, InvoiceSum FROM invoice WHERE DateTimeDoc BETWEEN ? AND ?";
            c2 = db.rawQuery(sql2, new String[]{dateStart, dateEnd});

            sql3 = "SELECT DISTINCT invoice.InvoiceNumber, invoice.AccountingType, invoice.InvoiceSum FROM invoice INNER JOIN " +
                    "paymentsServer ON invoice.InvoiceNumber = paymentsServer.InvoiceNumber " +
                    "WHERE invoice.DateTimeDoc BETWEEN ? AND ?";
            c3 = db.rawQuery(sql3, new String[]{dateStart, dateEnd});
        } else {
            sql = "SELECT items.Наименование, invoice.Quantity, invoice.ExchangeQuantity," +
                    "invoice.ReturnQuantity FROM invoice INNER JOIN items " +
                    "ON invoice.ItemID LIKE items.Артикул " +
                    "WHERE invoice.DateTimeDoc > ?";
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
            ArrayList<String> itemNameList = new ArrayList<>();
            ArrayList<Double> quantityReceiveList = new ArrayList<>();
            ArrayList<Double> quantityInvoiceList = new ArrayList<>();
            ArrayList<Double> exchangeQuantityList = new ArrayList<>();
            ArrayList<Double> returnQuantityList = new ArrayList<>();
            do {
                itemNameList.add(c.getString(itemNameTmp));
                quantityInvoiceList.add(c.getDouble(quantityInvoiceTmp));
                exchangeQuantityList.add(c.getDouble(exchangeQuantityTmp));
                returnQuantityList.add(c.getDouble(returnQuantityTmp));

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
                arrayMapQuantity.put(c.getString(itemNameTmp), quantity);
                arrayMapExchange.put(c.getString(itemNameTmp), exchange);
                arrayMapReturn.put(c.getString(itemNameTmp), returnQuantity);
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
            reportList = new String[arrayMapQuantityReduced.size()];
            for (int i = 0; i < arrayMapQuantityReduced.size(); i++) {
                for (int j = 0; j < arrayMapReceive.size(); j++) {
                    if (arrayMapQuantityReduced.keyAt(i).equals(arrayMapReceive.keyAt(j))) {
                        Double tmp = arrayMapReceive.valueAt(j) - arrayMapQuantityReduced.valueAt(i) - arrayMapExchangeReduced.valueAt(i);
                        reportList[i] = "Обмен: " + arrayMapExchangeReduced.valueAt(i).toString() + System.getProperty("line.separator") +
                                "Наименование: " + System.getProperty("line.separator") +
                                arrayMapExchangeReduced.keyAt(i) + System.getProperty("line.separator") +
                                "Остаток: " + roundUp(tmp, 2).toString() + System.getProperty("line.separator") +
                                "Продажа: " + arrayMapQuantityReduced.valueAt(i).toString() + System.getProperty("line.separator") +
                                "Загрузка: " + arrayMapReceive.valueAt(j).toString() + System.getProperty("line.separator") +
                                System.getProperty("line.separator");
                    }
                }
            }
            showReceiveReport("Конец смены");
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

    private void printReport()  throws IOException {
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
        String output = zdt.format( formatter );

        File sd = Environment.getExternalStorageDirectory();
        csvFileFormCopy = "агент_отчет_" + output + ".xls";
        File directorySave = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Агент_отчет");
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

            for (int j = 0; j < sheet.getColumns(); j++){
                for (int i = 0; i < sheet.getRows(); i++){
                    WritableCell cell = sheet.getWritableCell(j, i);
                    CellFormat cfm = cell.getCellFormat();
                    if (j == 2 && i == 2) {
                        if (cell.getType() == CellType.LABEL) {
                            Label l = (Label) cell;
                            l.setString("Дата: " + output); //Дата
                        }
                    }
                    if (j == 0 && i == 4) {
                        if (cell.getType() == CellType.LABEL) {
                            Label l = (Label) cell;
                            l.setString("Район № " + areaDefault); //Район
                        }
                    }
                    if (i > 29 && i < 37) {
                        if (j == 2 && i == 30) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(invoiceNumberList.size())); //Всего накладных
                            }
                        }
                        if (j == 2 && i == 31) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(invoiceAccTypeOneCount)); //Всего проводных
                            }
                        }
                        if (j == 2 && i == 32) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(invoiceAccTypeTwoCount)); //Всего непроводных
                            }
                        }
                        if (j == 2 && i == 33) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(invoiceAccTypeOnePaymentsCount.toString()); //Всего проводных за наличные
                            }
                        }
                        if (j == 2 && i == 34) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(invoiceAccTypeTwoPaymentsCount.toString()); //Всего непроводных за наличные
                            }
                        }
                        if (j == 2 && i == 35) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(invoiceAccTypeTwoCount - invoiceAccTypeTwoPaymentsCount)); //Всего непроводных на реализацию
                            }
                        }

                        if (j == 4 && i == 30) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(invoiceSumTotal)); //Всего накладных сумма
                            }
                        }
                        if (j == 4 && i == 31) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(invoiceSumTypeOneTotal)); //Всего проводных сумма
                            }
                        }
                        if (j == 4 && i == 32) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(invoiceSumTypeTwoTotal)); //Всего непроводных сумма
                            }
                        }
                        if (j == 4 && i == 33) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(invoiceSumTypeOnePaymentsTotal.toString()); //Всего проводных за нал. сумма
                            }
                        }
                        if (j == 4 && i == 34) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(invoiceSumTypeTwoPaymentsTotal.toString()); //Всего непроводных за нал. сумма
                            }
                        }
                        if (j == 4 && i == 35) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(invoiceSumTypeTwoTotal - invoiceSumTypeTwoPaymentsTotal)); //Всего непроводных на реал. сумма
                            }
                        }

                    }

                    if (i > 6 && i < 28 && i < (arrayMapExchangeReduced.size() + 7)) {
//                        for (int a = 0; a < arrayMapQuantityReduced.size(); a++) {
                            for (int b = 0; b < arrayMapReceive.size(); b++) {
                                if (arrayMapQuantityReduced.keyAt(i - 7).equals(arrayMapReceive.keyAt(b))) {
                                    if (j == 0) {
                                        if (cell.getType() == CellType.LABEL) {
                                            Label l = (Label) cell;
                                            l.setString(String.valueOf(arrayMapExchangeReduced.valueAt(i - 7))); //Обмен
                                        }
                                    }
                                    if (j == 1) {
                                        if (cell.getType() == CellType.LABEL) {
                                            Label l = (Label) cell;
                                            l.setString(String.valueOf(arrayMapExchangeReduced.keyAt(i - 7))); //Наименование
                                        }
                                    }
                                    if (j == 2) {
                                        Double tmp = arrayMapReceive.valueAt(b) - arrayMapQuantityReduced.valueAt(i - 7)
                                                - arrayMapExchangeReduced.valueAt(i - 7);
                                        if (cell.getType() == CellType.LABEL) {
                                            Label l = (Label) cell;
                                            l.setString(String.valueOf(roundUp(tmp, 2))); //Остаток
                                        }
                                    }
                                    if (j == 3) {
                                        if (cell.getType() == CellType.LABEL) {
                                            Label l = (Label) cell;
                                            l.setString(String.valueOf(arrayMapQuantityReduced.valueAt(i - 7))); //Продажа
                                        }
                                    }
                                    if (j == 4) {
                                        if (cell.getType() == CellType.LABEL) {
                                            Label l = (Label) cell;
                                            l.setString(String.valueOf(arrayMapReceive.valueAt(b))); //Загрузка
                                        }
                                    }
                                    if (j == 5) {
                                        if (cell.getType() == CellType.LABEL) {
                                            Label l = (Label) cell;
                                            l.setString(String.valueOf(0)); //Сумма
                                        }
                                    }
                                }
//                            }
                        }
                    }
                    cell.setCellFormat(cfm);
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
        csvFileForm = "agentReport.xls";
        File directory = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Агент_отчет_форма");
        file = new File(directory, csvFileForm);

    }
}
