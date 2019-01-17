package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateInvoiceSetItemsQuantitiesActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefItemsList, sPrefSalesPartner,
            sPrefConnectionStatus, sPrefAccountingType, sPrefItemName;
    String requestUrlFinalPrice = "https://caiman.ru.com/php/price.php", dbName, dbUser, dbPassword,
            salesPartner, connStatus, item;
    String[] itemPrice, discountType, discountValue;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_ItemsListToInvoice = "itemsToInvoice";
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_CONNSTATUS = "connectionStatus";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_ITEMNAME = "itemName";
    Double finalPrice;
//    ArrayList<String> myList;
    ArrayList<DataPrice> dataArray;
    TextView textViewSalesPartner, textViewItemName, textViewPrice, textViewAccountingType;
    EditText editTextQuantity, editTextExchange, editTextReturn;
    Button btnChangePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_set_items_quantities);

        dataArray = new ArrayList<>();
        btnChangePrice = findViewById(R.id.buttonChangePrice);
        btnChangePrice.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefItemsList = getSharedPreferences(SAVED_ItemsListToInvoice, Context.MODE_PRIVATE);
        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefItemName = getSharedPreferences(SAVED_ITEMNAME, Context.MODE_PRIVATE);

        textViewSalesPartner = findViewById(R.id.textViewSalesPartner);
        textViewItemName = findViewById(R.id.textViewItemName);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewAccountingType = findViewById(R.id.textViewAccountingType);

        salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");
        textViewSalesPartner.setText(salesPartner);
        textViewItemName.setText(sPrefItemName.getString(SAVED_ITEMNAME, ""));
        item = textViewItemName.getText().toString();
        textViewAccountingType.setText(sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, ""));

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)) {
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        }

//        myList = getStringArrayPref(getApplicationContext(), SAVED_ItemsListToInvoice);

        if (sPrefConnectionStatus.contains(SAVED_CONNSTATUS)) {
            connStatus = sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "");
            if (connStatus.equals("success")) {
                getPriceFromServerDB();
            } else {
                getPriceFromLocalDB();
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonChangePrice:
                Intent intent = new Intent(this, CreateInvoiceSetItemsQuantitiesActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonSaveTmp:
                saveTmp();
                break;
            default:
                break;
        }
    }

    public static ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    private void getPriceFromServerDB() {
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
                parameters.put("ItemName", item);
                parameters.put("SalesPartner", salesPartner);
                return parameters;
            }
        };
//        requestQueue.add(request);
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void getPriceFromLocalDB(){

    }

    private void saveTmp(){

    }
}
