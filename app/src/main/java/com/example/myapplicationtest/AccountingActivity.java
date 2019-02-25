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
import java.util.Locale;
import java.util.Map;

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
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class AccountingActivity extends AppCompatActivity implements View.OnClickListener {

    String csvFile, folder_name, csvFileCopy;
    Button btnOptions, btnExecute;
    TextView textViewAgentName, textViewAccountingType;
    EditText editTextDateStart, editTextDateEnd;
    String chosenArea = "любой", chosenRoot = "любой", chosenAccountingType = "провод", dbName, dbUser,
            dbPassword, syncUrl = "https://caiman.ru.com/php/syncDB.php", areaDefault = "accountant",
            agentChosen = "любой", dateStart = "", dateEnd = "", email = "accountant@caiman.ru.com";
    DBHelper dbHelper;
    SQLiteDatabase db;
    final String LOG_TAG = "myLogs";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser;
    ArrayList<Integer> agentAreaList, salesPartnerIDList, invoiceNumberListTmp, agentIDListTmp,
            itemIDListTmp, salesPartnerIDListTmp;
    ArrayList<Double> quantityListTmp, priceListTmp, totalListTmp, invoiceSumListTmp;
    ArrayList<String> secondNameList, firstNameList, middleNameList, salesPartnerNameList,
            dateTimeDocListTmp;
    String[] agentsList, salesPartnersList;
    boolean[] mCheckedItems;
    private String inputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting);

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
        textViewAgentName.setText("Любой Агент");
        textViewAccountingType.setText("Проводные документы");

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
        final String[] choice ={"Настройки", "Контрагенты", "Агенты", "Учёт", "Просмотр", "Выполнить", "Обновить БД"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Меню");
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("Настройки")){
                    settingsMenu();
                    dialog.cancel();
                }
                if (choice[item].equals("Контрагенты")){
                    salesPartnerChoiceMenu();
                    dialog.cancel();
                }
                if (choice[item].equals("Агенты")){
                    agentChoice();
                    dialog.cancel();
                }
                if (choice[item].equals("Учёт")){
                    accountingTypeChoice();
                    dialog.cancel();
                }
                if (choice[item].equals("Просмотр")){
                    showChosen();
                    dialog.cancel();
                }
                if (choice[item].equals("Выполнить")){
                    executeChoice();
                    dialog.cancel();
                }
                if (choice[item].equals("Обновить БД")){
                    updateLocalDB();
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
        final String[] choice ={"Изменить фильтр", "Показать"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Контрагенты/Меню");
        builder.setPositiveButton("Назад",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("Изменить фильтр")){
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("Показать")){
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
        final String[] choice ={"Район", "Маршрут", "Учёт"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Контрагенты/Фильтр");
        builder.setPositiveButton("Назад",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        salesPartnerChoiceMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("Район")){
                    salesPartnerChoiceArea();
                    dialog.cancel();
                }
                if (choice[item].equals("Маршрут")){
                    salesPartnerChoiceRoot();
                    dialog.cancel();
                }
                if (choice[item].equals("Учёт")){
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
        final String[] choice = {"Район №1", "Район №2", "Район №3", "Район №4", "Район №5"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Контрагенты/Район");
        builder.setPositiveButton("Назад",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        salesPartnerChoiceFilter();
                        dialog.cancel();
                    }
                });
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (choice[item].equals("Район №1")) {
                    chosenArea  = "1";
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("Район №2")) {
                    chosenArea = "2";
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("Район №3")) {
                    chosenArea = "3";
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("Район №4")) {
                    chosenArea = "4";
                    salesPartnerChoiceFilter();
                    dialog.cancel();
                }
                if (choice[item].equals("Район №5")) {
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
        final String[] choice = {"понедельник-четверг", "среда", "вторник-пятница", "север", "любой"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Контрагенты/Маршрут");
        builder.setPositiveButton("Назад",
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
        final String[] choice = {"провод", "непровод"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Контрагенты/Учёт");
        builder.setPositiveButton("Назад",
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
            if (chosenRoot.equals("любой") && !chosenArea.equals("любой")){
                sql = "SELECT serverDB_ID, Наименование FROM salesPartners " +
                        "WHERE Район LIKE ? AND Учет LIKE ? ";
                c = db.rawQuery(sql, new String[]{chosenArea, chosenAccountingType});
            } else {
                sql = "SELECT serverDB_ID, Наименование FROM salesPartners " +
                        "WHERE DayOfTheWeek LIKE ? AND Учет LIKE ? ";
                c = db.rawQuery(sql, new String[]{chosenRoot, chosenAccountingType});
            }
            if (!chosenRoot.equals("любой") && !chosenArea.equals("любой")){
                sql = "SELECT serverDB_ID, Наименование FROM salesPartners " +
                        "WHERE Район LIKE ? AND Учет LIKE ? AND DayOfTheWeek LIKE ?";
                c = db.rawQuery(sql, new String[]{chosenArea, chosenAccountingType, chosenRoot});
            }
            if (chosenRoot.equals("любой") && chosenArea.equals("любой")){
                sql = "SELECT serverDB_ID, Наименование FROM salesPartners " +
                        "WHERE Учет LIKE ? ";
                c = db.rawQuery(sql, new String[]{chosenAccountingType});
            }

            if (c.moveToFirst()) {
                int salesPartnerIDTmp = c.getColumnIndex("serverDB_ID");
                int salesPartnerNameTmp = c.getColumnIndex("Наименование");
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
        builder.setTitle("Выберите контрагентов")
                .setCancelable(true)
                .setMultiChoiceItems(salesPartnersList, mCheckedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                mCheckedItems[which] = isChecked;
                            }
                        })
                .setPositiveButton("Готово",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                for (int i = 0; i < salesPartnersList.length; i++) {

                                    if (mCheckedItems[i]){

                                    }
                                }
                            }
                        })
                .setNegativeButton("Отмена",
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
        builder.setTitle("Агенты/выбрать");
        builder.setItems(agentsList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                agentChosen = agentsList[item];
                chosenArea = agentAreaList.get(item).toString();
                textViewAgentName.setText(agentChosen);
                mainMenu();
            }
        });
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void accountingTypeChoice(){
        final String[] choice = {"провод", "непровод"};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Тип учёта");
        builder.setPositiveButton("Назад",
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
        final String[] choice = {"Дата с: " + dateStart, "Дата по: " + dateEnd, "Учёт: " + chosenAccountingType,
                "Район №" + chosenArea, "Агент: " + agentChosen, "Маршрут: " + chosenRoot};
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Заданные параметры");
        builder.setPositiveButton("Назад",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainMenu();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("По умолчанию",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        chosenArea = "любой";
                        chosenAccountingType = "провод";
                        chosenRoot = "любой";
                        agentChosen = "любой";
                        chosenArea = "любой";
                        textViewAccountingType.setText("Проводные документы");
                        textViewAgentName.setText("Любой агент");
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
                chosenAccountingType = choice[item];
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void executeChoice(){
        if (tableExists(db, "invoice")) {
            String sql = "SELECT InvoiceNumber, AgentID, SalesPartnerID, ItemID, Quantity, Price, Total, DateTimeDoc, InvoiceSum FROM invoice";
            Cursor c = db.rawQuery(sql, null);
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

                } while (c.moveToNext());
            }
            c.close();
        }
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
                    String[] itemName= new String[jsonArray.length()];
                    Integer[] itemPrice= new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemNumber[i] = obj.getInt("Артикул");
                            itemName[i] = obj.getString("Наименование");
                            itemPrice[i] = obj.getInt("Цена");
                            ContentValues cv = new ContentValues();
                            Log.d(LOG_TAG, "--- Insert in items: ---");
                            cv.put("Артикул", itemNumber[i]);
                            cv.put("Наименование", itemName[i]);
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

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public void read() throws IOException  {
        File sd = Environment.getExternalStorageDirectory();
        csvFileCopy = "accountant.xls";

        File directory = new File(sd.getAbsolutePath() + File.separator + "Download" + File.separator + "Excel");
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        //file path
        File file = new File(directory, csvFileCopy);
        File inputWorkbook = new File(inputFile);
        Workbook w;
        try {
            w = Workbook.getWorkbook(inputWorkbook);
            WritableWorkbook copy = Workbook.createWorkbook(file, w);
            WritableSheet sheet = copy.getSheet(0);
//            WritableCell cell = sheet.getWritableCell(1, 2);
//            CellFormat cfm = cell.getCellFormat();
//            Label l = (Label) cell;
//            l.setString("modified cell");
//            cell.setCellFormat(cfm);

//            if (cell.getType() == CellType.LABEL)
//            {
//                l = (Label) cell;
//                l.setString("modified cell");
//            }
//            copy.write();
//            copy.close();
//            w.close();
            // Get the first sheet
//            Sheet sheet = w.getSheet(0);
            // Loop over first 10 column and lines

            for (int j = 0; j < sheet.getColumns(); j++) {
                for (int i = 0; i < sheet.getRows(); i++) {
//                    Cell cell = sheet.getCell(j, i);
                    WritableCell cell = sheet.getWritableCell(j, i);
                    CellFormat cfm = cell.getCellFormat();
                    CellType type = cell.getType();
                    if (type == CellType.LABEL) {
                        if (j == 21 && i == 3) {
                            System.out.println("I got a label "
                                    + cell.getContents());
                            Label l = (Label) cell;
                            l.setString("modified cell");
                            cell.setCellFormat(cfm);
                        }
                    }

//                    if (type == CellType.NUMBER) {
//                        System.out.println("I got a number "
//                                + cell.getContents());
//                        Number l = (Number) cell;
//                        l.setValue();
//                        cell.setCellFormat(cfm);
//                    }
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
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd-HH-mm-ss" );
        String output = zdt.format( formatter );

        File sd = Environment.getExternalStorageDirectory();
        csvFile = "accountant-" + output + ".xls";

        File directory = new File(sd.getAbsolutePath() + File.separator + "Download" + File.separator + "Excel");
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        //file path
        File file = new File(directory, csvFile);

        setInputFile(sd.getAbsolutePath() + File.separator + "Download" + File.separator + "Excel" + File.separator + "УПД ИП ЧЕ ВЕ.xls");

        MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
//        WorkbookSettings wbSettings = new WorkbookSettings();
//        wbSettings.setLocale(new Locale("ru", "Ru"));
//        WritableWorkbook workbook;
//        try {
//            workbook = Workbook.createWorkbook(file, wbSettings);
//            //Excel sheet name. 0 represents first sheet
//            WritableSheet sheet = workbook.createSheet("userList", 0);
//            // column and row
//            sheet.addCell(new Label(0, 0, "UserName"));
//            sheet.addCell(new Label(1, 0, "PhoneNumber"));
//            String name = "Vova";
//            String phoneNumber = "Che";
//            sheet.addCell(new Label(0, 1, name));
//            sheet.addCell(new Label(1, 1, phoneNumber));
//
//            workbook.write();
//            workbook.close();
//            Toast.makeText(getApplication(),
//                    "Выбранные данные экспортированы в файл Эксель", Toast.LENGTH_SHORT).show();
//            sendViaEmail("Download" + File.separator + "Excel", csvFile, email);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (RowsExceededException e) {
//            e.printStackTrace();
//        } catch (WriteException e) {
//            e.printStackTrace();
//        }

    }

    private void sendViaEmail(String folder_name, String file_name, String emailAddress) {
        try {
            File sd= Environment.getExternalStorageDirectory();
            String fileLocation = sd.getAbsolutePath() + File.separator + folder_name + File.separator + file_name;
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            String message="Файл с накладными за период " + file_name + ".";
            intent.putExtra(Intent.EXTRA_SUBJECT, "Накладные");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://" + fileLocation));
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setData(Uri.parse("mailto:" + emailAddress));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } catch(Exception e)  {
            System.out.println("is exception raises during sending mail" + e);
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
}
