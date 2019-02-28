package com.example.myapplicationtest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class AgentReportActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnOptions;
    String lastReceiveDate = "", dateStart = "", dateEnd = "", receiveDate = "";
    DBHelper dbHelper;
    final String LOG_TAG = "myLogs";
    SQLiteDatabase db;
    String[] reportList, receiveDateList;
    ArrayMap<String, Double> arrayMapReceive;
    EditText editTextDateStart, editTextDateEnd;
    ArrayList<String> receiveDateListTmp;

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
        ArrayMap<String, Double> arrayMapQuantity = new ArrayMap<>();
        ArrayMap<String, Double> arrayMapExchange = new ArrayMap<>();
        ArrayMap<String, Double> arrayMapReturn = new ArrayMap<>();

        ArrayMap<String, Double> arrayMapQuantityReduced = new ArrayMap<>();
        ArrayMap<String, Double> arrayMapExchangeReduced = new ArrayMap<>();
        ArrayMap<String, Double> arrayMapReturnReduced = new ArrayMap<>();

//        String sql = "SELECT items.Наименование FROM items ";
//        Cursor c = db.rawQuery(sql, null);
//        if (c.moveToFirst()) {
//            int itemNameTmp = c.getColumnIndex("Наименование");
//            itemNameListDefault = new ArrayList<>();
//            do {
//                itemNameListDefault.add(c.getString(itemNameTmp));
//            } while (c.moveToNext());
//        }
        String sql;
        Cursor c;
        if (dateStart.length() > 0 && dateEnd.length() > 0){
            sql = "SELECT items.Наименование, invoice.Quantity, invoice.ExchangeQuantity," +
                    "invoice.ReturnQuantity FROM invoice INNER JOIN items " +
                    "ON invoice.ItemID LIKE items.Артикул " +
                    "WHERE invoice.DateTimeDoc BETWEEN ? AND ?";
            c = db.rawQuery(sql, new String[]{dateStart, dateEnd});
        } else {
            sql = "SELECT items.Наименование, invoice.Quantity, invoice.ExchangeQuantity," +
                    "invoice.ReturnQuantity FROM invoice INNER JOIN items " +
                    "ON invoice.ItemID LIKE items.Артикул " +
                    "WHERE invoice.DateTimeDoc > ?";
            c = db.rawQuery(sql, new String[]{output});
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
                Double tmp = arrayMapReceive.valueAt(i) - arrayMapQuantityReduced.valueAt(i) - arrayMapExchangeReduced.valueAt(i);
                reportList[i] = "Обмен: " + arrayMapExchangeReduced.valueAt(i).toString() + System.getProperty("line.separator") +
                        "Наименование: " + System.getProperty("line.separator") +
                        arrayMapExchangeReduced.keyAt(i) + System.getProperty("line.separator") +
                        "Остаток: " + roundUp(tmp, 2).toString() + System.getProperty("line.separator") +
                        "Продажа: " + arrayMapQuantityReduced.valueAt(i).toString() + System.getProperty("line.separator") +
                        "Загрузка: " + arrayMapReceive.valueAt(i).toString() + System.getProperty("line.separator") +
                        System.getProperty("line.separator");
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
}
