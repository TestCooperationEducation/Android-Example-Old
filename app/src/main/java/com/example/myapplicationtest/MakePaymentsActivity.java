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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MakePaymentsActivity extends AppCompatActivity implements View.OnClickListener{

    ListView listViewAccountingType, listViewDebtors;
    String[] invoiceNumber, total;
    String accountingType, dbName, dbUser, dbPassword, debtor, loginSecurity, dateStart, dateEnd,
            requestUrlLoadDebtors = "https://caiman.ru.com/php/loadDebtors.php";
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefLogin;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_LOGIN = "Login";
    Button btnReceiveList;
    EditText editTextDateStart, editTextDateEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payments);

        Intent intent = getIntent();
        String agentName = intent.getStringExtra(MainMenu.EXTRA_AGENTNAMENEXT);
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);

        btnReceiveList = findViewById(R.id.buttonReceiveList);
        btnReceiveList.setOnClickListener(this);

        editTextDateStart = findViewById(R.id.editTextDateStart);
        editTextDateEnd = findViewById(R.id.editTextDateEnd);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");

        listViewAccountingType = findViewById(R.id.listViewAccountingType);
        listViewDebtors  = findViewById(R.id.listViewDebtors);

        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        loginSecurity = sPrefLogin.getString(SAVED_LOGIN, "");

        loadListAccountingType();

        listViewAccountingType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                accountingType = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Тип учета :" + accountingType, Toast.LENGTH_SHORT).show();
            }
        });

        listViewDebtors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                debtor = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Контрагент :" + debtor, Toast.LENGTH_SHORT).show();
                String[] tmpDebtor = new String[1];
                tmpDebtor[0] = debtor;
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, tmpDebtor);
                listViewAccountingType.setAdapter(arrayAdapter);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonReceiveList:
                receiveList();
                break;
            default:
                break;
        }
    }

    private void loadListAccountingType(){
        String[] accountingType = new String[2];
        accountingType[0] = "провод";
        accountingType[1] = "непровод";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountingType);
        listViewAccountingType.setAdapter(arrayAdapter);
    }

    private void receiveList(){
        dateStart = editTextDateStart.getText().toString();
        dateEnd = editTextDateEnd.getText().toString();

        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlLoadDebtors, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", "result: " + response);
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Toast.makeText(getApplicationContext(), "Запрос выполнен удачно", Toast.LENGTH_SHORT).show();
//                    itemPrice = new String[jsonArray.length()];
                    total = new String[jsonArray.length()];
                    invoiceNumber = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
//                            itemPrice[i] = obj.getString("Цена");
//                            arrPrice.add(Double.parseDouble(itemPrice[0]));
                            if (obj.isNull("InvoiceNumber") && obj.isNull("Total")) {
//                                discountValue[i] = String.valueOf(0);
//                                discountType[i] = String.valueOf(0);
                                Toast.makeText(getApplicationContext(), "Нет", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                invoiceNumber[i] = obj.getString("InvoiceNumber");
                                total[i] = obj.getString("Total");
                                Toast.makeText(getApplicationContext(), invoiceNumber[i], Toast.LENGTH_SHORT).show();
                            }
                        }
//                        textViewPrice.setText(itemPrice[0]);
//                        textViewDiscountType.setText(discountType[0]);
//                        textViewDiscountValue.setText(discountValue[0]);
//                        if (Double.parseDouble(discountType[0]) == 0){
//                            textViewPrice.setText(itemPrice[0]);
//                            finalPrice = Double.parseDouble(textViewPrice.getText().toString());
//                        }
//                        if (Double.parseDouble(discountType[0]) == 1){
//                            finalPrice = Double.parseDouble(itemPrice[0]) - Double.parseDouble(discountValue[0]);
//                            textViewPrice.setText(finalPrice.toString());
//                        }
//                        if (Double.parseDouble(discountType[0]) == 2){
//                            finalPrice = Double.parseDouble(itemPrice[0]) - (Double.parseDouble(itemPrice[0]) / 10);
//                            textViewPrice.setText(finalPrice.toString());
//                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                    }
//                    Toast.makeText(getApplicationContext(), textViewPrice.getText().toString(), Toast.LENGTH_SHORT).show();
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
                parameters.put("dateStart", dateStart);
                parameters.put("dateEnd", dateEnd);
                parameters.put("accountingType", accountingType);
                parameters.put("loginSecurity", loginSecurity);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }
}
