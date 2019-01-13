package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoice, btnPayments, btnSalesAgents;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDayOfTheWeekDefault, sPrefDBName,
            sPrefDBPassword, sPrefDBUser, sPrefDayOfTheWeek, sPrefVisited;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DAYOFTHEWEEKDEFAULT = "DayOfTheWeekDefault";
    final String SAVED_DayOfTheWeek = "DayOfTheWeek";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_VISITED = "visited";
    String loginUrl = "https://caiman.ru.com/php/login.php", dbName, dbUser, dbPassword;
    String[] dayOfTheWeek;
    SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        btnInvoice = findViewById(R.id.buttonInvoice);
        btnPayments = findViewById(R.id.buttonPayments);
        btnSalesAgents = findViewById(R.id.buttonSalesPartners);
        btnInvoice.setOnClickListener(this);
        btnPayments.setOnClickListener(this);
        btnSalesAgents.setOnClickListener(this);

//        Intent intent = getIntent();
//        String agentName = intent.getStringExtra(Login.EXTRA_AGENTNAME);
//        TextView textView = findViewById(R.id.textViewAgent);
//        textView.setText(agentName);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeekDefault = getSharedPreferences(SAVED_DAYOFTHEWEEKDEFAULT, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DayOfTheWeek, Context.MODE_PRIVATE);
        sPrefVisited = getSharedPreferences(SAVED_VISITED, Context.MODE_PRIVATE);

        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");

        sPrefArea.edit().clear().apply();
        sPrefAccountingType.edit().clear().apply();
        sPrefDayOfTheWeek.edit().clear().apply();
        sPrefVisited.edit().clear().apply();

        loadData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoice:
                Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
                createInvoice();
                break;
            case R.id.buttonPayments:
                makePayments();
                break;
            case R.id.buttonSalesPartners:
                manageSalesPartners();
                break;
            default:
                break;
        }
    }

    private void createInvoice(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAreaActivity.class);
        startActivity(intent);
    }

    private void makePayments(){
        Intent intent = new Intent(getApplicationContext(), MakePaymentsActivity.class);
        startActivity(intent);
    }

    private void manageSalesPartners(){
        Intent intent = new Intent(getApplicationContext(), ManageSalesPartnersActivity.class);
        startActivity(intent);
    }

    private void loadData(){
        StringRequest request = new StringRequest(Request.Method.POST,
                loginUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    dayOfTheWeek = new String[jsonArray.length()];
                    if (jsonArray.length() == 1){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("dayOfTheWeek");
                        }
                        e = sPrefDayOfTheWeekDefault.edit();
                        e.putString(SAVED_DAYOFTHEWEEKDEFAULT, dayOfTheWeek[0]);
                        e.apply();
                        Toast.makeText(getApplicationContext(), "День недели: " + dayOfTheWeek[0], Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка Входа. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
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
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }
}
