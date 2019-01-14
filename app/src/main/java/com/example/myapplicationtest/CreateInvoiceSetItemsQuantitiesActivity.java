package com.example.myapplicationtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

public class CreateInvoiceSetItemsQuantitiesActivity extends AppCompatActivity {

    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefItemsList, sPrefSalesPartner  ;
    String requestUrlFinalPrice = "https://caiman.ru.com/php/price.php", dbName, dbUser, dbPassword,
            salesPartner;
    String[] items;
    ListView listViewTest;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_ItemsListToInvoice = "itemsToInvoice";
    final String SAVED_SALESPARTNER = "SalesPartner";
    ArrayList<String> myList;
    ArrayList<DataPrice> dataArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_set_items_quantities);

        dataArray = new ArrayList<>();

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefItemsList = getSharedPreferences(SAVED_ItemsListToInvoice, Context.MODE_PRIVATE);
        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);

        salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)) {
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        }

        myList = getStringArrayPref(getApplicationContext(), SAVED_ItemsListToInvoice);

//        getPrices();
        listViewTest = findViewById(R.id.listViewTest);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, myList);
        listViewTest.setAdapter(arrayAdapter);
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

//    private void getPrices(){
//        for (int i = 0; i < myList.size(); i ++){
//            DataPrice dt = new DataPrice(myList.get(i));
//            dataArray.add(dt);
//        }
//        Gson gson = new Gson();
//        final String newDataArray = gson.toJson(dataArray);
//
//        StringRequest request = new StringRequest(Request.Method.POST,
//                requestUrlFinalPrice, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.d("response", "result: " + response);
//                invoiceNumber = response;
//                Toast.makeText(getApplicationContext(), "Номер накладной: " + invoiceNumber, Toast.LENGTH_SHORT).show();
//                dataArray.clear();
//                if (invoiceNumber.matches("-?\\d+")) {
//                    Toast.makeText(getApplicationContext(), "Документ сохранён", Toast.LENGTH_SHORT).show();
//                    statusSave = "Сохранено";
//                    textViewStatusSave.setText(statusSave);
//                }
//            }
//        }, new Response.ErrorListener(){
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getApplicationContext(), "Сообщите об этой ошибке. Код 001", Toast.LENGTH_SHORT).show();
//                Log.e("TAG", "Error " + error.getMessage());
//            }
//        }){
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> parameters = new HashMap<>();
//                parameters.put("dbName", dbName);
//                parameters.put("dbUser", dbUser);
//                parameters.put("dbPassword", dbPassword);
//                parameters.put("salesPartner", salesPartner);
//                parameters.put("array", newDataArray);
//                return parameters;
//            }
//        };
//        VolleySingleton.getInstance(this).getRequestQueue().add(request);
//    }
}
