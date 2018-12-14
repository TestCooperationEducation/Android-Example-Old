package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateInvoiceMainActivity extends AppCompatActivity implements View.OnClickListener {

    RequestQueue requestQueue;
    Button btnAddItem, btnReceivePrice;
    ArrayList<String> arrItems, arrTotal;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrPrice;
    Integer iteration;
    String[] itemPrice, discountValue, discountType;
    String requestUrl = "https://caiman.ru.com/php/items.php", dbName, dbUser, dbPassword,
            accountingType, salesPartner, items,
            requestUrlFinalPrice = "https://caiman.ru.com/php/price.php";
    ListView listViewItems, listViewItemsTotal;
    EditText editTextQuantity, editTextExchange, editTextReturn;
    TextView textViewAccountingType, textViewSalesPartner, textViewPrice;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefAccountingType,
            sPrefSalesPartner;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_SALESPARTNER = "SalesPartner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_main);

        iteration = -1;
        btnAddItem = findViewById(R.id.buttonAddItem);
        btnAddItem.setOnClickListener(this);
        btnReceivePrice = findViewById(R.id.buttonReceivePrice);
        btnReceivePrice.setOnClickListener(this);

        arrItems = new ArrayList<>();
        arrTotal = new ArrayList<>();
        arrQuantity = new ArrayList<>();
        arrExchange = new ArrayList<>();
        arrReturn = new ArrayList<>();
        arrPrice = new ArrayList<>();

        requestQueue = Volley.newRequestQueue((getApplicationContext()));
        listViewItems = findViewById(R.id.listViewItems);
        listViewItemsTotal = findViewById(R.id.listViewItemsTotal);
        textViewSalesPartner = findViewById(R.id.textViewSalesPartner);
        textViewAccountingType = findViewById(R.id.textViewAccountingType);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        editTextExchange = findViewById(R.id.editTextExchange);
        editTextReturn = findViewById(R.id.editTextReturn);
        textViewPrice = findViewById(R.id.textViewPrice);

        requestQueue = Volley.newRequestQueue((getApplicationContext()));

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)
                && sPrefSalesPartner.contains(SAVED_SALESPARTNER) && sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)){
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
            accountingType = sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, "");
            salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");
        }

        Intent intent = getIntent();
        String agentName = intent.getStringExtra(CreateInvoiceFilterSecondActivity.EXTRA_AGENTNAMENEXT);
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);

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
        requestQueue.add(request);
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
                            if (obj.getString("Скидка").length() == 0
                                    && obj.getString("Тип_скидки").length() == 0) {
                                discountValue[i] = String.valueOf(0);
                                discountType[i] = String.valueOf(0);
                                Toast.makeText(getApplicationContext(), "Нет", Toast.LENGTH_SHORT).show();
                            }
                            if (obj.getString("Скидка").length() > 0
                                    && obj.getString("Тип_скидки").length() > 0) {
                                discountValue[i] = obj.getString("Скидка");
                                discountType[i] = obj.getString("Тип_скидки");
                                Toast.makeText(getApplicationContext(), "Есть", Toast.LENGTH_SHORT).show();
                            }
                        }
                        textViewPrice.setText(itemPrice[0]);
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
        requestQueue.add(request);
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
            default:
                break;
        }
    }

    private void addItem(){
        if (!TextUtils.isEmpty(items) && editTextQuantity.getText().toString().trim().length() > 0
                && editTextExchange.getText().toString().trim().length() > 0
                && editTextReturn.getText().toString().trim().length() > 0){
            iteration = iteration + 1;
            arrItems.add(items);
//            arrPrice.add(Double.parseDouble(itemPrice[0]));
//            Toast.makeText(getApplicationContext(), itemPrice[0], Toast.LENGTH_SHORT).show();
            arrQuantity.add(Double.parseDouble(editTextQuantity.getText().toString()));
            arrExchange.add(Double.parseDouble(editTextExchange.getText().toString()));
            arrReturn.add(Double.parseDouble(editTextReturn.getText().toString()));

            arrTotal.add((iteration + 1) + ". " + arrItems.get(iteration)
                    + " || Цена: " + textViewPrice.getText().toString()
                    + " || Кол-во: " + arrQuantity.get(iteration)
//                    + " || Сумма: " + (arrQuantity.get(iteration) * arrPrice.get(iteration))
                    + " || Обмен: " + arrExchange.get(iteration) + " || Возврат: " + arrReturn.get(iteration));
        }
//        String[] tmpItemsList = new String[ar.size()];
//        for (int iteration = 0; iteration < ar.size(); iteration ++ ) {
//            tmpItemsList[iteration] = ar.get(iteration);
//        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrTotal);
        listViewItemsTotal.setAdapter(arrayAdapter);
    }
}
