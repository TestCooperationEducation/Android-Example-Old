package com.example.myapplicationtest;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import java.lang.reflect.Array;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleToLongFunction;

import jxl.Cell;
import jxl.CellType;
import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class AccountingActivity extends AppCompatActivity implements View.OnClickListener {

    String csvFile, folder_name, csvFileCopy, csvFileCopyTwo;
    Button btnOptions, btnExecute;
    TextView textViewAgentName, textViewAccountingType;
    EditText editTextDateStart, editTextDateEnd;
    String chosenArea = "??????????", chosenRoot = "??????????", chosenAccountingType = "????????????", dbName, dbUser,
            dbPassword, syncUrl = "https://caiman.ru.com/php/syncDB.php", areaDefault = "accountant",
            agentChosen = "??????????", dateStart = "", dateEnd = "", email = "accountant@caiman.ru.com",
            output;
    DBHelper dbHelper;
    SQLiteDatabase db;
    final String LOG_TAG = "myLogs";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser;
    ArrayList<Integer> agentAreaList, salesPartnerIDList, invoiceNumberListTmp, agentIDListTmp,
            itemIDListTmp, salesPartnerIDListTmp, itemIDBuhgalterListTmp;
    ArrayList<Double> quantityListTmp, priceListTmp, totalListTmp, invoiceSumListTmp;
    ArrayList<String> secondNameList, firstNameList, middleNameList, salesPartnerNameList,
            dateTimeDocListTmp, salesPartnerNameListTmp, itemsNameListTmp, spTINListTmp;
    String[] agentsList, salesPartnersList;
    boolean[] mCheckedItems;
    private String inputFile;
    Integer g = 1;
    boolean mChecked = false;
    ArrayList<Integer> invoiceNumberListTmpTypeOneReduced;
    ArrayList<Integer> invoiceNumberListTmpTypeTwoReduced;
    ArrayList<Integer> agentIDListTmpTypeOneReduced;
    ArrayList<Integer> agentIDListTmpTypeTwoReduced;
    ArrayList<String> salesPartnerNameListTmpTypeOneReduced;
    ArrayList<String> salesPartnerNameListTmpTypeTwoReduced;
    ArrayList<String> spTINListTmpTypeOneReduced;
    ArrayList<String> spTINListTmpTypeTwoReduced;
    ArrayList<String> itemNameListTmpTypeOneReduced;
    ArrayList<String> itemNameListTmpTypeTwoReduced;
    ArrayList<Integer> itemIDBuhgalterListTmpTypeOneReduced;
    ArrayList<Integer> itemIDBuhgalterListTmpTypeTwoReduced;
    ArrayList<Double> priceListTmpTypeOneReduced;
    ArrayList<Double> priceListTmpTypeTwoReduced;
    ArrayList<Double> quantityListTmpTypeOneReduced;
    ArrayList<Double> quantityListTmpTypeTwoReduced;
    ArrayList<Double> totalListTmpTypeOneReduced;
    ArrayList<Double> totalListTmpTypeTwoReduced;
    ArrayList<Double> invoiceSumListTmpTypeOneReduced;
    ArrayList<Double> invoiceSumListTmpTypeTwoReduced;
    ArrayList<String> dateTimeDocListTmpTypeOneReduced;
    ArrayList<String> dateTimeDocListTmpTypeTwoReduced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Instant instant = Instant.now();
            ZoneId zoneId = ZoneId.of("Asia/Sakhalin");
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
            DateTimeFormatter formatter = null;
            formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            output = zdt.format(formatter);
        }

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");

        folder_name = File.separator + "Download" + File.separator + "Excel";
        btnOptions = findViewById(R.id.buttonOptions);
        btnOptions.setOnClickListener(this);
        btnExecute = findViewById(R.id.buttonExecute);
        btnExecute.setOnClickListener(this);
        textViewAgentName = findViewById(R.id.textViewAgent);
        textViewAccountingType = findViewById(R.id.textViewAccountingType);
        editTextDateStart = findViewById(R.id.editTextDateStart);
        editTextDateEnd = findViewById(R.id.editTextDateEnd);
        textViewAgentName.setText("?????????? ??????????");
        textViewAccountingType.setText("?????????????????? ??????????????????");

        setInitialValues();
        onChangeListener();
        email = "deadprofessor@hpeprint.com";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonOptions:
                mainMenu();
                break;
            case R.id.buttonExecute:
                makeExcel();
                try {
                    read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void mainMenu(){
        final String[] choice ={"??????????????????", "??????????????????????", "????????????", "????????", "???????????????? ??????????????????",
                "??????????????????", "???????????????? ????", "???????????????? ??????????????????"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("????????");
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("??????????????????")){
                    settingsMenu();
                    dialog.cancel();
                }
                if (choice[item].equals("??????????????????????")){
                    salesPartnerChoiceMenu();
                    dialog.cancel();
                }
                if (choice[item].equals("????????????")){
                    agentChoice();
                    dialog.cancel();
                }
                if (choice[item].equals("????????")){
                    accountingTypeChoice();
                    dialog.cancel();
                }
                if (choice[item].equals("???????????????? ??????????????????")){
                    showChosen();
                    dialog.cancel();
                }
                if (choice[item].equals("??????????????????")){
                    executeChoice();
                    dialog.cancel();
                }
                if (choice[item].equals("???????????????? ????")){
                    updateLocalDB();
                    dialog.cancel();
                }

                if (choice[item].equals("???????????????? ??????????????????")){
                    dropAggregate();
                    dialog.cancel();
                }
            }
        });
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(alert.getWindow().getAttributes());
//        lp.width = 500;
//        lp.height = 1100;
//        lp.x=-300;
//        lp.y=-200;
//        alert.getWindow().setAttributes(lp);
    }

    private void setInitialValues(){
        if (tableExists(db, "agents")) {
            String sql = "SELECT area, secondName, firstName, middleName FROM agents";
            Cursor c = db.rawQuery(sql, null);
            if (c.moveToFirst()) {
                int agentAreaTmp = c.getColumnIndex("area");
                int secondNameTmp = c.getColumnIndex("secondName");
                int firstNameTmp = c.getColumnIndex("firstName");
                int middleNameTmp = c.getColumnIndex("middleName");
                agentAreaList = new ArrayList<>();
                secondNameList = new ArrayList<>();
                firstNameList = new ArrayList<>();
                middleNameList = new ArrayList<>();
                do {
                    agentAreaList.add(c.getInt(agentAreaTmp));
                    secondNameList.add(c.getString(secondNameTmp));
                    firstNameList.add(c.getString(firstNameTmp));
                    middleNameList.add(c.getString(middleNameTmp));
                } while (c.moveToNext());
                agentsList = new String[agentAreaList.size()];
                for (int i = 0; i < agentAreaList.size(); i++) {
                    agentsList[i] = secondNameList.get(i) + " " + firstNameList.get(i) + " " + middleNameList.get(i);
                }
            }
            c.close();
        }
    }

    private void settingsMenu(){

    }

    private void salesPartnerChoiceMenu(){
        final String[] choice ={"???????????????? ????????????", "????????????????"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("??????????????????????/????????");
        builder.setPositiveButton("??????????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("???????????????? ????????????")){
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("????????????????")){
                    salesPartnerChoice();
                    dialog.cancel();
                }
            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void salesPartnerChoiceFilter(){
        final String[] choice ={"??????????", "??????????????", "????????"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("??????????????????????/????????????");
        builder.setPositiveButton("??????????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        salesPartnerChoiceMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("??????????")){
                    salesPartnerChoiceArea();
                    dialog.cancel();
                }
                if (choice[item].equals("??????????????")){
                    salesPartnerChoiceRoot();
                    dialog.cancel();
                }
                if (choice[item].equals("????????")){
                    salesPartnerChoiceAccountingType();
                    dialog.cancel();
                }
            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void salesPartnerChoiceArea() {
        final String[] choice = {"?????????? ???1", "?????????? ???2", "?????????? ???3", "?????????? ???4", "?????????? ???5"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("??????????????????????/??????????");
        builder.setPositiveButton("??????????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        salesPartnerChoiceFilter();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("?????????? ???1")) {
                    chosenArea  = "1";
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("?????????? ???2")) {
                    chosenArea = "2";
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("?????????? ???3")) {
                    chosenArea = "3";
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("?????????? ???4")) {
                    chosenArea = "4";
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("?????????? ???5")) {
                    chosenArea = "5";
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void salesPartnerChoiceRoot() {
        final String[] choice = {"??????????????????????-??????????????", "??????????", "??????????????-??????????????", "??????????", "??????????"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("??????????????????????/??????????????");
        builder.setPositiveButton("??????????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        salesPartnerChoiceFilter();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                chosenRoot = choice[item];
                salesPartnerChoiceFilter();
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void salesPartnerChoiceAccountingType() {
        final String[] choice = {"????????????", "????????????????"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("??????????????????????/????????");
        builder.setPositiveButton("??????????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        salesPartnerChoiceFilter();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                chosenAccountingType = choice[item];
                textViewAccountingType.setText(chosenAccountingType);
                salesPartnerChoiceFilter();
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void salesPartnerChoice(){
        if (tableExists(db, "salesPartners")) {
            String sql;
            Cursor c;
            if (chosenRoot.equals("??????????") && !chosenArea.equals("??????????")){
                sql = "SELECT serverDB_ID, ???????????????????????? FROM salesPartners " +
                        "WHERE ?????????? LIKE ? AND ???????? LIKE ? ";
                c = db.rawQuery(sql, new String[]{chosenArea, chosenAccountingType});
            } else {
                sql = "SELECT serverDB_ID, ???????????????????????? FROM salesPartners " +
                        "WHERE DayOfTheWeek LIKE ? AND ???????? LIKE ? ";
                c = db.rawQuery(sql, new String[]{chosenRoot, chosenAccountingType});
            }
            if (!chosenRoot.equals("??????????") && !chosenArea.equals("??????????")){
                sql = "SELECT serverDB_ID, ???????????????????????? FROM salesPartners " +
                        "WHERE ?????????? LIKE ? AND ???????? LIKE ? AND DayOfTheWeek LIKE ?";
                c = db.rawQuery(sql, new String[]{chosenArea, chosenAccountingType, chosenRoot});
            }
            if (chosenRoot.equals("??????????") && chosenArea.equals("??????????")){
                sql = "SELECT serverDB_ID, ???????????????????????? FROM salesPartners " +
                        "WHERE ???????? LIKE ? ";
                c = db.rawQuery(sql, new String[]{chosenAccountingType});
            }

            if (c.moveToFirst()) {
                int salesPartnerIDTmp = c.getColumnIndex("serverDB_ID");
                int salesPartnerNameTmp = c.getColumnIndex("????????????????????????");
                salesPartnerIDList = new ArrayList<>();
                salesPartnerNameList = new ArrayList<>();
                do {
                    salesPartnerIDList.add(c.getInt(salesPartnerIDTmp));
                    salesPartnerNameList.add(c.getString(salesPartnerNameTmp));
                } while (c.moveToNext());
                salesPartnersList = new String[salesPartnerIDList.size()];
                for (int i = 0; i < salesPartnerIDList.size(); i++) {
                    salesPartnersList[i] = salesPartnerIDList.get(i) + " " + salesPartnerNameList.get(i);
                }
            }
        }
        mCheckedItems = new boolean[salesPartnersList.length];
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("???????????????? ????????????????????????")
                .setCancelable(true)
                .setMultiChoiceItems(salesPartnersList, mCheckedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                mCheckedItems[which] = isChecked;
                            }
                        })
                .setPositiveButton("????????????",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                for (int i = 0; i < salesPartnersList.length; i++) {

                                    if (mCheckedItems[i]){
                                        mChecked = true;
                                    }
                                }
                            }
                        })
                .setNegativeButton("????????????",
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

    private void agentChoice(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????/??????????????");
        builder.setItems(agentsList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                agentChosen = agentsList[item];
                chosenArea = agentAreaList.get(item).toString();
                g = agentAreaList.get(item);
                textViewAgentName.setText(agentChosen);
                mainMenu();
                Toast.makeText(getApplicationContext(), g.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void accountingTypeChoice(){
        final String[] choice = {"????????????", "????????????????"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("?????? ??????????");
        builder.setPositiveButton("??????????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                chosenAccountingType = choice[item];
                textViewAccountingType.setText(chosenAccountingType);
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showChosen(){
        final String[] choice = {"???????? ??: " + dateStart, "???????? ????: " + dateEnd, "????????: " + chosenAccountingType,
                "?????????? ???" + chosenArea, "??????????: " + agentChosen, "??????????????: " + chosenRoot};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("???????????????? ??????????????????");
        builder.setPositiveButton("??????????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainMenu();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("???? ??????????????????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        chosenArea = "??????????";
                        chosenAccountingType = "????????????";
                        chosenRoot = "??????????";
                        agentChosen = "??????????";
                        g = 1;
                        mChecked = false;
                        textViewAccountingType.setText("?????????????????? ??????????????????");
                        textViewAgentName.setText("?????????? ??????????");
                        editTextDateStart.setText("");
                        editTextDateEnd.setText("");
                        dateStart = "";
                        dateEnd = "";
                        mainMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
//                chosenAccountingType = choice[item];
//                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void executeChoice(){
        invoiceNumberListTmpTypeOneReduced = new ArrayList<>();
        invoiceNumberListTmpTypeTwoReduced = new ArrayList<>();
        agentIDListTmpTypeOneReduced = new ArrayList<>();
        agentIDListTmpTypeTwoReduced = new ArrayList<>();
        salesPartnerNameListTmpTypeOneReduced = new ArrayList<>();
        salesPartnerNameListTmpTypeTwoReduced = new ArrayList<>();
        spTINListTmpTypeOneReduced = new ArrayList<>();
        spTINListTmpTypeTwoReduced = new ArrayList<>();
        itemNameListTmpTypeOneReduced = new ArrayList<>();
        itemNameListTmpTypeTwoReduced = new ArrayList<>();
        itemIDBuhgalterListTmpTypeOneReduced = new ArrayList<>();
        itemIDBuhgalterListTmpTypeTwoReduced = new ArrayList<>();
        priceListTmpTypeOneReduced = new ArrayList<>();
        priceListTmpTypeTwoReduced = new ArrayList<>();
        quantityListTmpTypeOneReduced = new ArrayList<>();
        quantityListTmpTypeTwoReduced = new ArrayList<>();
        totalListTmpTypeOneReduced = new ArrayList<>();
        totalListTmpTypeTwoReduced = new ArrayList<>();
        invoiceSumListTmpTypeOneReduced = new ArrayList<>();
        invoiceSumListTmpTypeTwoReduced = new ArrayList<>();
        dateTimeDocListTmpTypeOneReduced = new ArrayList<>();
        dateTimeDocListTmpTypeTwoReduced = new ArrayList<>();
        if (tableExists(db, "invoiceAggregate")) {
            Cursor c;
            if (chosenArea.equals("??????????") &&
                    !dateStart.equals("") && !dateEnd.equals("")) {
                if (mChecked){
                    invoiceNumberListTmp = new ArrayList<>();
                    agentIDListTmp = new ArrayList<>();
                    salesPartnerIDListTmp = new ArrayList<>();
                    salesPartnerNameListTmp = new ArrayList<>();
                    itemIDListTmp = new ArrayList<>();
                    quantityListTmp = new ArrayList<>();
                    priceListTmp = new ArrayList<>();
                    totalListTmp = new ArrayList<>();
                    dateTimeDocListTmp = new ArrayList<>();
                    invoiceSumListTmp = new ArrayList<>();
                    itemsNameListTmp = new ArrayList<>();
                    spTINListTmp = new ArrayList<>();
                    itemIDBuhgalterListTmp = new ArrayList<>();
                    for (int i = 0; i < salesPartnerIDList.size(); i++) {
                        if (mCheckedItems[i]) {
                            Integer tmpID = salesPartnerIDListTmp.get(i);
                            String sql = "SELECT InvoiceNumber, AgentID, SalesPartnerID, ItemID, Quantity, Price, " +
                                    "Total, DateTimeDoc, InvoiceSum, salesPartners.????????????????????????, salesPartners.??????, " +
                                    "items.????????????????????????, items.??????????????_1?? " +
                                    "FROM invoiceAggregate INNER JOIN salesPartners ON " +
                                    "invoiceAggregate.SalesPartnerID = salesPartners.serverDB_ID " +
                                    "INNER JOIN items ON invoiceAggregate.ItemID = items.?????????????? " +
                                    "WHERE DateTimeDoc BETWEEN " +
                                    "? AND ? AND AccountingType = ? AND SalesPartnerID = ?";
                            c = db.rawQuery(sql, new String[]{dateStart, dateEnd, chosenAccountingType, tmpID.toString()});

                            if (c.moveToFirst()) {
                                int invoiceNumberTmp = c.getColumnIndex("InvoiceNumber");
                                int agentIDTmp = c.getColumnIndex("AgentID");
                                int salesPartnerIDTmp = c.getColumnIndex("SalesPartnerID");
                                int itemIDTmp = c.getColumnIndex("ItemID");
                                int quantityTmp = c.getColumnIndex("Quantity");
                                int priceTmp = c.getColumnIndex("Price");
                                int totalTmp = c.getColumnIndex("Total");
                                int dateTimeDocTmp = c.getColumnIndex("DateTimeDoc");
                                int invoiceSumTmp = c.getColumnIndex("InvoiceSum");
                                do {
                                    invoiceNumberListTmp.add(c.getInt(invoiceNumberTmp));
                                    agentIDListTmp.add(c.getInt(agentIDTmp));
                                    salesPartnerIDListTmp.add(c.getInt(salesPartnerIDTmp));
                                    itemIDListTmp.add(c.getInt(itemIDTmp));
                                    quantityListTmp.add(c.getDouble(quantityTmp));
                                    priceListTmp.add(c.getDouble(priceTmp));
                                    totalListTmp.add(c.getDouble(totalTmp));
                                    dateTimeDocListTmp.add(c.getString(dateTimeDocTmp));
                                    invoiceSumListTmp.add(c.getDouble(invoiceSumTmp));
                                    salesPartnerNameListTmp.add(c.getString(9));
                                    spTINListTmp.add(c.getString(10));
                                    itemsNameListTmp.add(c.getString(11));
                                    itemIDBuhgalterListTmp.add(c.getInt(12));
                                } while (c.moveToNext());
                            }
                            c.close();
                        }
                    }
                } else {
                    String sql = "SELECT InvoiceNumber, AgentID, SalesPartnerID, ItemID, Quantity, Price, " +
                            "Total, DateTimeDoc, InvoiceSum, salesPartners.????????????????????????, salesPartners.??????, " +
                            "items.????????????????????????, items.??????????????_1?? " +
                            "FROM invoiceAggregate INNER JOIN salesPartners ON " +
                            "invoiceAggregate.SalesPartnerID = salesPartners.serverDB_ID " +
                            "INNER JOIN items ON invoiceAggregate.ItemID = items.?????????????? " +
                            "WHERE DateTimeDoc BETWEEN " +
                            "? AND ? AND AccountingType = ?";
                    c = db.rawQuery(sql, new String[]{dateStart, dateEnd, chosenAccountingType});

                    if (c.moveToFirst()) {
                        int invoiceNumberTmp = c.getColumnIndex("InvoiceNumber");
                        int agentIDTmp = c.getColumnIndex("AgentID");
                        int salesPartnerIDTmp = c.getColumnIndex("SalesPartnerID");
                        int itemIDTmp = c.getColumnIndex("ItemID");
                        int quantityTmp = c.getColumnIndex("Quantity");
                        int priceTmp = c.getColumnIndex("Price");
                        int totalTmp = c.getColumnIndex("Total");
                        int dateTimeDocTmp = c.getColumnIndex("DateTimeDoc");
                        int invoiceSumTmp = c.getColumnIndex("InvoiceSum");
                        invoiceNumberListTmp = new ArrayList<>();
                        agentIDListTmp = new ArrayList<>();
                        salesPartnerIDListTmp = new ArrayList<>();
                        itemIDListTmp = new ArrayList<>();
                        quantityListTmp = new ArrayList<>();
                        priceListTmp = new ArrayList<>();
                        totalListTmp = new ArrayList<>();
                        dateTimeDocListTmp = new ArrayList<>();
                        invoiceSumListTmp = new ArrayList<>();
                        salesPartnerNameListTmp = new ArrayList<>();
                        itemsNameListTmp = new ArrayList<>();
                        spTINListTmp = new ArrayList<>();
                        itemIDBuhgalterListTmp = new ArrayList<>();
                        do {
                            invoiceNumberListTmp.add(c.getInt(invoiceNumberTmp));
                            agentIDListTmp.add(c.getInt(agentIDTmp));
                            salesPartnerIDListTmp.add(c.getInt(salesPartnerIDTmp));
                            itemIDListTmp.add(c.getInt(itemIDTmp));
                            quantityListTmp.add(c.getDouble(quantityTmp));
                            priceListTmp.add(c.getDouble(priceTmp));
                            totalListTmp.add(c.getDouble(totalTmp));
                            dateTimeDocListTmp.add(c.getString(dateTimeDocTmp));
                            invoiceSumListTmp.add(c.getDouble(invoiceSumTmp));
                            salesPartnerNameListTmp.add(c.getString(9));
                            spTINListTmp.add(c.getString(10));
                            itemsNameListTmp.add(c.getString(11));
                            itemIDBuhgalterListTmp.add(c.getInt(12));
                        } while (c.moveToNext());
                    }
                    c.close();
                }
            }
            if (!chosenArea.equals("??????????") &&
                    !dateStart.equals("") && !dateEnd.equals("")) {
                if (!mChecked) {
                    String sql = "SELECT InvoiceNumber, AgentID, SalesPartnerID, ItemID, Quantity, Price, " +
                            "Total, DateTimeDoc, InvoiceSum, salesPartners.????????????????????????, salesPartners.??????, " +
                            "items.????????????????????????, items.??????????????_1?? " +
                            "FROM invoiceAggregate INNER JOIN salesPartners ON " +
                            "invoiceAggregate.SalesPartnerID = salesPartners.serverDB_ID " +
                            "INNER JOIN items ON invoiceAggregate.ItemID = items.?????????????? " +
                            "WHERE DateTimeDoc BETWEEN ? AND ? " +
                            "AND AccountingType = ? AND AgentID = ?";
                    c = db.rawQuery(sql, new String[]{dateStart, dateEnd, chosenAccountingType, chosenArea});

                    if (c.moveToFirst()) {
                        int invoiceNumberTmp = c.getColumnIndex("InvoiceNumber");
                        int agentIDTmp = c.getColumnIndex("AgentID");
                        int salesPartnerIDTmp = c.getColumnIndex("SalesPartnerID");
                        int itemIDTmp = c.getColumnIndex("ItemID");
                        int quantityTmp = c.getColumnIndex("Quantity");
                        int priceTmp = c.getColumnIndex("Price");
                        int totalTmp = c.getColumnIndex("Total");
                        int dateTimeDocTmp = c.getColumnIndex("DateTimeDoc");
                        int invoiceSumTmp = c.getColumnIndex("InvoiceSum");
                        invoiceNumberListTmp = new ArrayList<>();
                        agentIDListTmp = new ArrayList<>();
                        salesPartnerIDListTmp = new ArrayList<>();
                        itemIDListTmp = new ArrayList<>();
                        quantityListTmp = new ArrayList<>();
                        priceListTmp = new ArrayList<>();
                        totalListTmp = new ArrayList<>();
                        dateTimeDocListTmp = new ArrayList<>();
                        invoiceSumListTmp = new ArrayList<>();
                        salesPartnerNameListTmp = new ArrayList<>();
                        itemsNameListTmp = new ArrayList<>();
                        spTINListTmp = new ArrayList<>();
                        itemIDBuhgalterListTmp = new ArrayList<>();
                        do {
                            invoiceNumberListTmp.add(c.getInt(invoiceNumberTmp));
                            agentIDListTmp.add(c.getInt(agentIDTmp));
                            salesPartnerIDListTmp.add(c.getInt(salesPartnerIDTmp));
                            itemIDListTmp.add(c.getInt(itemIDTmp));
                            quantityListTmp.add(c.getDouble(quantityTmp));
                            priceListTmp.add(c.getDouble(priceTmp));
                            totalListTmp.add(c.getDouble(totalTmp));
                            dateTimeDocListTmp.add(c.getString(dateTimeDocTmp));
                            invoiceSumListTmp.add(c.getDouble(invoiceSumTmp));
                            salesPartnerNameListTmp.add(c.getString(9));
                            spTINListTmp.add(c.getString(10));
                            itemsNameListTmp.add(c.getString(11));
                            itemIDBuhgalterListTmp.add(c.getInt(12));
                        } while (c.moveToNext());
                    }
                    c.close();
                } else {
                    invoiceNumberListTmp = new ArrayList<>();
                    agentIDListTmp = new ArrayList<>();
                    salesPartnerIDListTmp = new ArrayList<>();
                    itemIDListTmp = new ArrayList<>();
                    quantityListTmp = new ArrayList<>();
                    priceListTmp = new ArrayList<>();
                    totalListTmp = new ArrayList<>();
                    dateTimeDocListTmp = new ArrayList<>();
                    invoiceSumListTmp = new ArrayList<>();
                    salesPartnerNameListTmp = new ArrayList<>();
                    itemsNameListTmp = new ArrayList<>();
                    spTINListTmp = new ArrayList<>();
                    itemIDBuhgalterListTmp = new ArrayList<>();
                    for (int i = 0; i < salesPartnerIDList.size(); i++) {
                        if (mCheckedItems[i]) {
                            Integer tmpID = salesPartnerIDListTmp.get(i);
                            String sql = "SELECT InvoiceNumber, AgentID, SalesPartnerID, ItemID, Quantity, Price, " +
                                    "Total, DateTimeDoc, InvoiceSum, salesPartners.????????????????????????," +
                                    " salesPartners.??????, items.????????????????????????, items.??????????????_1?? " +
                                    "FROM invoiceAggregate INNER JOIN salesPartners ON " +
                                    "invoiceAggregate.SalesPartnerID = salesPartners.serverDB_ID " +
                                    "INNER JOIN items ON invoiceAggregate.ItemID = items.?????????????? " +
                                    "WHERE DateTimeDoc > ? " +
                                    "AND AccountingType = ? AND SalesPartnerID = ?";
                            c = db.rawQuery(sql, new String[]{output, chosenAccountingType, tmpID.toString()});

                            if (c.moveToFirst()) {
                                int invoiceNumberTmp = c.getColumnIndex("InvoiceNumber");
                                int agentIDTmp = c.getColumnIndex("AgentID");
                                int salesPartnerIDTmp = c.getColumnIndex("SalesPartnerID");
                                int itemIDTmp = c.getColumnIndex("ItemID");
                                int quantityTmp = c.getColumnIndex("Quantity");
                                int priceTmp = c.getColumnIndex("Price");
                                int totalTmp = c.getColumnIndex("Total");
                                int dateTimeDocTmp = c.getColumnIndex("DateTimeDoc");
                                int invoiceSumTmp = c.getColumnIndex("InvoiceSum");
                                do {
                                    invoiceNumberListTmp.add(c.getInt(invoiceNumberTmp));
                                    agentIDListTmp.add(c.getInt(agentIDTmp));
                                    salesPartnerIDListTmp.add(c.getInt(salesPartnerIDTmp));
                                    itemIDListTmp.add(c.getInt(itemIDTmp));
                                    quantityListTmp.add(c.getDouble(quantityTmp));
                                    priceListTmp.add(c.getDouble(priceTmp));
                                    totalListTmp.add(c.getDouble(totalTmp));
                                    dateTimeDocListTmp.add(c.getString(dateTimeDocTmp));
                                    invoiceSumListTmp.add(c.getDouble(invoiceSumTmp));
                                    salesPartnerNameListTmp.add(c.getString(9));
                                    spTINListTmp.add(c.getString(10));
                                    itemsNameListTmp.add(c.getString(11));
                                    itemIDBuhgalterListTmp.add(c.getInt(12));
                                } while (c.moveToNext());
                            }
                            c.close();
                        }
                    }
                }
            }

            for (int i = 0; i < invoiceNumberListTmp.size(); i++) {
                if (quantityListTmp.get(i) > 0) {
                    if (salesPartnerIDListTmp.get(i).equals(1127) || salesPartnerIDListTmp.get(i).equals(357)
                            || salesPartnerIDListTmp.get(i).equals(505)
                            || salesPartnerIDListTmp.get(i).equals(512) || salesPartnerIDListTmp.get(i).equals(1057)
                            || salesPartnerIDListTmp.get(i).equals(1059)
                            || salesPartnerIDListTmp.get(i).equals(339) || salesPartnerIDListTmp.get(i).equals(351)
                            || (salesPartnerIDListTmp.get(i) > 99 && salesPartnerIDListTmp.get(i) < 106)) {
                        invoiceNumberListTmpTypeTwoReduced.add(invoiceNumberListTmp.get(i));
                        agentIDListTmpTypeTwoReduced.add(agentIDListTmp.get(i));
                        salesPartnerNameListTmpTypeTwoReduced.add(salesPartnerNameListTmp.get(i));
                        spTINListTmpTypeTwoReduced.add(spTINListTmp.get(i));
                        itemNameListTmpTypeTwoReduced.add(itemsNameListTmp.get(i));
                        itemIDBuhgalterListTmpTypeTwoReduced.add(itemIDBuhgalterListTmp.get(i));
                        priceListTmpTypeTwoReduced.add(priceListTmp.get(i));
                        quantityListTmpTypeTwoReduced.add(quantityListTmp.get(i));
                        totalListTmpTypeTwoReduced.add(totalListTmp.get(i));
                        invoiceSumListTmpTypeTwoReduced.add(invoiceSumListTmp.get(i));
                        dateTimeDocListTmpTypeTwoReduced.add(dateTimeDocListTmp.get(i));
                    } else {
                        invoiceNumberListTmpTypeOneReduced.add(invoiceNumberListTmp.get(i));
                        agentIDListTmpTypeOneReduced.add(agentIDListTmp.get(i));
                        salesPartnerNameListTmpTypeOneReduced.add(salesPartnerNameListTmp.get(i));
                        spTINListTmpTypeOneReduced.add(spTINListTmp.get(i));
                        itemNameListTmpTypeOneReduced.add(itemsNameListTmp.get(i));
                        itemIDBuhgalterListTmpTypeOneReduced.add(itemIDBuhgalterListTmp.get(i));
                        priceListTmpTypeOneReduced.add(priceListTmp.get(i));
                        quantityListTmpTypeOneReduced.add(quantityListTmp.get(i));
                        totalListTmpTypeOneReduced.add(totalListTmp.get(i));
                        invoiceSumListTmpTypeOneReduced.add(invoiceSumListTmp.get(i));
                        dateTimeDocListTmpTypeOneReduced.add(dateTimeDocListTmp.get(i));
                    }
                }
            }
            Toast.makeText(getApplicationContext(), String.valueOf(invoiceNumberListTmp.size()), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLocalDB(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("???????????????????? ?????????????????? ???????? ????????????")
                .setMessage("?????? ?????????????? ?????????? ????????????????????????!")
                .setCancelable(true)
                .setPositiveButton("??????????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainMenu();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("????????????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                receiveUpdateFromServerPrompt();
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
    }

    private void receiveUpdateFromServerPrompt(){
        final boolean[] listChecked = new boolean[7];
        final String[] listCheckTableName = { "??????????????????????", "????????????????????????", "???????????????????????????? ????????????",
                "?????? ????????????", "??????????????????", "??????????????", "????????????" };
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
                            public void onClick(DialogInterface dialog, int id) {
                                for (int i = 0; i < listCheckTableName.length; i++) {
                                    if (listChecked[i] == true){
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
//                                            db.execSQL("DROP TABLE IF EXISTS invoice");
                                            dbHelper.onUpgrade(db, 1, 2);
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {

                                                    loadInvoicesFromServerDB(g);
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
                                        if (listCheckTableName[i].equals("????????????")){
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
    }

    private void loadSalesPartnersFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    String[] dayOfTheWeek = new String[jsonArray.length()];
                    String[] salesPartnersName = new String[jsonArray.length()];
                    String[] tin = new String[jsonArray.length()];
                    Integer[] area = new Integer[jsonArray.length()];
                    String[] accountingType = new String[jsonArray.length()];
                    String[] author = new String[jsonArray.length()];
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("DayOfTheWeek");
                            salesPartnersName[i] = obj.getString("????????????????????????");
                            tin[i] = obj.getString("??????");
                            area[i] = obj.getInt("??????????");
                            accountingType[i] = obj.getString("????????");
                            author[i] = obj.getString("??????????");
                            serverDB_ID[i] = obj.getInt("ID");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in salesPartners: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("????????????????????????", salesPartnersName[i]);
                            cv.put("??????", tin[i]);
                            cv.put("??????????", area[i]);
                            cv.put("????????", accountingType[i]);
                            cv.put("DayOfTheWeek", dayOfTheWeek[i]);
                            cv.put("??????????", author[i]);
                            long rowID = db.insert("salesPartners", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "?????????????????????? ??????????????????", Toast.LENGTH_SHORT).show();
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
                    Integer[] itemNumberBuhgalter = new Integer[jsonArray.length()];
                    String[] itemDescription = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemNumber[i] = obj.getInt("??????????????");
                            itemNumberBuhgalter[i] = obj.getInt("??????????????_1??");
                            itemName[i] = obj.getString("????????????????????????");
                            itemDescription[i] = obj.getString("????????????????");
                            itemPrice[i] = obj.getInt("????????");
                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in items: ---");
                            cv.put("??????????????", itemNumber[i]);
                            cv.put("??????????????_1??", itemNumberBuhgalter[i]);
                            cv.put("????????????????????????", itemName[i]);
                            cv.put("????????????????", itemName[i]);
                            cv.put("????????", itemPrice[i]);
                            long rowID = db.insert("items", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "???????????????????????? ??????????????????", Toast.LENGTH_SHORT).show();
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

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in itemsWithDiscount: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("??????????????", itemNumber[i]);
                            cv.put("ID_????????????", discountID[i]);
                            cv.put("ID_??????????????????????", spID[i]);
                            cv.put("??????????", author[i]);
                            long rowID = db.insert("itemsWithDiscount", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "???????????????????????????? ???????????? ??????????????????", Toast.LENGTH_SHORT).show();
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

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in discount: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("??????_????????????", discountType[i]);
                            cv.put("????????????", discount[i]);
                            cv.put("??????????", author[i]);
                            long rowID = db.insert("discount", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "???????? ???????????? ??????????????????", Toast.LENGTH_SHORT).show();
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
//                            Log.d(LOG_TAG, "--- Insert in invoice: ---");
//                            cv.put("serverDB_ID", serverDB_ID[i]);
//                            cv.put("InvoiceNumber", invoiceNumber[i]);
//                            cv.put("AgentID", agentID[i]);
//                            cv.put("SalesPartnerID", salesPartnerID[i]);
//                            cv.put("AccountingType", accountingType[i]);
//                            cv.put("ItemID", itemNumber[i]);
//                            cv.put("Quantity", itemQuantity[i]);
//                            cv.put("Price", itemPrice[i]);
//                            cv.put("Total", totalSum[i]);
//                            cv.put("ExchangeQuantity", exchangeQuantity[i]);
//                            cv.put("ReturnQuantity", returnQuantity[i]);
//                            cv.put("DateTimeDoc", dateTimeDoc[i]);
//                            cv.put("InvoiceSum", invoiceSum[i]);
//                            cv.put("Comment", comment[i]);
//                            long rowID = db.insert("invoice", null, cv);
//                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);

//                            cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in invoiceAggregate: ---");
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
                        Toast.makeText(getApplicationContext(), "?????????????????? ??????????????????", Toast.LENGTH_SHORT).show();
                    }else{
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
                            dateTimeDoc[i] = obj.getString("????????_??????????????");
                            invoiceNumber[i] = obj.getInt("???_??????????????????");
                            paymentAmount[i] = obj.getDouble("??????????_????????????????");
                            author[i] = obj.getString("??????????");

                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in paymentsServer: ---");
                            cv.put("serverDB_ID", serverDB_ID[i]);
                            cv.put("DateTimeDoc", dateTimeDoc[i]);
                            cv.put("InvoiceNumber", invoiceNumber[i]);
                            cv.put("??????????_????????????????", paymentAmount[i]);
                            cv.put("??????????", author[i]);
                            long rowID = db.insert("paymentsServer", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        Toast.makeText(getApplicationContext(), "?????????????? ??????????????????", Toast.LENGTH_SHORT).show();
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
                parameters.put("tableName", "??????????????");
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

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public void read() throws IOException  {
        File sd = Environment.getExternalStorageDirectory();
        csvFileCopy = "??????????????????_????_" + output + "_" + (invoiceNumberListTmpTypeOneReduced.size() + 1) + ".xls";
        csvFileCopyTwo = "??????????????????_????_" + output + "_" + (invoiceNumberListTmpTypeTwoReduced.size() + 1) + ".xls";

        File directory = new File(sd.getAbsolutePath() + File.separator + "Download" +
                File.separator + "Excel" + File.separator + "Accountant_??????????");
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        String fileLocationOne = sd.getAbsolutePath() + File.separator + "Download" +
                File.separator + "Excel" + File.separator + "Accountant_??????????" + File.separator + csvFileCopy;
        String fileLocationTwo = sd.getAbsolutePath() + File.separator + "Download" +
                File.separator + "Excel" + File.separator + "Accountant_??????????" + File.separator + csvFileCopyTwo;
        List<String> filesForEmail = new ArrayList<>();
        filesForEmail.add(fileLocationOne);
        if (invoiceNumberListTmpTypeTwoReduced.size() > 0) {
            filesForEmail.add(fileLocationTwo);
        }
        File file = new File(directory, csvFileCopy);
        File fileTwo = new File(directory, csvFileCopyTwo);
        File inputWorkbook = new File(inputFile);
        Workbook w;

//        Toast.makeText(getApplicationContext(), String.valueOf(Environment.getExternalStorageDirectory()), Toast.LENGTH_SHORT).show();

        Integer k = 1;
        try {
            w = Workbook.getWorkbook(inputWorkbook);

            WritableWorkbook copy = Workbook.createWorkbook(file, w);
            WritableSheet sheet = copy.getSheet(0);
            WritableWorkbook copyTwo = Workbook.createWorkbook(fileTwo, w);
            WritableSheet sheetTwo = copyTwo.getSheet(0);

            for (int j = 0; j < 13; j++) {
                for (int i = 0; i < invoiceNumberListTmpTypeOneReduced.size() + 1; i++) {
                    WritableCell cellWritable  = sheet.getWritableCell(j, i);
                    CellFormat cfm = cellWritable.getCellFormat();
                    Cell readCell = sheet.getCell(j, i);
                    Label label = new Label(j, i, readCell.getContents());

                    if (j == 0 && i == 1) {
                        label = new Label(j, i, String.valueOf(k)); //?????????? ??????????????
                        k += 1;
                    }
                    if (j == 0 && i > 1) {
                        label = new Label(j, i, String.valueOf(k)); //?????????? ??????????????
                        k += 1;
                    }
                    if (j == 1 && i > 0) {
                        label = new Label(j, i, String.valueOf(0)); //?????????? ??????????????????
                        k += 1;
                    }
                    if (j == 2 && i > 0) {
                        label = new Label(j, i, String.valueOf(invoiceNumberListTmpTypeOneReduced.get(i - 1))); //?????????? ??????????????????
                        k += 1;
                    }
                    if (j == 3 && i > 0) {
                        label = new Label(j, i, String.valueOf(agentIDListTmpTypeOneReduced.get(i - 1))); //?????????? ????????????
                        k += 1;
                    }
                    if (j == 4 && i > 0) {
                        label = new Label(j, i, String.valueOf(salesPartnerNameListTmpTypeOneReduced.get(i - 1))); //???????????????? ????????????????
                        k += 1;
                    }
                    if (j == 5 && i > 0) {
                        label = new Label(j, i, spTINListTmpTypeOneReduced.get(i - 1)); //??????
                        k += 1;
                    }
                    if (j == 6 && i > 0) {
                        label = new Label(j, i, String.valueOf(itemNameListTmpTypeOneReduced.get(i - 1))); //????????????????????????
                        k += 1;
                    }
                    if (j == 7 && i > 0) {
                        label = new Label(j, i, String.valueOf(itemIDBuhgalterListTmpTypeOneReduced.get(i - 1))); //??????????????_1??
                        k += 1;
                    }
                    if (j == 8 && i > 0) {
                        label = new Label(j, i, String.valueOf(priceListTmpTypeOneReduced.get(i - 1)).replace(".", ",")); //????????
                        k += 1;
                    }
                    if (j == 9 && i > 0) {
                        label = new Label(j, i, String.valueOf(quantityListTmpTypeOneReduced.get(i - 1)).replace(".", ",")); //??????-????
                        k += 1;
                    }
                    if (j == 10 && i > 0) {
                        label = new Label(j, i, String.valueOf(totalListTmpTypeOneReduced.get(i - 1)).replace(".", ",")); //??????????
                        k += 1;
                    }
                    if (j == 11 && i > 0) {
                        label = new Label(j, i, String.valueOf(invoiceSumListTmpTypeOneReduced.get(i - 1)).replace(".", ",")); //??????????
                        k += 1;
                    }
                    if (j == 12 && i > 0) {
                        label = new Label(j, i, (dateTimeDocListTmpTypeOneReduced.get(i - 1)).replaceAll("-", ".")); //???????? ??????????????
                        k += 1;
                    }
                    sheet.addCell(label);
                    CellView cell = sheet.getColumnView(j);
                    cell.setAutosize(true);
                    sheet.setColumnView(j, cell);
                    cellWritable.setCellFormat(cfm);
                }
                if (invoiceNumberListTmpTypeTwoReduced.size() > 0) {
                    for (int i = 0; i < invoiceNumberListTmpTypeTwoReduced.size() + 1; i++) {
                        WritableCell cellWritable = sheetTwo.getWritableCell(j, i);
                        CellFormat cfm = cellWritable.getCellFormat();
                        Cell readCell = sheetTwo.getCell(j, i);
                        Label label = new Label(j, i, readCell.getContents());

                        if (j == 0 && i == 1) {
                            label = new Label(j, i, String.valueOf(k)); //?????????? ??????????????
                            k += 1;
                        }
                        if (j == 0 && i > 1) {
                            label = new Label(j, i, String.valueOf(k)); //?????????? ??????????????
                            k += 1;
                        }
                        if (j == 1 && i > 0) {
                            label = new Label(j, i, String.valueOf(0)); //?????????? ??????????????????
                            k += 1;
                        }
                        if (j == 2 && i > 0) {
                            label = new Label(j, i, String.valueOf(invoiceNumberListTmpTypeTwoReduced.get(i - 1))); //?????????? ??????????????????
                            k += 1;
                        }
                        if (j == 3 && i > 0) {
                            label = new Label(j, i, String.valueOf(agentIDListTmpTypeTwoReduced.get(i - 1))); //?????????? ????????????
                            k += 1;
                        }
                        if (j == 4 && i > 0) {
                            label = new Label(j, i, String.valueOf(salesPartnerNameListTmpTypeTwoReduced.get(i - 1))); //???????????????? ????????????????
                            k += 1;
                        }
                        if (j == 5 && i > 0) {
                            label = new Label(j, i, spTINListTmpTypeTwoReduced.get(i - 1)); //??????
                            k += 1;
                        }
                        if (j == 6 && i > 0) {
                            label = new Label(j, i, String.valueOf(itemNameListTmpTypeTwoReduced.get(i - 1))); //????????????????????????
                            k += 1;
                        }
                        if (j == 7 && i > 0) {
                            label = new Label(j, i, String.valueOf(itemIDBuhgalterListTmpTypeTwoReduced.get(i - 1))); //??????????????_1??
                            k += 1;
                        }
                        if (j == 8 && i > 0) {
                            label = new Label(j, i, String.valueOf(priceListTmpTypeTwoReduced.get(i - 1)).replace(".", ",")); //????????
                            k += 1;
                        }
                        if (j == 9 && i > 0) {
                            label = new Label(j, i, String.valueOf(quantityListTmpTypeTwoReduced.get(i - 1)).replace(".", ",")); //??????-????
                            k += 1;
                        }
                        if (j == 10 && i > 0) {
                            label = new Label(j, i, String.valueOf(totalListTmpTypeTwoReduced.get(i - 1)).replace(".", ",")); //??????????
                            k += 1;
                        }
                        if (j == 11 && i > 0) {
                            label = new Label(j, i, String.valueOf(invoiceSumListTmpTypeTwoReduced.get(i - 1)).replace(".", ",")); //??????????
                            k += 1;
                        }
                        if (j == 12 && i > 0) {
                            label = new Label(j, i, (dateTimeDocListTmpTypeTwoReduced.get(i - 1)).replaceAll("-", ".")); //???????? ??????????????
                            k += 1;
                        }
                        sheetTwo.addCell(label);
                        CellView cell = sheetTwo.getColumnView(j);
                        cell.setAutosize(true);
                        sheetTwo.setColumnView(j, cell);
                        cellWritable.setCellFormat(cfm);
                    }
                }
            }
            copy.write();
            copy.close();
            copyTwo.write();
            copyTwo.close();
            w.close();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
        sendViaEmailMultipleAttachments(this, "deadprofessor@hotmail.com",
                "iamcomputers@mail.ru", "??????????????????", "?????????????????? ?????? ??????????????????????", filesForEmail);
    }

    private void makeExcel(){
        File sd = Environment.getExternalStorageDirectory();
        csvFile = "accountant_form.xls";
        File directory = new File(sd.getAbsolutePath() + File.separator + "Download" +
                File.separator + "Excel"  + File.separator + "Accountant_??????????_??????????");
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        setInputFile(directory + File.separator + csvFile);
//        MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
    }

    private void sendViaEmail(String file_name, String emailAddress) {
        try {
            File sd= Environment.getExternalStorageDirectory();
            String fileLocation = sd.getAbsolutePath() + File.separator + "Download" +
                    File.separator + "Excel" + File.separator + "Accountant_??????????" + File.separator + file_name;
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            String message="???????? ?? ???????????????????? ???? ???????????? " + file_name + ".";
            intent.putExtra(Intent.EXTRA_SUBJECT, "??????????????????");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://" + fileLocation));
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setData(Uri.parse("mailto:" + emailAddress));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } catch(Exception e)  {
            System.out.println("is exception raises during sending mail" + e);
        }
    }

    public void sendViaEmailMultipleAttachments(Context context, String emailTo, String emailCC,
                             String subject, String emailText, List<String> filePaths)
    {
        //need to "send multiple" to get more than one attachment
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{emailTo});
        emailIntent.putExtra(android.content.Intent.EXTRA_CC,
                new String[]{emailCC});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //has to be an ArrayList
        ArrayList<Uri> uris = new ArrayList<>();
        //convert from paths to Android friendly Parcelable Uri's
        for (String file : filePaths)
        {
            File fileIn = new File(file);
            fileIn.setReadable(true, false);
            Uri u = FileProvider.getUriForFile(this,"com.example.myapplicationtest.fileprovider", fileIn);
            uris.add(u);
        }

//        Uri u = Uri.parse("file://" + filePaths[0]);
//        uris.add(u);
//        u = Uri.parse("file://" + filePaths[1]);
//        uris.add(u);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
//        startActivity(emailIntent);
    }

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
                        + "?????? text,"
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

    private void dropAggregate(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder = new AlertDialog.Builder(this);
        builder.setTitle("?????????? ???????????????????????? ??????????????")
//                .setMessage("?????? ?????????????? ?????????? ????????????????????????!")
                .setCancelable(true)
                .setNegativeButton("????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.execSQL("DROP TABLE IF EXISTS invoiceAggregate");
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
    }
}
