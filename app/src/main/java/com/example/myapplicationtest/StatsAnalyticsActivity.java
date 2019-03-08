package com.example.myapplicationtest;

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
    String dateStart = "", dateEnd = "", csvFileFormCopy, csvFileForm, areaDefault, tempDate;
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
    SharedPreferences sPrefAreaDefault;
    final String SAVED_AREADEFAULT = "areaDefault";
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
        final String[] choice ={"Посмотреть продажи", "Сформировать", "По умолчанию"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Меню");
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("Посмотреть продажи")){

                    dialog.cancel();
                }
                if (choice[item].equals("Сформировать")){
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
            sql = "SELECT items.Наименование, invoice.Quantity, invoice.ExchangeQuantity," +
                    "invoice.ReturnQuantity, invoice.Price, invoice.Total FROM invoice INNER JOIN items " +
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
                    "invoice.ReturnQuantity, invoice.Price, invoice.Total FROM invoice INNER JOIN items " +
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
            reportList = new String[arrayMapQuantityReduced.size()];
            for (int i = 0; i < arrayMapQuantityReduced.size(); i++) {
                Double tmp = arrayMapReceive.valueAt(i) - arrayMapQuantityReduced.valueAt(i) - arrayMapExchangeReduced.valueAt(i);
                reportList[i] = "Обмен: " + arrayMapExchangeReduced.valueAt(i).toString() + System.getProperty("line.separator") +
                        "Наименование: " + System.getProperty("line.separator") +
                        arrayMapExchangeReduced.keyAt(i) + System.getProperty("line.separator") +
                        "Остаток: " + roundUp(tmp, 2).toString() + System.getProperty("line.separator") +
                        "Продажа: " + arrayMapQuantityReduced.valueAt(i).toString() + System.getProperty("line.separator") +
                        "Загрузка: " + arrayMapReceive.valueAt(i).toString() + System.getProperty("line.separator") +
                        System.getProperty("line.separator");
            }
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

            for (int j = 0; j < 6; j++){
                for (int i = 0; i < sheet.getRows(); i++){
                    WritableCell cell = sheet.getWritableCell(j, i);
                    CellFormat cfm = cell.getCellFormat();
                    if (j == 2 && i == 2) {
                        if (cell.getType() == CellType.LABEL) {
                            Label l = (Label) cell;
                            l.setString("Дата: " + output); //Дата
                        }
                    }
//                    Cell readCell = sheet.getCell(j, i);
//                    Label label = new Label(j, i, readCell.getContents());
//                    CellView cell = sheet.getColumnView(x);
//                    cell.setAutosize(true);
//                    sheet.setColumnView(x, cell);
//
//                    if (j == 0){
//                        label = new Label(j, i, String.valueOf(j));
//                    }
//                    sheet.addCell(label);
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
}
