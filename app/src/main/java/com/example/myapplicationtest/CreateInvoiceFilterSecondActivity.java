package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class CreateInvoiceFilterSecondActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnNext;
    RequestQueue requestQueue;
    SharedPreferences sPrefSalesPartners, sPrefAccountingType, sPrefDBName, sPrefDBPassword, sPrefDBUser;
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    SharedPreferences.Editor e;
    public static final String EXTRA_AGENT = "com.example.myapplicationtest.AGENT";
    String requestUrl = "https://caiman.ru.com/php/filter.php", agentName, dbName, dbUser, dbPassword,
            salesPartner, accountingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_filter_second);

        //Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String agentName = intent.getStringExtra(MainMenu.EXTRA_AGENTNAMENEXT);

        //Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefSalesPartners = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)
                && sPrefSalesPartners.contains(SAVED_SALESPARTNER) && sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE)){
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        }

        recieveData();
    }

    @Override
    public void onClick(View v) {

    }

    private void recieveData(){
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    //JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = new JSONArray(response);
                    Toast.makeText(getApplicationContext(), "Login successfull", Toast.LENGTH_SHORT).show();
                    /*if(!jsonObject.names().get(0).equals("failed")){
                        Toast.makeText(getApplicationContext(), "Login successfull", Toast.LENGTH_SHORT).show();
                        agentName = jsonObject.getString("secondname") + " " +
                                     jsonObject.getString("firstname") + " " +
                                     jsonObject.getString("middlename");*/
                    String[] agentName = new String[jsonArray.length()];
                    if (jsonArray.length() == 1){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            agentName[i] = obj.getString("secondname") + " " + obj.getString("firstname")
                                    + " " + obj.getString("middlename");
                        }
                        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                        //EditText editText = (EditText) findViewById(R.id.editText2);
                        //String message = editText.getText().toString();
                        intent.putExtra(EXTRA_AGENTNAME, agentName[0]);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Login ERROR", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("Area", Login.getText().toString());
                parameters.put("AccountingType", Password.getText().toString());
                return parameters;
            }
        };
        requestQueue.add(request);
    }
}
