package com.example.myapplicationtest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

public class CreateInvoiceMainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnAddItem, btnReceivePrice, btnRemove, btnSaveRecord, btnPay;
    ArrayList<String> arrItems, arrTotal;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrPrice, arrSum;
    Integer iteration, itemsCheck;
    Double finalPrice, tmpQuantityType, tmpExchangeType, tmpReturnType, paymentAmount, invoiceSum;
    String[] itemPrice, discountValue, discountType;
    String requestUrl = "https://caiman.ru.com/php/items.php", dbName, dbUser, dbPassword,
            accountingType, salesPartner, items, area, dayOfTheWeek, author, statusSave, statusPay,
            requestUrlFinalPrice = "https://caiman.ru.com/php/price.php",
            requestUrlSaveRecord = "https://caiman.ru.com/php/saveNewInvoice.php",
            requestUrlMakePayment = "https://caiman.ru.com/php/makePayment.php", loginSecurity, invoiceNumber;
    ListView listViewItems, listViewItemsTotal;
    EditText editTextQuantity, editTextExchange, editTextReturn, editTextPaymentAmount;
    TextView textViewAccountingType, textViewSalesPartner, textViewPrice, textViewDiscountValue,
            textViewDiscountType, textViewTotalSum, textViewStatusSave, textViewStatusPay;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefAccountingType,
            sPrefSalesPartner, sPrefLogin, sPrefArea, sPrefDayOfTheWeek;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_LOGIN = "Login";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_AREA = "Area";
    final String SAVED_DayOfTheWeek = "DayOfTheWeek";
    List<DataInvoice> dataArray;
    List<DataPay> dataPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_main);

        dataArray = new ArrayList<>();
        dataPay = new ArrayList<>();
        iteration = -1;
        btnAddItem = findViewById(R.id.buttonAddItem);
        btnAddItem.setOnClickListener(this);
        btnReceivePrice = findViewById(R.id.buttonReceivePrice);
        btnReceivePrice.setOnClickListener(this);
        btnRemove = findViewById(R.id.buttonRemove);
        btnRemove.setOnClickListener(this);
        btnSaveRecord = findViewById(R.id.buttonSaveRecord);
        btnSaveRecord.setOnClickListener(this);
        btnPay = findViewById(R.id.buttonPay);
        btnPay.setOnClickListener(this);

        arrItems = new ArrayList<>();
        arrTotal = new ArrayList<>();
        arrQuantity = new ArrayList<>();
        arrExchange = new ArrayList<>();
        arrReturn = new ArrayList<>();
        arrPrice = new ArrayList<>();
        arrSum = new ArrayList<>();

        listViewItems = findViewById(R.id.listViewItems);
        listViewItemsTotal = findViewById(R.id.listViewItemsTotal);
        textViewSalesPartner = findViewById(R.id.textViewSalesPartner);
        textViewAccountingType = findViewById(R.id.textViewAccountingType);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        editTextExchange = findViewById(R.id.editTextExchange);
        editTextReturn = findViewById(R.id.editTextReturn);
        editTextPaymentAmount = findViewById(R.id.editTextPaymentAmount);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewDiscountType = findViewById(R.id.textViewDiscountType);
        textViewDiscountValue = findViewById(R.id.textViewDiscountValue);
        textViewTotalSum = findViewById(R.id.textViewTotalSum);
        textViewTotalSum.setText("0");
        textViewStatusSave = findViewById(R.id.textViewStatusSave);
        textViewStatusPay = findViewById(R.id.textViewStatusPay);

//        requestQueue = Volley.newRequestQueue((getApplicationContext()));

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);
        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DayOfTheWeek, Context.MODE_PRIVATE);

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)
                && sPrefSalesPartner.contains(SAVED_SALESPARTNER) && sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)
                && sPrefArea.contains(SAVED_AREA) ){
//            && sPrefDayOfTheWeek.contains(SAVED_DayOfTheWeek)

            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
            accountingType = sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, "");
            salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");
            area = sPrefArea.getString(SAVED_AREA, "");
//            dayOfTheWeek = sPrefSalesPartner.getString(SAVED_DayOfTheWeek, "");
            loginSecurity = sPrefLogin.getString(SAVED_LOGIN, "");
        }

        Intent intent = getIntent();
        String agentName = intent.getStringExtra(CreateInvoiceFilterSecondActivity.EXTRA_AGENTNAMENEXT);
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);
        author = agentName;
//        agentNameGlobal = intent.getStringExtra(CreateInvoiceFilterSecondActivity.EXTRA_AGENTNAMENEXT);

        textViewSalesPartner.setText(salesPartner);
        textViewAccountingType.setText(accountingType);

        receiveItemsList();

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                items = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Selected Item :" + items, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void receiveItemsList(){
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Toast.makeText(getApplicationContext(), "Query successful", Toast.LENGTH_SHORT).show();
                    String[] itemsList = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemsList[i] = obj.getString("Наименование");
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Something went wrong with DB query", Toast.LENGTH_SHORT).show();
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, itemsList);
                    listViewItems.setAdapter(arrayAdapter);
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Response Error, fuck!", Toast.LENGTH_SHORT).show();
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

    private void receivePrice(){
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlFinalPrice, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Toast.makeText(getApplicationContext(), "Query successful", Toast.LENGTH_SHORT).show();
                    itemPrice = new String[jsonArray.length()];
                    discountType = new String[jsonArray.length()];
                    discountValue = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemPrice[i] = obj.getString("Цена");
//                            arrPrice.add(Double.parseDouble(itemPrice[0]));
                            if (obj.isNull("Скидка") && obj.isNull("Тип_скидки")) {
                                discountValue[i] = String.valueOf(0);
                                discountType[i] = String.valueOf(0);
                                Toast.makeText(getApplicationContext(), "Нет", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                discountValue[i] = obj.getString("Скидка");
                                discountType[i] = obj.getString("Тип_скидки");
                                Toast.makeText(getApplicationContext(), "Есть", Toast.LENGTH_SHORT).show();
                            }
                        }
                        textViewPrice.setText(itemPrice[0]);
                        textViewDiscountType.setText(discountType[0]);
                        textViewDiscountValue.setText(discountValue[0]);
                        if (Double.parseDouble(discountType[0]) == 0){
                            textViewPrice.setText(itemPrice[0]);
                            finalPrice = Double.parseDouble(textViewPrice.getText().toString());
                        }
                        if (Double.parseDouble(discountType[0]) == 1){
                            finalPrice = Double.parseDouble(itemPrice[0]) - Double.parseDouble(discountValue[0]);
                            textViewPrice.setText(finalPrice.toString());
                        }
                        if (Double.parseDouble(discountType[0]) == 2){
                            finalPrice = Double.parseDouble(itemPrice[0]) - (Double.parseDouble(itemPrice[0]) / 10);
                            textViewPrice.setText(finalPrice.toString());
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Something went wrong with DB query", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getApplicationContext(), textViewPrice.getText().toString(), Toast.LENGTH_SHORT).show();
//                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, itemsList);
//                    listViewItems.setAdapter(arrayAdapter);
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Response Error, fuck!", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("ItemName", items);
                parameters.put("SalesPartner", salesPartner);
                return parameters;
            }
        };
//        requestQueue.add(request);
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAddItem:
                addItem();
                break;
            case R.id.buttonReceivePrice:
                receivePrice();
                break;
            case R.id.buttonRemove:
                delItem();
                break;
            case R.id.buttonSaveRecord:
                saveRecordPrompt();
                break;
            case R.id.buttonPay:
                makePaymentPrompt();
                break;
            default:
                break;
        }
    }

    private void addItem(){
        if (arrItems.size() > 0 && !TextUtils.isEmpty(items)) {
            if (arrItems.contains(items)){
                Toast.makeText(getApplicationContext(), "Этот товар уже есть в списке", Toast.LENGTH_SHORT).show();
                itemsCheck = 1;
                textViewPrice.setText("Цена");
                textViewDiscountType.setText("Тип скидки");
                textViewDiscountValue.setText("Скидка");
            } else {
                itemsCheck = 0;
            }
        }
        if (arrItems.size() == 0) {
            itemsCheck = 0;
        }
        if (!TextUtils.isEmpty(items) && !textViewPrice.getText().toString().equals("Цена") && itemsCheck == 0){
            if (editTextQuantity.getText().toString().trim().length() == 0) {
                editTextQuantity.setText("0");
            }
            if (editTextExchange.getText().toString().trim().length() == 0) {
                editTextExchange.setText("0");
            }
            if (editTextReturn.getText().toString().trim().length() == 0) {
                editTextReturn.setText("0");
            }
            if (editTextQuantity.getText().toString().equals("0")
                    && editTextExchange.getText().toString().equals("0")
                    && editTextReturn.getText().toString().equals("0")) {
                Toast.makeText(getApplicationContext(), "Все не может быть пустым!", Toast.LENGTH_SHORT).show();
                items = null;
            } else {
                if (Double.parseDouble(editTextQuantity.getText().toString()) >= 1) {
                    tmpQuantityType = Double.parseDouble(editTextQuantity.getText().toString())
                            % Math.floor(Double.parseDouble(editTextQuantity.getText().toString()));
                    if (tmpQuantityType > 0) {
                        tmpQuantityType = 1.5d;
                    } else {
                        tmpQuantityType = 1d;
                    }
                }
                if (Double.parseDouble(editTextQuantity.getText().toString()) < 1 &&
                        Double.parseDouble(editTextQuantity.getText().toString()) > 0) {
                    tmpQuantityType = 1.5d;
                }
                if (Double.parseDouble(editTextQuantity.getText().toString()) == 0) {
                    tmpQuantityType = 1d;
                }
                if (Double.parseDouble(editTextExchange.getText().toString()) >= 1) {
                    tmpExchangeType = Double.parseDouble(editTextExchange.getText().toString())
                            % Math.floor(Double.parseDouble(editTextExchange.getText().toString()));
                    if (tmpExchangeType > 0) {
                        tmpExchangeType = 1.5d;
                    } else {
                        tmpExchangeType = 1d;
                    }
                }
                if (Double.parseDouble(editTextExchange.getText().toString()) < 1 &&
                        Double.parseDouble(editTextExchange.getText().toString()) > 0) {
                    tmpExchangeType = 1.5d;
                }
                if (Double.parseDouble(editTextExchange.getText().toString()) == 0) {
                    tmpExchangeType = 1d;
                }
                if (Double.parseDouble(editTextReturn.getText().toString()) >= 1) {
                    tmpReturnType = Double.parseDouble(editTextReturn.getText().toString())
                            % Math.floor(Double.parseDouble(editTextReturn.getText().toString()));
                    if (tmpReturnType > 0) {
                        tmpReturnType = 1.5d;
                    } else {
                        tmpReturnType = 1d;
                    }
                }
                if (Double.parseDouble(editTextReturn.getText().toString()) < 1 &&
                        Double.parseDouble(editTextReturn.getText().toString()) > 0) {
                    tmpReturnType = 1.5d;
                }
                if (Double.parseDouble(editTextReturn.getText().toString()) == 0) {
                    tmpReturnType = 1d;
                }
                if (tmpQuantityType % Math.floor(tmpQuantityType) > 0
                        || tmpExchangeType % Math.floor(tmpExchangeType) > 0
                        || tmpReturnType % Math.floor(tmpReturnType) > 0) {
                    if (items.equals("Ким-ча весовая") || items.equals("Редька по-восточному весовая")) {
                        iteration = iteration + 1;
                        arrItems.add(items);
                        arrPrice.add(Double.parseDouble(textViewPrice.getText().toString()));
                        arrQuantity.add(Double.parseDouble(editTextQuantity.getText().toString()));
                        arrExchange.add(Double.parseDouble(editTextExchange.getText().toString()));
                        arrReturn.add(Double.parseDouble(editTextReturn.getText().toString()));
                        arrSum.add((double) Math.round(Double.parseDouble(textViewPrice.getText().toString())
                                * arrQuantity.get(iteration)));
//                        arrSum.add(Double.parseDouble(String.format("%.3g%n", Double.parseDouble(textViewPrice.getText().toString())
//                                * arrQuantity.get(iteration))));

                        Double tmp = (arrSum.get(iteration) + Double.parseDouble(textViewTotalSum.getText().toString()));
                        textViewTotalSum.setText(tmp.toString());

                        arrTotal.add((iteration + 1) + ". " + arrItems.get(iteration)
                                + " || Цена: " + textViewPrice.getText().toString()
                                + " || Кол-во: " + arrQuantity.get(iteration)
                                + " || Сумма: " + arrSum.get(iteration)
                                + " || Обмен: " + arrExchange.get(iteration)
                                + " || Возврат: " + arrReturn.get(iteration));

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrTotal);
                        listViewItemsTotal.setAdapter(arrayAdapter);
                        items = null;
                        textViewPrice.setText("Цена");
                        textViewDiscountType.setText("Тип скидки");
                        textViewDiscountValue.setText("Скидка");
                    } else {
                        Toast.makeText(getApplicationContext(), "Этот товар продается только целыми упаковками!", Toast.LENGTH_SHORT).show();
                        items = null;
                        textViewPrice.setText("Цена");
                        textViewDiscountType.setText("Тип скидки");
                        textViewDiscountValue.setText("Скидка");
                    }
                }
                if (tmpQuantityType % Math.floor(tmpQuantityType) == 0
                        && tmpExchangeType % Math.floor(tmpExchangeType) == 0
                        && tmpReturnType % Math.floor(tmpReturnType) == 0) {
//                    Toast.makeText(getApplicationContext(), "Бля3", Toast.LENGTH_SHORT).show();
                    iteration = iteration + 1;
                    arrItems.add(items);
                    arrPrice.add(Double.parseDouble(textViewPrice.getText().toString()));
                    arrQuantity.add(Double.parseDouble(editTextQuantity.getText().toString()));
                    arrExchange.add(Double.parseDouble(editTextExchange.getText().toString()));
                    arrReturn.add(Double.parseDouble(editTextReturn.getText().toString()));
                    arrSum.add((double) Math.round(Double.parseDouble(textViewPrice.getText().toString())
                            * arrQuantity.get(iteration)));

                    Double tmp = (arrSum.get(iteration) + Double.parseDouble(textViewTotalSum.getText().toString()));
                    textViewTotalSum.setText(tmp.toString());
                    invoiceSum = tmp;

                    arrTotal.add((iteration + 1) + ". " + arrItems.get(iteration)
                            + " || Цена: " + textViewPrice.getText().toString()
                            + " || Кол-во: " + arrQuantity.get(iteration)
                            + " || Сумма: " + arrSum.get(iteration)
                            + " || Обмен: " + arrExchange.get(iteration)
                            + " || Возврат: " + arrReturn.get(iteration));

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrTotal);
                    listViewItemsTotal.setAdapter(arrayAdapter);
                    items = null;
                    textViewPrice.setText("Цена");
                    textViewDiscountType.setText("Тип скидки");
                    textViewDiscountValue.setText("Скидка");


                }
            }
        }
    }

    private void delItem(){
        if (arrTotal.size() > 0){
            Double tmpSum = Double.parseDouble(textViewTotalSum.getText().toString()) - arrSum.get(arrSum.size() - 1);
            textViewTotalSum.setText(tmpSum.toString());
            arrTotal.remove(arrTotal.size() - 1);
            arrItems.remove(arrItems.size() - 1);
            arrQuantity.remove(arrQuantity.size() - 1);
            arrSum.remove(arrSum.size() - 1);
            arrExchange.remove(arrExchange.size() - 1);
            arrReturn.remove(arrReturn.size() -1);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrTotal);
            listViewItemsTotal.setAdapter(arrayAdapter);
            iteration = iteration - 1;
            items = null;
            textViewPrice.setText("Цена");
            textViewDiscountType.setText("Тип скидки");
            textViewDiscountValue.setText("Скидка");
            editTextQuantity.setText("");
            editTextExchange.setText("");
            editTextReturn.setText("");
        }
    }

    private void saveRecordPrompt() {
        if (statusSave != "Сохранено") {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Сохранение накладной")
                    .setMessage("Вы собираетесь сохранить накладную!!!")
                    .setCancelable(true)
                    .setNegativeButton("Да, я хочу сохранить накладную",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    saveRecord();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        if (statusSave == "Сохранено") {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Внимание")
                    .setMessage("Вы уже сохранили этот документ!")
                    .setCancelable(true)
                    .setNegativeButton("Я всё понял",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    private void saveRecord(){
        invoiceSum = Double.parseDouble(textViewTotalSum.getText().toString());
        for (int i = 0; i < arrTotal.size(); i ++){
            DataInvoice dt = new DataInvoice(salesPartner, accountingType, arrItems.get(i),
                    arrPrice.get(i), arrQuantity.get(i), arrSum.get(i), arrExchange.get(i),
                    arrReturn.get(i), invoiceSum);
            dataArray.add(dt);
        }
        Gson gson = new Gson();
        final String newDataArray = gson.toJson(dataArray);

        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlSaveRecord, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", "result: " + response);
                invoiceNumber = response;
                Toast.makeText(getApplicationContext(), "Номер накладной: " + invoiceNumber, Toast.LENGTH_SHORT).show();
                dataArray.clear();
                if (invoiceNumber.matches("-?\\d+")) {
                    Toast.makeText(getApplicationContext(), "Документ сохранён", Toast.LENGTH_SHORT).show();
                    statusSave = "Сохранено";
                    textViewStatusSave.setText(statusSave);
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Сообщите об этой ошибке. Код 001", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("area", area);
                parameters.put("accountingType", accountingType);
                parameters.put("loginSecurity", loginSecurity);
//                parameters.put("dayOfTheWeek", dayOfTheWeek);
                parameters.put("array", newDataArray);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void makePaymentPrompt(){
        if (statusPay != "Оплачено") {
            if (statusSave != "Сохранено") {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Внимание")
                        .setMessage("Вы не сохранили документ!")
                        .setCancelable(true)
                        .setNegativeButton("Я всё понял",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            if (statusSave == "Сохранено") {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Внесение денег")
                        .setMessage("Убедитесь, что вы взяли деньги!")
                        .setCancelable(true)
                        .setNegativeButton("Да, я хочу внести деньги",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        checkPaymentPrompt();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Внимание")
                    .setMessage("Документ закрыт для изменений")
                    .setCancelable(true)
                    .setNegativeButton("Я всё понял",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void checkPaymentPrompt(){
        if (editTextPaymentAmount.getText().toString().trim().length() == 0
                || Double.parseDouble(editTextPaymentAmount.getText().toString())
                == Double.parseDouble(textViewTotalSum.getText().toString())) {
            paymentAmount = Double.parseDouble(textViewTotalSum.getText().toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Проверьте сумму")
                    .setMessage("Хотите внести всю сумму?")
                    .setCancelable(true)
                    .setNegativeButton("Да, я хочу внести всю сумму",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    makePayment();
//                                dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            paymentAmount = Double.parseDouble(editTextPaymentAmount.getText().toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Проверьте сумму")
                    .setMessage("Сумма внесения отличается от суммы накладной!")
                    .setCancelable(true)
                    .setNegativeButton("Да, все равно внести",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    makePayment();
//                                dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void makePayment(){
        DataPay dt = new DataPay(paymentAmount);
        dataPay.add(dt);

        Gson gson = new Gson();
        final String newDataArray = gson.toJson(dataPay);

        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlMakePayment, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", "result: " + response);
                dataPay.clear();
                if (response.equals("Бабло внесено")) {
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                    if (paymentAmount == Double.parseDouble(textViewTotalSum.getText().toString())){
                        textViewStatusPay.setText("Оплачено полностью");
                        statusPay = "Оплачено";
                    } else {
                        textViewStatusPay.setText("Оплачено частично");
                        statusPay = "Оплачено";
                    }
                    clearAll();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Сообщите об этой ошибке. Код 002", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("invoiceNumber", invoiceNumber);
                parameters.put("author", author);
                parameters.put("paymentAmount", newDataArray);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void clearAll(){
        while (arrTotal.size() > 0){
            Double tmpSum = Double.parseDouble(textViewTotalSum.getText().toString()) - arrSum.get(arrSum.size() - 1);
            textViewTotalSum.setText(tmpSum.toString());
            arrTotal.remove(arrTotal.size() - 1);
            arrItems.remove(arrItems.size() - 1);
            arrQuantity.remove(arrQuantity.size() - 1);
            arrSum.remove(arrSum.size() - 1);
            arrExchange.remove(arrExchange.size() - 1);
            arrReturn.remove(arrReturn.size() -1);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrTotal);
            listViewItemsTotal.setAdapter(arrayAdapter);
            iteration = iteration - 1;
            items = null;
            textViewPrice.setText("Цена");
            textViewDiscountType.setText("Тип скидки");
            textViewDiscountValue.setText("Скидка");
            editTextQuantity.setText("");
            editTextExchange.setText("");
            editTextReturn.setText("");
        }
    }

}
