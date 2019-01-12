package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateInvoiceChooseSalesPartnerActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnNext;
    ListView listViewSalesPartners, listViewAccountingType;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDBName, sPrefDBPassword, sPrefDBUser,
            sPrefDayOfTheWeek, sPrefSalesPartner;
    ArrayAdapter<String> arrayAdapter;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_DayOfTheWeek = "DayOfTheWeek";
    final String SAVED_SALESPARTNER = "SalesPartner";
    SharedPreferences.Editor e;
//    public static final String EXTRA_AGENTNAMENEXT = "com.example.myapplicationtest.AGENTNAMENEXT";
    String requestUrl = "https://caiman.ru.com/php/filter_new.php", salesPartner, dbName, dbUser, dbPassword,
            area, accountingType, dayOfTheWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_choose_sales_partner);

//        btnNext = findViewById(R.id.buttonNext);
//        btnNext.setOnClickListener(this);
        EditText search = findViewById(R.id.editTextSearch);

        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);

        listViewSalesPartners = findViewById(R.id.listViewSalesPartners);
//        listViewAccountingType = findViewById(R.id.listViewAccountingType);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefArea= getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DayOfTheWeek, Context.MODE_PRIVATE);

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)){
//                && sPrefArea.contains(SAVED_AREA) && sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)
//                && sPrefDayOfTheWeek.contains(SAVED_DayOfTheWeek)){
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
            area = sPrefArea.getString(SAVED_AREA, "");
            accountingType = sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, "");
            dayOfTheWeek = sPrefDayOfTheWeek.getString(SAVED_DayOfTheWeek, "");
        }

        receiveData();
//        loadListAccountingType();

//        listViewAccountingType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                accountingType = ((TextView) view).getText().toString();
//                Toast.makeText(getApplicationContext(), "Selected Item :" + accountingType, Toast.LENGTH_SHORT).show();
//            }
//        });

        listViewSalesPartners.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                salesPartner = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Контрагент: " + salesPartner, Toast.LENGTH_SHORT).show();
                createInvoice();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                arrayAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
                    Toast.makeText(getApplicationContext(), "Сервер ответил", Toast.LENGTH_SHORT).show();
                    String[] salesPartners = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            salesPartners[i] = obj.getString("Наименование");
                        }
                        Toast.makeText(getApplicationContext(), "Данные загружены", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                    }

                    arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, salesPartners);
                    listViewSalesPartners.setAdapter(arrayAdapter);
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Не смогли получить ответ от сервера!", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Ошибка: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                if (sPrefArea.contains(SAVED_AREA)){
                    parameters.put("Area", area);
                }
                if (sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)){
                    parameters.put("AccountingType", accountingType);
                }
                if (sPrefDayOfTheWeek.contains(SAVED_DayOfTheWeek)){
                    parameters.put("DayOfTheWeek", dayOfTheWeek);
                }
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
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

        Intent intent = new Intent(getApplicationContext(), CreateInvoiceChooseTypeOfInvoiceActivity.class);
//        TextView textView = findViewById(R.id.textViewAgent);
//        String agentName = textView.getText().toString();
//        intent.putExtra(EXTRA_AGENTNAMENEXT, agentName);
//        Toast.makeText(this, agentName, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}
