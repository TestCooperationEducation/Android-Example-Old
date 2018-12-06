package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class CreateInvoiceFilterSecondActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnNext;
    ListView listViewSalesPartners, listViewAccountingType;
    RequestQueue requestQueue;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDBName, sPrefDBPassword, sPrefDBUser,
            sPrefDayOfTheWeek, sPrefSalesPartner;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_DayOfTheWeek = "DayOfTheWeek";
    final String SAVED_SALESPARTNER = "SalesPartner";
    SharedPreferences.Editor e;
    public static final String EXTRA_AGENTNAMENEXT = "com.example.myapplicationtest.AGENTNAMENEXT";
    String requestUrl = "https://caiman.ru.com/php/filter.php", salesPartner, dbName, dbUser, dbPassword,
            area, accountingType, dayOfTheWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_filter_second);

        Intent intent = getIntent();
        String agentName = intent.getStringExtra(CreateInvoiceFilterFirstActivity.EXTRA_AGENT);
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);

        btnNext = findViewById(R.id.buttonNext);
        btnNext.setOnClickListener(this);

        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);

        listViewSalesPartners = findViewById(R.id.listViewSalesPartners);
        listViewAccountingType = findViewById(R.id.listViewAccountingType);

        requestQueue = Volley.newRequestQueue((getApplicationContext()));

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefArea= getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DayOfTheWeek, Context.MODE_PRIVATE);

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)
                && sPrefArea.contains(SAVED_AREA) && sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)){
//                && sPrefDayOfTheWeek.contains(SAVED_DayOfTheWeek)){
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
            area = sPrefArea.getString(SAVED_AREA, "");
            accountingType = sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, "");
            dayOfTheWeek = sPrefDayOfTheWeek.getString(SAVED_DayOfTheWeek, "");
        }

        receiveData();
        loadListAccountingType();

        listViewAccountingType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                accountingType = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Selected Item :" + accountingType, Toast.LENGTH_SHORT).show();
            }
        });

        listViewSalesPartners.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                salesPartner = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Selected Item :" + salesPartner, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:
                createInvoice();
                break;
            default:
                break;
        }
    }

    private void receiveData(){
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Toast.makeText(getApplicationContext(), "Query successful", Toast.LENGTH_SHORT).show();
                    String[] salesPartners = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            salesPartners[i] = obj.getString("Наименование");
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Something went wrong with DB query", Toast.LENGTH_SHORT).show();
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, salesPartners);
                    listViewSalesPartners.setAdapter(arrayAdapter);
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
                parameters.put("Area", area);
                parameters.put("AccountingType", accountingType);
                //parameters.put("DayOfTheWeek", dayOfTheWeek);
                return parameters;
            }
        };
        requestQueue.add(request);
    }

    private void loadListAccountingType(){
        String[] accountingType = new String[2];
        accountingType[0] = "провод";
        accountingType[1] = "непровод";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountingType);
        listViewAccountingType.setAdapter(arrayAdapter);
    }

    private void createInvoice(){
        e = sPrefSalesPartner.edit();
        e.putString(SAVED_SALESPARTNER, salesPartner);
        e.apply();
        e = sPrefAccountingType.edit();
        e.putString(SAVED_ACCOUNTINGTYPE, accountingType);
        e.apply();

        Intent intent = new Intent(getApplicationContext(), CreateInvoiceMainActivity.class);
        TextView textView = findViewById(R.id.textViewAgent);
        String agentName = textView.getText().toString();
        intent.putExtra(EXTRA_AGENTNAMENEXT, agentName);
        Toast.makeText(this, agentName, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}