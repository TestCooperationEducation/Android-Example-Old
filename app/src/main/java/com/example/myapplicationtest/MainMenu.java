package com.example.myapplicationtest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoice, btnPayments, btnSalesAgents, btnUpdateLocalDB, btnClearLocalTables,
            btnReports, btnViewInvoices, btnReceive;
    SharedPreferences sPrefArea, sPrefAccountingTypeDocFilter, sPrefDayOfTheWeekDefault, sPrefDBName,
            sPrefFreshStatus, sPrefDBPassword, sPrefDBUser, sPrefDayOfTheWeek, sPrefVisited,
            sPrefConnectionStatus, sPrefAreaDefault, sPrefInvoiceNumberLast, sPrefPaymentNumberLast,
            sPrefChangeInvoiceNumberNotSynced, sPrefChangeInvoiceNotSynced, sPrefAccountingTypeDoc,
            sPrefLastReceiveDate, sPrefAccountingType;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPEDocFilter = "AccountingTypeDocFilter";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DAYOFTHEWEEKDEFAULT = "DayOfTheWeekDefault";
    final String SAVED_DayOfTheWeek = "DayOfTheWeek";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_VISITED = "visited";
    final String SAVED_CONNSTATUS = "connectionStatus";
    final String SAVED_FRESHSTATUS = "freshStatus";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_InvoiceNumberLast = "invoiceNumberLast";
    final String SAVED_PaymentNumberLast = "paymentNumberLast";
    final String SAVED_ChangeInvoiceNotSynced = "changeInvoiceNotSynced";
    final String SAVED_ChangeInvoiceNumberNotSynced = "changeInvoiceNumberNotSynced";
    final String SAVED_AccountingTypeDoc = "accountingTypeDoc";
    final String SAVED_LastReceiveDate = "lastReceiveDate";
    String loginUrl = "https://caiman.ru.com/php/login.php", dbName, dbUser, dbPassword,
            syncUrl = "https://caiman.ru.com/php/syncDB.php", connStatus, areaDefault, invoiceNumberLast,
            paymentNumberLast, lastReceiveDate, csvFileForm, csvFileFormCopy;
    String[] dayOfTheWeek, salesPartnersName, accountingType, author, itemName, comment, dateTimeDoc,
            reportList;
    Integer[] itemPrice, discountID, spID, area, serverDB_ID, itemNumber, discountType, discount,
            invoiceNumber, agentID, salesPartnerID;
    Double[] itemQuantity, totalSum, exchangeQuantity, returnQuantity, invoiceSum, paymentAmount;
    SharedPreferences.Editor e;
    DBHelper dbHelper;
    final String LOG_TAG = "myLogs";
    SQLiteDatabase db;
    Boolean one, two, three, four, five, six, seven, agentReportChoice = false, dropped = false;
    Integer countGlobal;
    ArrayMap<String, Double> arrayMapReceive, arrayMapQuantity, arrayMapExchange, arrayMapReturn,
            arrayMapQuantityReduced, arrayMapExchangeReduced, arrayMapReturnReduced;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        btnInvoice = findViewById(R.id.buttonInvoice);
        btnPayments = findViewById(R.id.buttonPayments);
        btnSalesAgents = findViewById(R.id.buttonSalesPartners);
        btnUpdateLocalDB = findViewById(R.id.buttonUpdateLocalDB);
        btnClearLocalTables = findViewById(R.id.buttonClearLocalDB);
        btnReports = findViewById(R.id.buttonReports);
        btnViewInvoices = findViewById(R.id.buttonViewInvoices);
        btnReceive = findViewById(R.id.buttonReceive);
        btnReceive.setOnClickListener(this);
        btnViewInvoices.setOnClickListener(this);
        btnReports.setOnClickListener(this);
        btnClearLocalTables.setOnClickListener(this);
        btnUpdateLocalDB.setOnClickListener(this);
        btnInvoice.setOnClickListener(this);
        btnPayments.setOnClickListener(this);
        btnSalesAgents.setOnClickListener(this);

        one = false;
        two = false;
        three = false;
        four = false;
        five = false;
        six = false;
        seven = false;

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingTypeDocFilter = getSharedPreferences(SAVED_ACCOUNTINGTYPEDocFilter, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeekDefault = getSharedPreferences(SAVED_DAYOFTHEWEEKDEFAULT, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DayOfTheWeek, Context.MODE_PRIVATE);
        sPrefVisited = getSharedPreferences(SAVED_VISITED, Context.MODE_PRIVATE);
        sPrefFreshStatus = getSharedPreferences(SAVED_FRESHSTATUS, Context.MODE_PRIVATE);
        sPrefAreaDefault = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefInvoiceNumberLast = getSharedPreferences(SAVED_InvoiceNumberLast, Context.MODE_PRIVATE);
        sPrefPaymentNumberLast = getSharedPreferences(SAVED_PaymentNumberLast, Context.MODE_PRIVATE);
        sPrefChangeInvoiceNotSynced = getSharedPreferences(SAVED_ChangeInvoiceNotSynced, Context.MODE_PRIVATE);
        sPrefChangeInvoiceNumberNotSynced = getSharedPreferences(SAVED_ChangeInvoiceNumberNotSynced, Context.MODE_PRIVATE);
        sPrefAccountingTypeDoc = getSharedPreferences(SAVED_AccountingTypeDoc, Context.MODE_PRIVATE);
        sPrefLastReceiveDate = getSharedPreferences(SAVED_LastReceiveDate, Context.MODE_PRIVATE);
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);

        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        invoiceNumberLast = sPrefInvoiceNumberLast.getString(SAVED_InvoiceNumberLast, "");
        paymentNumberLast = sPrefPaymentNumberLast.getString(SAVED_PaymentNumberLast, "");
        lastReceiveDate = sPrefLastReceiveDate.getString(SAVED_LastReceiveDate, "");

        sPrefArea.edit().clear().apply();
        sPrefAccountingTypeDocFilter.edit().clear().apply();
        sPrefAccountingType.edit().clear().apply();
        sPrefDayOfTheWeek.edit().clear().apply();
        sPrefVisited.edit().clear().apply();
        sPrefChangeInvoiceNotSynced.edit().clear().apply();
        sPrefChangeInvoiceNumberNotSynced.edit().clear().apply();
        sPrefAccountingTypeDoc.edit().clear().apply();

        e = sPrefFreshStatus.edit();
        e.putString(SAVED_FRESHSTATUS, "fresh");
        e.apply();

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        arrayMapReceive = new ArrayMap<>();

        String dayOfTheWeekTmp = sPrefDayOfTheWeekDefault.getString(SAVED_DAYOFTHEWEEKDEFAULT, "");
        if (sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "").equals("failed")){
            Toast.makeText(getApplicationContext(), "<<< ?????????????? >>>", Toast.LENGTH_SHORT).show();
        } else {
            loadDateFromServer();
            Toast.makeText(getApplicationContext(), "??????????????: " + dayOfTheWeekTmp, Toast.LENGTH_SHORT).show();
        }

//        db.execSQL("DROP TABLE IF EXISTS invoiceLocalDB");
//        db.execSQL("DROP TABLE IF EXISTS invoice");
//        db.execSQL("DROP TABLE IF EXISTS syncedInvoice");
//        db.execSQL("DROP TABLE IF EXISTS syncedPayments");
//        db.execSQL("DROP TABLE IF EXISTS payments");
//        db.execSQL("DROP TABLE IF EXISTS itemsToInvoiceTmp");
//        db.execSQL("DROP TABLE IF EXISTS receiveLocal");
//        db.execSQL("DROP TABLE IF EXISTS items");
//        dbHelper.onUpgrade(db, 1, 2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoice:
                createInvoice();
                break;
            case R.id.buttonViewInvoices:
                viewInvoices();
                break;
            case R.id.buttonPayments:
                makePayments();
                break;
            case R.id.buttonReceive:
                Intent intent = new Intent(getApplicationContext(), ReceiveActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonSalesPartners:
//                final String[] choice ={};
                AlertDialog.Builder builder;
//                builder = new AlertDialog.Builder(this);
//                builder.setTitle("Choice");
//                builder.setItems(choice, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int item) {
//                        if (choice[item].equals("Choice")){
//                            manageSalesPartners();
//                        }
//                    }
//                });
//                builder.setCancelable(false);
//                AlertDialog alert = builder.create();
//                alert.show();

//                final boolean[] mCheckedItems = new boolean[3];
//                final String[] checkCatsName = { "????????????", "??????????", "????????????" };
//                builder = new AlertDialog.Builder(this);
//                builder.setTitle("???????????????? ??????????")
//                    .setCancelable(true)
//                    .setMultiChoiceItems(checkCatsName, mCheckedItems,
//                            new DialogInterface.OnMultiChoiceClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog,
//                                                    int which, boolean isChecked) {
//                                    mCheckedItems[which] = isChecked;
//                                }
//                            })
//                    .setPositiveButton("????????????",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog,
//                                                    int id) {
//                                    StringBuilder state = new StringBuilder();
//                                    for (int i = 0; i < checkCatsName.length; i++) {
//                                        state.append("" + checkCatsName[i]);
//                                        if (mCheckedItems[i])
//                                            state.append(" ????????????\n");
//                                        else
//                                            state.append(" ???? ????????????\n");
//                                    }
//                                }
//                            })
//                    .setNegativeButton("????????????",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog,
//                                                    int id) {
//                                    dialog.cancel();
//
//                                }
//                            });
//                AlertDialog alert = builder.create();
//                alert.show();
//                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                lp.copyFrom(alert.getWindow().getAttributes());
//                lp.width = 720;
//                lp.height = 1200;
//                lp.x=-170;
//                lp.y=100;
//                alert.getWindow().setAttributes(lp);
                break;
            case R.id.buttonUpdateLocalDB:
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder = new AlertDialog.Builder(this);
                builder.setTitle("???????????????????? ?????????????????? ???????? ????????????")
                        .setMessage("?????? ?????????????? ?????????? ????????????????????????!")
                        .setCancelable(true)
                        .setNegativeButton("????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        updateLocalDB();
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("??????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.buttonClearLocalDB:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("?????????? ????????????????????")
                        .setMessage("?????????????? ?? ???????????? ???????")
                        .setCancelable(true)
                        .setNegativeButton("????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (tableExists(db, "invoiceLocalDB")){
                                            clearTable("invoiceLocalDB");
                                        }
                                        if (tableExists(db, "syncedInvoice")){
                                            clearTable("syncedInvoice");
                                        }
                                        if (tableExists(db, "payments")){
                                            clearTable("payments");
                                        }
                                        if (tableExists(db, "syncedPayments")){
                                            clearTable("syncedPayments");
                                        }
                                        if (tableExists(db, "itemsToInvoiceTmp")){
                                            clearTable("itemsToInvoiceTmp");
                                        }
                                        if (tableExists(db, "receiveLocal")){
                                            clearTable("receiveLocal");
                                        }
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                alert = builder.create();
                alert.show();
                break;
            case R.id.buttonReports:
                testCheck();
                break;
            default:
                break;
        }
    }

    private void createInvoice(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAreaActivity.class);
        startActivity(intent);
    }

    private void viewInvoices(){
        Intent intent = new Intent(getApplicationContext(), ViewInvoicesMenuActivity.class);
        startActivity(intent);
    }

    private void makePayments(){
        Intent intent = new Intent(getApplicationContext(), ViewPaymentsMenuActivity.class);
        startActivity(intent);
    }

    private void manageSalesPartners(){
        Intent intent = new Intent(getApplicationContext(), ManageSalesPartnersActivity.class);
        startActivity(intent);
    }

    private void testCheck(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????")
                .setCancelable(true)
                .setNeutralButton("?????????????????? ??????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                systemReport();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("?????????????????? ??????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                agentReportPrompt();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void agentReportPrompt(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("?????????????????? ??????????")
                .setCancelable(true)
                .setNeutralButton("?????????????????????? ????????????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                agentReportChoice = false;
                                agentReport();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("?????????? ??????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                agentReportChoice = true;
                                agentReport();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("???? ????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(getApplicationContext(), AgentReportActivity.class);
                                startActivity(intent);
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void getReceiveList(){
//        lastReceiveDate = "2019-02-14 07:49:01";
        String sql = "SELECT DISTINCT dateTimeDoc FROM receive ORDER BY id DESC LIMIT 1";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int dateTimeDocTmp = c.getColumnIndex("dateTimeDoc");
            lastReceiveDate = c.getString(dateTimeDocTmp);
        }
        if (agentReportChoice == false) {
            sql = "SELECT items.????????????????????????, receive.quantity FROM receive INNER JOIN items " +
                    "ON receive.itemID LIKE items.?????????????? " +
                    "WHERE receive.dateTimeDoc LIKE ? ";
            c = db.rawQuery(sql, new String[]{lastReceiveDate});
            if (c.moveToFirst()) {
                int itemNameTmp = c.getColumnIndex("????????????????????????");
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
                    reportList[i] = "????????????????????????: " + itemNameList.get(i) + System.getProperty("line.separator") +
                            "??????-????: " + String.valueOf(quantityList.get(i));
                }
                showReceiveReport("????????????????");
            }
        } else {
            sql = "SELECT items.????????????????????????, receive.quantity FROM receive INNER JOIN items " +
                    "ON receive.itemID LIKE items.?????????????? " +
                    "WHERE receive.dateTimeDoc LIKE ? ";
            c = db.rawQuery(sql, new String[]{lastReceiveDate});
            if (c.moveToFirst()) {
                int itemNameTmp = c.getColumnIndex("????????????????????????");
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
                    reportList[i] = "????????????????????????: " + itemNameList.get(i) + System.getProperty("line.separator") +
                            "??????-????: " + String.valueOf(quantityList.get(i));
                }
            }
        }
        c.close();
    }

    private void agentReport(){
        if(agentReportChoice == false){
//            if (resultExistsVariant(db, "receive")) {
                getReceiveList();
//            } else {
//                Toast.makeText(getApplicationContext(), "???????????? ???????????????? ?????????", Toast.LENGTH_SHORT).show();
//            }
        } else {
            getReceiveList();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
            LocalDateTime d = LocalDateTime.parse(lastReceiveDate, formatter);
            final String output = d.with(LocalTime.MIN).format( formatter );
            Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();

//            String output = "2019-03-13 07:00:00";

//            ArrayList<String> itemNameListDefault;
            Double quantity;
            Double exchange;
            Double returnQuantity;
            arrayMapQuantity = new ArrayMap<>();
            arrayMapExchange = new ArrayMap<>();
            arrayMapReturn = new ArrayMap<>();
            arrayMapQuantityReduced = new ArrayMap<>();
            arrayMapExchangeReduced = new ArrayMap<>();
            arrayMapReturnReduced = new ArrayMap<>();

//            String sql = "SELECT items.???????????????????????? FROM items ";
//            Cursor c = db.rawQuery(sql, null);
//            if (c.moveToFirst()) {
//                int itemNameTmp = c.getColumnIndex("????????????????????????");
//                itemNameListDefault = new ArrayList<>();
//                do {
//                    itemNameListDefault.add(c.getString(itemNameTmp));
//                } while (c.moveToNext());
//            }

            String sql = "SELECT items.????????????????????????, invoice.Quantity, invoice.ExchangeQuantity," +
                    "invoice.ReturnQuantity FROM invoice INNER JOIN items " +
                    "ON invoice.ItemID LIKE items.?????????????? " +
                    "WHERE invoice.DateTimeDoc > ?";
            Cursor c = db.rawQuery(sql, new String[]{output});
            if (c.moveToFirst()) {
                int itemNameTmp = c.getColumnIndex("????????????????????????");
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


//                    for (int i = 0; i < itemNameListDefault.size(); i++){
//                        if (itemNameListDefault.get(i).equals(c.getString(itemNameTmp))){
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
//                        }
//                    }
                } while (c.moveToNext());
//                Toast.makeText(getApplicationContext(), String.valueOf(arrayMapQuantity.size()), Toast.LENGTH_SHORT).show();

                for (int i = 0; i < arrayMapQuantity.size(); i++){
                    if (!arrayMapQuantity.keyAt(i).equals("??????-???? 700 ???? ???????????? ???????? 1") &&
                            !arrayMapQuantity.keyAt(i).equals("??????-???? 700 ???? ???????????? ???????? 2") &&
                            !arrayMapQuantity.keyAt(i).equals("??????-???? 500 ???? ???????????? ???????? 1") &&
                            !arrayMapQuantity.keyAt(i).equals("??????-???? 500 ???? ???????????? ???????? 2") &&
                            !arrayMapQuantity.keyAt(i).equals("???????????? ????-???????????????????? 500???? ???????????? ???????? 1") &&
                            !arrayMapQuantity.keyAt(i).equals("???????????? ????-???????????????????? 500???? ???????????? ???????? 2")){
                        if (arrayMapQuantity.keyAt(i).equals("??????-???? ??????????????")) {
                            if (arrayMapQuantityReduced.containsKey("??????-???? ??????????????") &&
                                    arrayMapExchangeReduced.containsKey("??????-???? ??????????????") &&
                                    arrayMapReturnReduced.containsKey("??????-???? ??????????????")) {
                                arrayMapQuantityReduced.put("??????-???? ??????????????", arrayMapQuantityReduced.get("??????-???? ??????????????") + (arrayMapQuantity.valueAt(i)));
                                arrayMapExchangeReduced.put("??????-???? ??????????????", arrayMapExchangeReduced.get("??????-???? ??????????????") + (arrayMapExchange.valueAt(i)));
                                arrayMapReturnReduced.put("??????-???? ??????????????", arrayMapReturnReduced.get("??????-???? ??????????????") + (arrayMapReturn.valueAt(i)));
                            } else {
                                arrayMapQuantityReduced.put("??????-???? ??????????????", (arrayMapQuantity.valueAt(i)));
                                arrayMapExchangeReduced.put("??????-???? ??????????????", (arrayMapExchange.valueAt(i)));
                                arrayMapReturnReduced.put("??????-???? ??????????????", (arrayMapReturn.valueAt(i)));
                            }
                        }

                        if (arrayMapQuantity.keyAt(i).equals("???????????? ????-???????????????????? ??????????????")){
                            if (arrayMapQuantityReduced.containsKey("???????????? ????-???????????????????? ??????????????") &&
                                    arrayMapExchangeReduced.containsKey("???????????? ????-???????????????????? ??????????????") &&
                                    arrayMapReturnReduced.containsKey("???????????? ????-???????????????????? ??????????????")) {
                                arrayMapQuantityReduced.put("???????????? ????-???????????????????? ??????????????", arrayMapQuantityReduced.get("???????????? ????-???????????????????? ??????????????") + (arrayMapQuantity.valueAt(i)));
                                arrayMapExchangeReduced.put("???????????? ????-???????????????????? ??????????????", arrayMapExchangeReduced.get("???????????? ????-???????????????????? ??????????????") + (arrayMapExchange.valueAt(i)));
                                arrayMapReturnReduced.put("???????????? ????-???????????????????? ??????????????", arrayMapReturnReduced.get("???????????? ????-???????????????????? ??????????????") + (arrayMapReturn.valueAt(i)));
                            } else {
                                arrayMapQuantityReduced.put("???????????? ????-???????????????????? ??????????????", (arrayMapQuantity.valueAt(i)));
                                arrayMapExchangeReduced.put("???????????? ????-???????????????????? ??????????????", (arrayMapExchange.valueAt(i)));
                                arrayMapReturnReduced.put("???????????? ????-???????????????????? ??????????????", (arrayMapReturn.valueAt(i)));
                            }
                        }

                        if (!arrayMapQuantity.keyAt(i).equals("??????-???? ??????????????") &&
                                !arrayMapQuantity.keyAt(i).equals("???????????? ????-???????????????????? ??????????????")) {
                            arrayMapQuantityReduced.put(arrayMapQuantity.keyAt(i), arrayMapQuantity.valueAt(i));
                            arrayMapExchangeReduced.put(arrayMapQuantity.keyAt(i), arrayMapExchange.valueAt(i));
                            arrayMapReturnReduced.put(arrayMapQuantity.keyAt(i), arrayMapReturn.valueAt(i));
                        }
                    }
                    if (arrayMapQuantity.keyAt(i).equals("??????-???? 700 ???? ???????????? ???????? 1")) {
                        if (arrayMapQuantityReduced.containsKey("??????-???? ??????????????") &&
                                arrayMapExchangeReduced.containsKey("??????-???? ??????????????") &&
                                arrayMapReturnReduced.containsKey("??????-???? ??????????????")) {
                            arrayMapQuantityReduced.put("??????-???? ??????????????", arrayMapQuantityReduced.get("??????-???? ??????????????") + (arrayMapQuantity.valueAt(i) * 0.7));
                            arrayMapExchangeReduced.put("??????-???? ??????????????", arrayMapExchangeReduced.get("??????-???? ??????????????") + (arrayMapExchange.valueAt(i) * 0.7));
                            arrayMapReturnReduced.put("??????-???? ??????????????", arrayMapReturnReduced.get("??????-???? ??????????????") + (arrayMapReturn.valueAt(i) * 0.7));
                        } else {
                            arrayMapQuantityReduced.put("??????-???? ??????????????", (arrayMapQuantity.valueAt(i) * 0.7));
                            arrayMapExchangeReduced.put("??????-???? ??????????????", (arrayMapExchange.valueAt(i) * 0.7));
                            arrayMapReturnReduced.put("??????-???? ??????????????", (arrayMapReturn.valueAt(i) * 0.7));
                        }
                    }

                    if (arrayMapQuantity.keyAt(i).equals("??????-???? 700 ???? ???????????? ???????? 2")) {
                        if (arrayMapQuantityReduced.containsKey("??????-???? ??????????????") &&
                                arrayMapExchangeReduced.containsKey("??????-???? ??????????????") &&
                                arrayMapReturnReduced.containsKey("??????-???? ??????????????")) {
                            arrayMapQuantityReduced.put("??????-???? ??????????????", arrayMapQuantityReduced.get("??????-???? ??????????????") + (arrayMapQuantity.valueAt(i) * 0.7));
                            arrayMapExchangeReduced.put("??????-???? ??????????????", arrayMapExchangeReduced.get("??????-???? ??????????????") + (arrayMapExchange.valueAt(i) * 0.7));
                            arrayMapReturnReduced.put("??????-???? ??????????????", arrayMapReturnReduced.get("??????-???? ??????????????") + (arrayMapReturn.valueAt(i) * 0.7));
                        }  else {
                            arrayMapQuantityReduced.put("??????-???? ??????????????", (arrayMapQuantity.valueAt(i) * 0.7));
                            arrayMapExchangeReduced.put("??????-???? ??????????????", (arrayMapExchange.valueAt(i) * 0.7));
                            arrayMapReturnReduced.put("??????-???? ??????????????", (arrayMapReturn.valueAt(i) * 0.7));
                        }
                    }
//
                    if (arrayMapQuantity.keyAt(i).equals("??????-???? 500 ???? ???????????? ???????? 1")) {
                        if (arrayMapQuantityReduced.containsKey("??????-???? ??????????????") &&
                                arrayMapExchangeReduced.containsKey("??????-???? ??????????????") &&
                                arrayMapReturnReduced.containsKey("??????-???? ??????????????")) {
                            arrayMapQuantityReduced.put("??????-???? ??????????????", arrayMapQuantityReduced.get("??????-???? ??????????????") + (arrayMapQuantity.valueAt(i) * 0.5));
                            arrayMapExchangeReduced.put("??????-???? ??????????????", arrayMapExchangeReduced.get("??????-???? ??????????????") + (arrayMapExchange.valueAt(i) * 0.5));
                            arrayMapReturnReduced.put("??????-???? ??????????????", arrayMapReturnReduced.get("??????-???? ??????????????") + (arrayMapReturn.valueAt(i) * 0.5));
                        } else {
                            arrayMapQuantityReduced.put("??????-???? ??????????????", (arrayMapQuantity.valueAt(i) * 0.5));
                            arrayMapExchangeReduced.put("??????-???? ??????????????", (arrayMapExchange.valueAt(i) * 0.5));
                            arrayMapReturnReduced.put("??????-???? ??????????????", (arrayMapReturn.valueAt(i) * 0.5));
                        }
                    }
//
                    if (arrayMapQuantity.keyAt(i).equals("??????-???? 500 ???? ???????????? ???????? 2")) {
                        if (arrayMapQuantityReduced.containsKey("??????-???? ??????????????") &&
                                arrayMapExchangeReduced.containsKey("??????-???? ??????????????") &&
                                arrayMapReturnReduced.containsKey("??????-???? ??????????????")) {
                            arrayMapQuantityReduced.put("??????-???? ??????????????", arrayMapQuantityReduced.get("??????-???? ??????????????") + (arrayMapQuantity.valueAt(i) * 0.5));
                            arrayMapExchangeReduced.put("??????-???? ??????????????", arrayMapExchangeReduced.get("??????-???? ??????????????") + (arrayMapExchange.valueAt(i) * 0.5));
                            arrayMapReturnReduced.put("??????-???? ??????????????", arrayMapReturnReduced.get("??????-???? ??????????????") + (arrayMapReturn.valueAt(i) * 0.5));
                        } else {
                            arrayMapQuantityReduced.put("??????-???? ??????????????", (arrayMapQuantity.valueAt(i) * 0.5));
                            arrayMapExchangeReduced.put("??????-???? ??????????????", (arrayMapExchange.valueAt(i) * 0.5));
                            arrayMapReturnReduced.put("??????-???? ??????????????", (arrayMapReturn.valueAt(i) * 0.5));
                        }
                    }
//
                    if (arrayMapQuantity.keyAt(i).equals("???????????? ????-???????????????????? 500???? ???????????? ???????? 1")) {
                        if (arrayMapQuantityReduced.containsKey("???????????? ????-???????????????????? ??????????????") &&
                                arrayMapExchangeReduced.containsKey("???????????? ????-???????????????????? ??????????????") &&
                                arrayMapReturnReduced.containsKey("???????????? ????-???????????????????? ??????????????")) {
                            arrayMapQuantityReduced.put("???????????? ????-???????????????????? ??????????????", arrayMapQuantityReduced.get("???????????? ????-???????????????????? ??????????????") + (arrayMapQuantity.valueAt(i) * 0.5));
                            arrayMapExchangeReduced.put("???????????? ????-???????????????????? ??????????????", arrayMapExchangeReduced.get("???????????? ????-???????????????????? ??????????????") + (arrayMapExchange.valueAt(i) * 0.5));
                            arrayMapReturnReduced.put("???????????? ????-???????????????????? ??????????????", arrayMapReturnReduced.get("???????????? ????-???????????????????? ??????????????") + (arrayMapReturn.valueAt(i) * 0.5));
                        } else {
                            arrayMapQuantityReduced.put("???????????? ????-???????????????????? ??????????????", (arrayMapQuantity.valueAt(i) * 0.5));
                            arrayMapExchangeReduced.put("???????????? ????-???????????????????? ??????????????", (arrayMapExchange.valueAt(i) * 0.5));
                            arrayMapReturnReduced.put("???????????? ????-???????????????????? ??????????????", (arrayMapReturn.valueAt(i) * 0.5));
                        }
                    }

                    if (arrayMapQuantity.keyAt(i).equals("???????????? ????-???????????????????? 500???? ???????????? ???????? 2")) {
                        if (arrayMapQuantityReduced.containsKey("???????????? ????-???????????????????? ??????????????") &&
                                arrayMapExchangeReduced.containsKey("???????????? ????-???????????????????? ??????????????") &&
                                arrayMapReturnReduced.containsKey("???????????? ????-???????????????????? ??????????????")) {
                            arrayMapQuantityReduced.put("???????????? ????-???????????????????? ??????????????", arrayMapQuantityReduced.get("???????????? ????-???????????????????? ??????????????") + (arrayMapQuantity.valueAt(i) * 0.5));
                            arrayMapExchangeReduced.put("???????????? ????-???????????????????? ??????????????", arrayMapExchangeReduced.get("???????????? ????-???????????????????? ??????????????") + (arrayMapExchange.valueAt(i) * 0.5));
                            arrayMapReturnReduced.put("???????????? ????-???????????????????? ??????????????", arrayMapReturnReduced.get("???????????? ????-???????????????????? ??????????????") + (arrayMapReturn.valueAt(i) * 0.5));
                        } else {
                            arrayMapQuantityReduced.put("???????????? ????-???????????????????? ??????????????", (arrayMapQuantity.valueAt(i) * 0.5));
                            arrayMapExchangeReduced.put("???????????? ????-???????????????????? ??????????????", (arrayMapExchange.valueAt(i) * 0.5));
                            arrayMapReturnReduced.put("???????????? ????-???????????????????? ??????????????", (arrayMapReturn.valueAt(i) * 0.5));
                        }
                    }
                }
                Toast.makeText(getApplicationContext(), String.valueOf(arrayMapQuantityReduced.get("??????-???? ???????????????????????? 250")), Toast.LENGTH_SHORT).show();
//                Toast.makeText(getApplicationContext(), String.valueOf(arrayMapQuantityReduced.get(i)), Toast.LENGTH_SHORT).show();
                reportList = new String[arrayMapQuantityReduced.size()];
                for (int i = 0; i < arrayMapQuantityReduced.size(); i++) {
                    for (int j = 0; j < arrayMapReceive.size(); j++){
                        if (arrayMapQuantityReduced.keyAt(i).equals(arrayMapReceive.keyAt(j))) {
                            Double tmp = arrayMapReceive.valueAt(j) - arrayMapQuantityReduced.valueAt(i) - arrayMapExchangeReduced.valueAt(i);
                            reportList[i] = "??????????: " + arrayMapExchangeReduced.valueAt(i).toString() + System.getProperty("line.separator") +
                                    "????????????????????????: " + System.getProperty("line.separator") +
                                    arrayMapExchangeReduced.keyAt(i) + System.getProperty("line.separator") +
                                    "??????????????: " + roundUp(tmp, 2).toString() + System.getProperty("line.separator") +
                                    "??????????????: " + arrayMapQuantityReduced.valueAt(i).toString() + System.getProperty("line.separator") +
                                    "????????????????: " + arrayMapReceive.valueAt(j).toString() + System.getProperty("line.separator") +
                                    System.getProperty("line.separator");
                        }
                    }
                }
                showReceiveReport("?????????? ??????????");
            }
        }
    }

    private void showReceiveReport(final String reportTitle){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(reportTitle)
                .setCancelable(true)
                .setNeutralButton("??????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                agentReportPrompt();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("????????????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (reportTitle.equals("?????????? ??????????"));
                                makeExcel();
                                try {
                                    printReport();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("??????????",
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
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(alert.getWindow().getAttributes());
//        lp.width = 720;
//        lp.height = 500;
//        lp.x=-170;
//        lp.y=100;
        alert.getWindow().setAttributes(lp);
    }

    private void systemReport(){
        String tmp = "No local invoice table";
        if (tableExists(db, "invoiceLocalDB")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM invoiceLocalDB ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String tmp2 = "No tmp list of items table";
        if (tableExists(db, "itemsToInvoiceTmp")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM itemsToInvoiceTmp ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp2 = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String tmp3 = "No synced table";
        if (tableExists(db, "syncedInvoice")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM syncedInvoice ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp3 = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String tmp4 = "No payments table";
        if (tableExists(db, "payments")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM payments ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp4 = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String tmp5 = "No syncedPayments table";
        if (tableExists(db, "syncedPayments")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM syncedPayments ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp5 = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String tmp6 = "No receiveLocal table";
        if (tableExists(db, "receiveLocal")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM receiveLocal ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp6 = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String sql = "SELECT invoiceNumber FROM syncedInvoice ";
        Cursor c = db.rawQuery(sql, null);
        String tmpIN = "";
        if (c.moveToFirst()) {
            int iNumber = c.getColumnIndex("invoiceNumber");
            do {
                tmpIN = tmpIN + "---" + c.getString(iNumber) ;
            } while (c.moveToNext());
        } else {
            tmpIN = "No";
        }
        c.close();

        Toast.makeText(getApplicationContext(), tmp + "-----" + tmp2 + "-----" + tmp3 + "-----"
                + tmp4 + "-----" + tmp5 + "-----" + tmp6 + "-----" + tmpIN, Toast.LENGTH_SHORT).show();
    }

    private void updateLocalDB(){
        final boolean[] listChecked = new boolean[8];
        final String[] listCheckTableName = { "??????????????????????", "????????????????????????", "???????????????????????????? ????????????",
                "?????? ????????????", "??????????????????", "??????????????", "????????????????", "????????????" };
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("???????????????? ??????????????(????) ?????? ????????????????????")
                .setCancelable(false)
                .setMultiChoiceItems(listCheckTableName, listChecked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                listChecked[which] = isChecked;
                            }
                        })
                .setPositiveButton("????????????????",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
//                                StringBuilder state = new StringBuilder();
                                loadDateFromServer();
                                for (int i = 0; i < listCheckTableName.length; i++) {
//                                    state.append("" + listCheckTableName[i]);
////                                    if (listChecked[i])
////                                        state.append(" ????????????\n");
////                                    else
////                                        state.append(" ???? ????????????\n");
                                    if (listChecked[i] == true){
                                        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
                                        if (sPrefConnectionStatus.contains(SAVED_CONNSTATUS)){
                                            if (sPrefFreshStatus.getString(SAVED_FRESHSTATUS, "").equals("fresh")){
                                                connStatus = sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "");
                                                if (!connStatus.equals("failed")){
                                                    if (listCheckTableName[i].equals("??????????????????????")){
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
                                                    if (listCheckTableName[i].equals("????????????????????????")){
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
                                                    if (listCheckTableName[i].equals("???????????????????????????? ????????????")){
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
                                                    if (listCheckTableName[i].equals("?????? ????????????")){
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
                                                    if (listCheckTableName[i].equals("??????????????????")){
                                                        db.execSQL("DROP TABLE IF EXISTS invoice");
                                                        dbHelper.onUpgrade(db, 1, 2);
                                                        Runnable runnable = new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                loadInvoicesFromServerDB();
                                                            }
                                                        };
                                                        Thread thread5 = new Thread(runnable);
                                                        thread5.start();
                                                    }
                                                    if (listCheckTableName[i].equals("??????????????")){
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
                                                    if (listCheckTableName[i].equals("????????????????")){
                                                        db.execSQL("DROP TABLE IF EXISTS receive");
                                                        dbHelper.onUpgrade(db, 1, 2);
                                                        Runnable runnable = new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                loadReceivesFromServerDB();
                                                            }
                                                        };
                                                        Thread thread7 = new Thread(runnable);
                                                        thread7.start();
                                                    }
                                                    if (listCheckTableName[i].equals("????????????")){
                                                        db.execSQL("DROP TABLE IF EXISTS agents");
                                                        dbHelper.onUpgrade(db, 1, 2);
                                                        Runnable runnable = new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                loadAgentsFromServerDB();
                                                            }
                                                        };
                                                        Thread thread8 = new Thread(runnable);
                                                        thread8.start();
                                                    }
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "<<< ?????? ?????????????????????? ?? ?????????????? >>>" + connStatus, Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), "<<< ?????????????????? ???????? >>>" + connStatus, Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "<<< ???????????????????? ???????????????? ???????????? ?????? ???????????? ?????????? >>>" + connStatus, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        })
                .setNegativeButton("?? ??????????????????(??)",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();

                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(alert.getWindow().getAttributes());
//        lp.width = 720;
//        lp.height = 500;
//        lp.x=-170;
//        lp.y=100;
//        alert.getWindow().setAttributes(lp);
    }

    private void loadDateFromServer(){
        StringRequest request = new StringRequest(Request.Method.POST,
                loginUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    String[] dayOfTheWeek = new String[jsonArray.length()];
                    if (jsonArray.length() == 1){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("dayOfTheWeek");
                        }
                        e = sPrefDayOfTheWeekDefault.edit();
                        e.putString(SAVED_DAYOFTHEWEEKDEFAULT, dayOfTheWeek[0]);
                        e.apply();
//                        Toast.makeText(getApplicationContext(), "???????? ????????????????...", Toast.LENGTH_SHORT).show();
                        one = true;
                    }else{
                        Toast.makeText(getApplicationContext(), "???????????? ??????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???????????????? ?? ???????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
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
                            salesPartnersName[i] = obj.getString("????????????????????????");
                            area[i] = obj.getInt("??????????");
                            accountingType[i] = obj.getString("????????");
                            author[i] = obj.getString("??????????");
                            serverDB_ID[i] = obj.getInt("ID");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in salesPartners: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("????????????????????????", salesPartnersName[i]);
                            cv.put("??????????", area[i]);
                            cv.put("????????", accountingType[i]);
                            cv.put("DayOfTheWeek", dayOfTheWeek[i]);
                            cv.put("??????????", author[i]);
                            long rowID = db.insert("salesPartners", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "?????????????????????? ??????????????????", Toast.LENGTH_SHORT).show();
                        two = true;
                    }else{
                        Toast.makeText(getApplicationContext(), "???????????? ????????????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???????????????? ?? ???????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
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
                    Integer[] itemNumberBuhgalter = new Integer[jsonArray.length()];
                    Integer[] filterOrder = new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemNumber[i] = obj.getInt("??????????????");
                            itemNumberBuhgalter[i] = obj.getInt("??????????????_1??");
                            filterOrder[i] = obj.getInt("filterOrder");
                            itemName[i] = obj.getString("????????????????????????");
                            itemDescription[i] = obj.getString("????????????????");
                            itemPrice[i] = obj.getInt("????????");
                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in items: ---");
                            cv.put("??????????????", itemNumber[i]);
                            cv.put("??????????????_1??", itemNumberBuhgalter[i]);
                            cv.put("filterOrder", filterOrder[i]);
                            cv.put("????????????????????????", itemName[i]);
                            cv.put("????????????????", itemDescription[i]);
                            cv.put("????????", itemPrice[i]);
                            long rowID = db.insert("items", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "???????????????????????? ??????????????????", Toast.LENGTH_SHORT).show();
                        three = true;
                        loadMessage();
                    }else{
                        Toast.makeText(getApplicationContext(), "???????????? ????????????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???????????????? ?? ???????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
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
                            itemNumber[i] = obj.getInt("??????????????");
                            discountID[i] = obj.getInt("ID_????????????");
                            spID[i] = obj.getInt("ID_??????????????????????");
                            author[i] = obj.getString("??????????");

//                            if (!resultExists(db, "itemsWithDiscount","??????????", "??????????", "admin")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in itemsWithDiscount: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("??????????????", itemNumber[i]);
                                cv.put("ID_????????????", discountID[i]);
                                cv.put("ID_??????????????????????", spID[i]);
                                cv.put("??????????", author[i]);
                                long rowID = db.insert("itemsWithDiscount", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "????????????: MainMenu itemsWithDiscount loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
                        Toast.makeText(getApplicationContext(), "???????????????????????????? ???????????? ??????????????????", Toast.LENGTH_SHORT).show();
                        four = true;
                        loadMessage();
                    }else{
                        Toast.makeText(getApplicationContext(), "???????????? ????????????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???????????????? ?? ???????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "??????????????????????????????????????????");
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
                            discountType[i] = obj.getInt("??????_????????????");
                            discount[i] = obj.getInt("????????????");
                            author[i] = obj.getString("??????????");

//                            if (!resultExists(db, "discount","??????????", "??????????", "admin")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in discount: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("??????_????????????", discountType[i]);
                                cv.put("????????????", discount[i]);
                                cv.put("??????????", author[i]);
                                long rowID = db.insert("discount", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "????????????: MainMenu discount loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
                        Toast.makeText(getApplicationContext(), "???????? ???????????? ??????????????????", Toast.LENGTH_SHORT).show();
                        five = true;
                        loadMessage();
                    }else{
                        Toast.makeText(getApplicationContext(), "???????????? ????????????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???????????????? ?? ???????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "????????????");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadInvoicesFromServerDB(){
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

//                            if (!resultExists(db, "invoice","ReturnQuantity", "ReturnQuantity", "0")){
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
                                long rowID = db.insert("invoice", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "????????????: MainMenu invoice loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
                        Toast.makeText(getApplicationContext(), "?????????????????? ??????????????????", Toast.LENGTH_SHORT).show();
                        if (resultExistsVariantTwo(db, "invoice","InvoiceNumber")){
                            String sql = "SELECT DISTINCT InvoiceNumber FROM invoice ORDER BY id DESC LIMIT 1 ";
                            Cursor c = db.rawQuery(sql, null);
                            if (c.moveToFirst()) {
                                int iNumber = c.getColumnIndex("InvoiceNumber");
                                invoiceNumberLast = c.getString(iNumber);
                                c.moveToNext();
                            } else {
                                invoiceNumberLast = "0";
                            }
                            c.close();
                        } else {
                            invoiceNumberLast = "0";
                        }
                        Toast.makeText(getApplicationContext(), invoiceNumberLast, Toast.LENGTH_SHORT).show();
                        e = sPrefInvoiceNumberLast.edit();
                        e.putString(SAVED_InvoiceNumberLast, invoiceNumberLast);
                        e.apply();

                        six = true;
                        loadMessage();
                    }else{
                        e = sPrefInvoiceNumberLast.edit();
                        e.putString(SAVED_InvoiceNumberLast, "0");
                        e.apply();
                        Toast.makeText(getApplicationContext(), "???????????? ??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???????????????? ?? ???????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
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
                            dateTimeDoc[i] = obj.getString("????????_??????????????");
                            invoiceNumber[i] = obj.getInt("???_??????????????????");
                            paymentAmount[i] = obj.getDouble("??????????_????????????????");
                            author[i] = obj.getString("??????????");

//                            if (!resultExists(db, "paymentsServer","??????????", "??????????", "???????????????????????????? ?????? ??????????????????")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in paymentsServer: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("DateTimeDoc", dateTimeDoc[i]);
                                cv.put("InvoiceNumber", invoiceNumber[i]);
                                cv.put("??????????_????????????????", paymentAmount[i]);
                                cv.put("??????????", author[i]);
                                long rowID = db.insert("paymentsServer", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "????????????: MainMenu ?????????????? loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
                        Toast.makeText(getApplicationContext(), "?????????????? ??????????????????", Toast.LENGTH_SHORT).show();

                        if (resultExistsVariantTwo(db, "paymentsServer","serverDB_ID")){
                            String sql = "SELECT DISTINCT serverDB_ID FROM paymentsServer ORDER BY id DESC LIMIT 1 ";
                            Cursor c = db.rawQuery(sql, null);
                            if (c.moveToFirst()) {
                                int iNumber = c.getColumnIndex("serverDB_ID");
                                paymentNumberLast = c.getString(iNumber);
                                c.moveToNext();
                            } else {
                                paymentNumberLast = "0";
                            }
                            c.close();
                        } else {
                            paymentNumberLast = "0";
                        }
                        e = sPrefPaymentNumberLast.edit();
                        e.putString(SAVED_PaymentNumberLast, paymentNumberLast);
                        e.apply();

                        seven = true;
                        loadMessage();
                    }else{
                        e = sPrefPaymentNumberLast.edit();
                        e.putString(SAVED_PaymentNumberLast, "0");
                        e.apply();
                        Toast.makeText(getApplicationContext(), "???????????? ????????????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???????????????? ?? ???????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
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
                parameters.put("tableName", "??????????????");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadReceivesFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] itemID = new Integer[jsonArray.length()];
                    String[] dateTimeDoc = new String[jsonArray.length()];
                    Double[] quantity = new Double[jsonArray.length()];
                    Integer[] agentID = new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemID[i] = obj.getInt("itemID");
                            dateTimeDoc[i] = obj.getString("dateTimeDoc");
                            quantity[i] = obj.getDouble("quantity");
                            agentID[i] = obj.getInt("agentID");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in receive: ---");
                            cv.put("itemID", itemID[i]);
                            cv.put("dateTimeDoc", dateTimeDoc[i]);
                            cv.put("quantity", quantity[i]);
                            cv.put("agentID", agentID[i]);
                            long rowID = db.insert("receive", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "???????????????? ??????????????????", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "???????????? ????????????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???????????????? ?? ???????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
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
                parameters.put("tableName", "receive");
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
                            areaAgent[i] = obj.getInt("??????????");
                            secondName[i] = obj.getString("??????????????");
                            firstName[i] = obj.getString("??????");
                            middleName[i] = obj.getString("????????????????");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in agents: ---");
                            cv.put("area", areaAgent[i]);
                            cv.put("secondName", secondName[i]);
                            cv.put("firstName", firstName[i]);
                            cv.put("middleName", middleName[i]);
                            long rowID = db.insert("agents", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "???????????? ??????????????????", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "???????????? ????????????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "???????????????? ?? ???????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
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

//    private void syncDB(){
//        ContentValues cv = new ContentValues();
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        Log.d(LOG_TAG, "--- Insert in salespartners: ---");
//        // ???????????????????? ???????????? ?????? ?????????????? ?? ???????? ??????: ???????????????????????? ?????????????? - ????????????????
//        for (int i = 0; i < serverDB_ID.length; i++){
//            cv.put("serverDB_ID", serverDB_ID[i]);
//            cv.put("????????????????????????", salesPartnersName[i]);
//            cv.put("??????????", area[i]);
//            cv.put("????????", accountingType[i]);
//            cv.put("DayOfTheWeek", dayOfTheWeek[i]);
//            cv.put("??????????", author[i]);
//            // ?????????????????? ???????????? ?? ???????????????? ???? ID
//            long rowID = db.insert("salespartners", null, cv);
//            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//        }
//    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // ?????????????????????? ??????????????????????
            super(context, "myLocalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");

            if (!tableExists(db, "salesPartners")) {
                db.execSQL("create table salesPartners ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                        + "???????????????????????? text,"
                        + "?????????? integer,"
                        + "???????? text,"
                        + "DayOfTheWeek text,"
                        + "?????????? text" + ");");
            }

            if (!tableExists(db, "items")) {
                db.execSQL("create table items ("
                        + "id integer primary key autoincrement,"
                        + "?????????????? integer UNIQUE ON CONFLICT REPLACE,"
                        + "??????????????_1?? integer,"
                        + "filterOrder integer,"
                        + "???????????????????????? text,"
                        + "???????????????? text,"
                        + "???????? integer" + ");");
            }

            if (!tableExists(db, "itemsWithDiscount")) {
                db.execSQL("create table itemsWithDiscount ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                        + "?????????????? integer,"
                        + "ID_???????????? integer,"
                        + "ID_?????????????????????? integer,"
                        + "?????????? text" + ");");
            }

            if (!tableExists(db, "discount")) {
                db.execSQL("create table discount ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                        + "??????_???????????? integer,"
                        + "???????????? integer,"
                        + "?????????? ??????????" + ");");
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
                        + "Surplus real,"
                        + "Comment text" + ");");
            }

            if (!tableExists(db, "paymentsServer")) {
                db.execSQL("create table paymentsServer ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer UNIQUE,"
                        + "DateTimeDoc text,"
                        + "InvoiceNumber integer,"
                        + "??????????_???????????????? real,"
                        + "?????????? text" + ");");
            }

            if (!tableExists(db, "payments")) {
                db.execSQL("create table payments ("
                        + "id integer primary key autoincrement,"
                        + "DateTimeDoc text,"
                        + "InvoiceNumber integer,"
                        + "??????????_???????????????? real,"
                        + "?????????? text" + ");");
            }

            if (!tableExists(db, "itemsToInvoiceTmp")) {
                db.execSQL("create table itemsToInvoiceTmp ("
                        + "id integer primary key autoincrement,"
                        + "???????????????????? text,"
                        + "???????????????????????? text UNIQUE ON CONFLICT REPLACE,"
                        + "???????? integer,"
                        + "???????????????????????????? integer,"
                        + "???????????????????? real,"
                        + "?????????? real,"
                        + "?????????????? real,"
                        + "?????????????? real,"
                        + "?????????? real" + ");");
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
                        + "surplus real,"
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

    boolean resultExistsVariant(SQLiteDatabase db, String tableName){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM invoiceLocalDB";
        Cursor cursor = db.rawQuery(sql, null);
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        countGlobal = count;
        return count > 0;
    }

    boolean resultExistsVariantTwo(SQLiteDatabase db, String tableName, String selectField){
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

    boolean resultExists(SQLiteDatabase db, String tableName, String selectField, String fieldName, String fieldValue){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT ? FROM " + tableName + " WHERE ? LIKE ? LIMIT 1";
        Cursor cursor = db.rawQuery(sql, new String[]{selectField, fieldName, fieldValue});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    private void loadMessage(){
        if (one && two && three && four && five && six && seven){
            Toast.makeText(getApplicationContext(), "<<< ???????????? ?????????????????? >>>", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearTable(String tableName){
        Log.d(LOG_TAG, "--- Clear " + tableName + " : ---");
        // ?????????????? ?????? ????????????
        int clearCount = db.delete(tableName, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
        Toast.makeText(getApplicationContext(), "<<< ?????????????? ?????????????? >>>", Toast.LENGTH_SHORT).show();
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
        csvFileFormCopy = "??????????_??????????_" + output + ".xls";
        File directorySave = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "??????????_??????????");
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
                            l.setString("????????: " + output); //????????
                        }
                    }
                    if (j == 0 && i == 4) {
                        if (cell.getType() == CellType.LABEL) {
                            Label l = (Label) cell;
                            l.setString("?????????? ??? " + areaDefault); //????????
                        }
                    }
                    if (i > 6 && i < 28 && i < (arrayMapExchangeReduced.size() + 7)) {
                        if (j == 0) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(arrayMapExchangeReduced.valueAt(i - 7))); //??????????
                            }
                        }
                        if (j == 1) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(arrayMapExchangeReduced.keyAt(i - 7))); //????????????????????????
                            }
                        }
                        if (j == 2) {
                            Double tmp = arrayMapReceive.valueAt(i - 7) - arrayMapQuantityReduced.valueAt(i - 7)
                                    - arrayMapExchangeReduced.valueAt(i - 7);
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(roundUp(tmp, 2))); //??????????????
                            }
                        }
                        if (j == 3) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(arrayMapQuantityReduced.valueAt(i - 7))); //??????????????
                            }
                        }
                        if (j == 4) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(arrayMapReceive.valueAt(i - 7))); //????????????????
                            }
                        }
                        if (j == 5) {
                            if (cell.getType() == CellType.LABEL) {
                                Label l = (Label) cell;
                                l.setString(String.valueOf(0)); //??????????
                            }
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
                + File.separator + "Excel" + File.separator + "??????????_??????????_??????????");
        file = new File(directory, csvFileForm);

    }
}
