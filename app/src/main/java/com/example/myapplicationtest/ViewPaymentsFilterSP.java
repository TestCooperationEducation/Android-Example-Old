package com.example.myapplicationtest;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ViewPaymentsFilterSP extends AppCompatActivity implements View.OnClickListener {

    TextView textViewDebtor;
    EditText editTextPaymentSum;
    Button btnNext, btnFilter, btnShowMore;
    ListView listViewDebts;
    ArrayAdapter<String> arrayAdapter;
    Boolean itemChecked = false;
    String[] rootMenu, salesPartnerNameList, areaList = {"1", "2", "3" ,"4"}, debtList;
    String menuItemChosen, areaChosen, dayOfTheWeekChosen, dayOfTheWeek, accountingTypeChosen = "непровод",
            salesPartnerChosen, debt, requestUrlMakePayment = "https://caiman.ru.com/php/makePayment.php",
            dbName, dbUser, dbPassword, agent;
    final String SAVED_DAYOFTHEWEEKDEFAULT = "DayOfTheWeekDefault";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_AGENT = "agent";
    SharedPreferences sPrefDayOfTheWeekDefault, sPrefAreaDefault, sPrefDBName, sPrefDBPassword,
            sPrefDBUser, sPrefAgent;
    DBHelper dbHelper;
    SQLiteDatabase db;
    final String LOG_TAG = "myLogs";
    ArrayList<String> salesPartnerNameListTmp, invoiceNumberList, dateTimeDocList, invoiceSumList,
            paymentSumList;
    ArrayList<Integer> debtsInvoiceNumberList;
    ArrayList<Double> debtsValueList;
    Integer debtInvoiceNumber;
    Double debtSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_payments_filter_sp);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        textViewDebtor = findViewById(R.id.textViewDebtor);
        editTextPaymentSum = findViewById(R.id.editTextPaymentSum);
        listViewDebts = findViewById(R.id.listViewDebts);

        btnNext = findViewById(R.id.buttonNext);
        btnNext.setOnClickListener(this);
        btnFilter = findViewById(R.id.buttonFilter);
        btnFilter.setOnClickListener(this);
        btnShowMore = findViewById(R.id.buttonShowMore);
        btnShowMore.setOnClickListener(this);

        sPrefDayOfTheWeekDefault = getSharedPreferences(SAVED_DAYOFTHEWEEKDEFAULT, Context.MODE_PRIVATE);
        sPrefAreaDefault  = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefAgent = getSharedPreferences(SAVED_AGENT, Context.MODE_PRIVATE);
        areaChosen = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        dayOfTheWeek = sPrefDayOfTheWeekDefault.getString(SAVED_DAYOFTHEWEEKDEFAULT, "");
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        agent = sPrefAgent.getString(SAVED_AGENT, "");

        if (dayOfTheWeek.equals("1") || dayOfTheWeek.equals("4")){
            dayOfTheWeekChosen = "понедельник-четверг";
        }
        if (dayOfTheWeek.equals("2") || dayOfTheWeek.equals("5")){
            dayOfTheWeekChosen = "вторник-пятница";
        }
        if (dayOfTheWeek.equals("3")){
            dayOfTheWeekChosen = "среда";
        }
        if (dayOfTheWeek.equals("6") || dayOfTheWeek.equals("7")){
            dayOfTheWeekChosen = "среда";
        }
        if (sPrefAreaDefault.getString(SAVED_AREADEFAULT, "").equals("4")){
            dayOfTheWeekChosen = "север";
        }
        if (sPrefAreaDefault.getString(SAVED_AREADEFAULT, "").equals("6")){
            dayOfTheWeekChosen = "среда";
            areaChosen = "2";
        }

        setMenuValues();

        listViewDebts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                debt = ((TextView) view).getText().toString();
                for (int i = 0; i < debtsInvoiceNumberList.size(); i++) {
                    if (debtList[i].equals(debt)) {
                        debtInvoiceNumber = debtsInvoiceNumberList.get(i);
                        debtSum = debtsValueList.get(i);
                        makePaymentPrompt();
                        Toast.makeText(getApplicationContext(), "Долг по накладной №: " + debtsInvoiceNumberList.get(i), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:

                break;
            case R.id.buttonFilter:
                rootMenu();
                break;
            case R.id.buttonShowMore:

                break;
            default:
                break;
        }
    }

    private void rootMenu(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите параметры")
                .setCancelable(false)
                .setNeutralButton("Назад",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        })
//                        .setNegativeButton("Удалить",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        if (itemChecked == true) {
//
//                                            Toast.makeText(getApplicationContext(), "<<< Запись " +
//                                                    sPListTmp[itemTmp] + " удалeна >>>", Toast.LENGTH_SHORT).show();
//                                            itemChecked = false;
//                                            dialog.cancel();
//                                        }
//                                    }
//                                })
                .setPositiveButton("Уйти",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        })
                .setSingleChoiceItems(rootMenu, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Вы выбрали: "
                                                + rootMenu[item],
                                        Toast.LENGTH_SHORT).show();
                                menuItemChosen = rootMenu[item];
                                setItemChosenValues();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setMenuValues() {
        rootMenu = new String[4];
        rootMenu[0] = "Контрагент";
        rootMenu[1] = "Маршрут";
        rootMenu[2] = "Учет";
        rootMenu[3] = "Район";
    }

    private void setItemChosenValues() {
        if (menuItemChosen.equals("Контрагент")) {
            salesPartnerNameListTmp = new ArrayList<>();
            loadSalesPartners();
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Контрагенты")
                    .setCancelable(false)
                    .setNeutralButton("Назад",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    rootMenu();
                                    dialog.cancel();
                                }
                            })
                        .setNegativeButton("Выполнить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (itemChecked == true) {
                                            loadDebts();
                                            dialog.cancel();
                                        }
                                    }
                                })
                    .setPositiveButton("Уйти",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    dialog.cancel();
                                }
                            })
                    .setSingleChoiceItems(salesPartnerNameList, -1,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int item) {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Вы выбрали: "
                                                    + salesPartnerNameList[item],
                                            Toast.LENGTH_SHORT).show();
                                    salesPartnerChosen = salesPartnerNameList[item];
                                    itemChecked = true;
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        if (menuItemChosen.equals("Маршрут")) {
            final String[] root = {"понедельник-четверг", "вторник-пятница", "среда", "север"};
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Выберите маршрут")
                    .setCancelable(false)
                    .setNeutralButton("Назад",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    rootMenu();
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton("Уйти",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    dialog.cancel();
                                }
                            })
                    .setSingleChoiceItems(root, -1,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int item) {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Вы выбрали: "
                                                    + root[item],
                                            Toast.LENGTH_SHORT).show();
                                    dayOfTheWeekChosen = root[item];
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        if (menuItemChosen.equals("Учет")) {
            final String[] accountingType = {"провод", "непровод"};
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Выберите тип учета")
                    .setCancelable(false)
                    .setNeutralButton("Назад",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    rootMenu();
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton("Уйти",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    dialog.cancel();
                                }
                            })
                    .setSingleChoiceItems(accountingType, -1,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int item) {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Вы выбрали: "
                                                    + accountingType[item],
                                            Toast.LENGTH_SHORT).show();
                                    accountingTypeChosen = accountingType[item];
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        if (menuItemChosen.equals("Район")) {
            final String[] areaListTmp = {"Район №1", "Район №2", "Район №3", "Район №4"};
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Выберите район")
                    .setCancelable(false)
                    .setNeutralButton("Назад",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    rootMenu();
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton("Уйти",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    dialog.cancel();
                                }
                            })
                    .setSingleChoiceItems(areaListTmp, -1,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int item) {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Вы выбрали: "
                                                    + areaListTmp[item],
                                            Toast.LENGTH_SHORT).show();
                                    areaChosen = areaList[item];
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void loadSalesPartners() {
        String sql = "SELECT Наименование FROM salesPartners WHERE DayOfTheWeek LIKE ? AND Район LIKE ? AND Учет LIKE ?";
        Cursor c = db.rawQuery(sql, new String[]{dayOfTheWeekChosen, areaChosen, accountingTypeChosen});
        if (c.moveToFirst()) {
            int salesPartnerNameTmp = c.getColumnIndex("Наименование");
            do {
                salesPartnerNameListTmp.add(c.getString(salesPartnerNameTmp));
            } while (c.moveToNext());
        }
        c.close();
        salesPartnerNameList = new String[salesPartnerNameListTmp.size()];
        for (int i = 0; i < salesPartnerNameListTmp.size(); i++){
            salesPartnerNameList[i] = salesPartnerNameListTmp.get(i);
        }
    }

    private void loadDebts(){
        invoiceNumberList = new ArrayList<>();
        dateTimeDocList = new ArrayList<>();
        invoiceSumList = new ArrayList<>();
        paymentSumList = new ArrayList<>();

        String sql = "SELECT DISTINCT invoice.InvoiceNumber, invoice.DateTimeDoc, invoice.InvoiceSum FROM invoice INNER JOIN salesPartners " +
                "ON invoice.SalesPartnerID LIKE salesPartners.serverDB_ID " +
                "WHERE Наименование LIKE ? AND AgentID LIKE ? AND AccountingType LIKE ? ";
        Cursor c = db.rawQuery(sql, new String[]{salesPartnerChosen, areaChosen, accountingTypeChosen});
        if (c.moveToFirst()) {
            int invoiceNumberTmp = c.getColumnIndex("InvoiceNumber");
            int dateTimeDocTmp = c.getColumnIndex("DateTimeDoc");
            int invoiceSumTmp = c.getColumnIndex("InvoiceSum");
            do {
                invoiceNumberList.add(c.getString(invoiceNumberTmp));
                dateTimeDocList.add(c.getString(dateTimeDocTmp));
                invoiceSumList.add(c.getString(invoiceSumTmp));

            } while (c.moveToNext());
            textViewDebtor.setText(salesPartnerChosen);
        }
        c.close();

        for (int i = 0; i < invoiceNumberList.size(); i++){
            String tmpPaymentSum = "0";
            sql = "SELECT сумма_внесения FROM paymentsServer WHERE InvoiceNumber LIKE ?";
            c = db.rawQuery(sql, new String[]{invoiceNumberList.get(i)});
            if (c.moveToFirst()) {
                int paymentSumTmp = c.getColumnIndex("сумма_внесения");
                do {
                    tmpPaymentSum = String.valueOf(Double.parseDouble(tmpPaymentSum) + Double.parseDouble(c.getString(paymentSumTmp)));
                } while (c.moveToNext());
            } else {
                tmpPaymentSum = "0";
            }
            c.close();
            paymentSumList.add(tmpPaymentSum);
        }


        debtsValueList = new ArrayList<>();

        debtsInvoiceNumberList = new ArrayList<>();
        ArrayList<String> debtsDatesList;
        debtsDatesList = new ArrayList<>();
        ArrayList<Double> debtsInvoiceSumList;
        debtsInvoiceSumList = new ArrayList<>();

        for (int i = 0; i < invoiceNumberList.size(); i++){
            if (!paymentSumList.get(i).equals("0")){
                if (Double.parseDouble(paymentSumList.get(i)) < Double.parseDouble(invoiceSumList.get(i))){
                    debtsValueList.add(Double.parseDouble(invoiceSumList.get(i)) - Double.parseDouble(paymentSumList.get(i)));
                    debtsInvoiceNumberList.add(Integer.parseInt(invoiceNumberList.get(i)));
                    debtsDatesList.add(dateTimeDocList.get(i));
                    debtsInvoiceSumList.add(Double.parseDouble(invoiceSumList.get(i)));
                }
            } else {
                debtsValueList.add(Double.parseDouble(invoiceSumList.get(i)));
                debtsInvoiceNumberList.add(Integer.parseInt(invoiceNumberList.get(i)));
                debtsDatesList.add(dateTimeDocList.get(i));
                debtsInvoiceSumList.add(Double.parseDouble(invoiceSumList.get(i)));
            }
        }

        debtList = new String[debtsValueList.size()];
        for (int i = 0; i < debtsInvoiceNumberList.size(); i++){
            debtList[i] = "№" + String.valueOf(debtsInvoiceNumberList.get(i))
                    + " Дата: " + String.valueOf(debtsDatesList.get(i))
                    + " Сумма накладной: " + String.valueOf(debtsInvoiceSumList.get(i))
                    + " Долг: " + String.valueOf(debtsValueList.get(i));
        }

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, debtList);
        listViewDebts.setAdapter(arrayAdapter);
    }

    private void makePaymentPrompt(){
        if (editTextPaymentSum.getText().toString().trim().length() == 0){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Внести всю сумму?")
                    .setMessage("Долг по накладной: " + debtSum.toString())
                    .setCancelable(true)
                    .setPositiveButton("Внести",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    makePayment(debtSum.toString());
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            if (Double.parseDouble(editTextPaymentSum.getText().toString()) < debtSum){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Внимание")
                        .setMessage("Сумма внесения меньше накладной")
                        .setCancelable(true)
                        .setPositiveButton("Внести",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        makePayment(editTextPaymentSum.getText().toString());
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            if (Double.parseDouble(editTextPaymentSum.getText().toString()) > debtSum){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Внимание")
                        .setMessage("Сумма внесения больше накладной")
                        .setCancelable(true)
                        .setPositiveButton("Назад",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void makePayment(String payment){
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy/MM/dd HH:mm:ss" );
        final String output = zdt.format( formatter );

        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, "--- Insert in payments: ---");
        cv.put("DateTimeDoc", output);
        cv.put("InvoiceNumber", debtInvoiceNumber);
        cv.put("сумма_внесения", payment);
        cv.put("Автор", agent);
        long rowID = db.insert("payments", null, cv);
        Log.d(LOG_TAG, "row inserted, ID = " + rowID);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Успешно")
                .setMessage("Деньги внесены")
                .setCancelable(false)
                .setNegativeButton("Уйти",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Назад",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

//        DataPay dt = new DataPay(Integer.parseInt(invoiceNumber), Double.parseDouble(editTextPaymentSum.getText().toString()));
//        dataPay.add(dt);
//
//        Gson gson = new Gson();
//        final String newDataArray = gson.toJson(dataPay);
//
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        StringRequest request = new StringRequest(Request.Method.POST,
//                requestUrlMakePayment, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.d("response", "result: " + response);
//                dataPay.clear();
//                try{
//                    JSONArray jsonArray = new JSONArray(response);
//                    String[] invoiceNumberFromRequest = new String[jsonArray.length()];
//                    String[] paymentIDFromRequest = new String[jsonArray.length()];
//                    String[] status = new String[jsonArray.length()];
//                    String tmpStatus = "";
//
//                    ContentValues cv = new ContentValues();
//                    Log.d(LOG_TAG, "--- Insert in syncedPayments: ---");
//
//                    if (jsonArray.length() > 0){
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject obj = jsonArray.getJSONObject(i);
//                            invoiceNumberFromRequest[i] = obj.getString("invoiceNumber");
//                            paymentIDFromRequest[i] = obj.getString("paymentID");
//                            status[i] = obj.getString("status");
//                            if (status[i].equals("Бабло внесено")) {
//                                tmpStatus = "Yes";
//                            }
//
//                            cv.put("invoiceNumber", Integer.parseInt(invoiceNumberFromRequest[i]));
//                            cv.put("paymentID", paymentIDFromRequest[i]);
//                            cv.put("agentID", areaDefault);
//                            cv.put("dateTimeDoc", output);
//                            long rowID = db.insert("syncedPayments", null, cv);
//                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                        }
//                        if (tmpStatus.equals("Yes")){
//                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
//                            builder.setTitle("Успешно")
//                                    .setMessage("Деньги внесены и синхронизированы")
//                                    .setCancelable(false)
//                                    .setPositiveButton("Назад",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    finish();
//                                                    dialog.cancel();
//                                                }
//                                            });
//                            AlertDialog alert = builder.create();
//                            alert.show();
//                        }
//                        Toast.makeText(getApplicationContext(), "<<< Платеж Синхронизирован >>>", Toast.LENGTH_SHORT).show();
//                    }else{
//                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                catch (JSONException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener(){
//            @Override
//            public void onErrorResponse(VolleyError error){
//                onConnectionFailedPayment();
//                Toast.makeText(getApplicationContext(), "Нет ответа от Сервера", Toast.LENGTH_SHORT).show();
//                Log.e("TAG", "Error " + error.getMessage());
//            }
//        }){
//            @Override
//            protected Map<String, String> getParams(){
//                Map<String, String> parameters = new HashMap<>();
//                parameters.put("dbName", dbName);
//                parameters.put("dbUser", dbUser);
//                parameters.put("dbPassword", dbPassword);
//                parameters.put("agent", agent);
//                parameters.put("agentID", areaDefault);
//                parameters.put("array", newDataArray);
//                return parameters;
//            }
//        };
//        VolleySingleton.getInstance(this).getRequestQueue().add(request);
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
