package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

import java.util.HashMap;
import java.util.Map;

public class CreateInvoiceMainActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    String requestUrl = "https://caiman.ru.com/php/items.php", dbName, dbUser, dbPassword,
            accountingType, salesPartner;
    ListView listViewItems;
    TextView textViewAccountingType, textViewSalesPartner;
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

        requestQueue = Volley.newRequestQueue((getApplicationContext()));
        listViewItems = findViewById(R.id.listViewItems);
        textViewSalesPartner = findViewById(R.id.textViewSalesPartner);
        textViewAccountingType = findViewById(R.id.textViewAccountingType);

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
}
