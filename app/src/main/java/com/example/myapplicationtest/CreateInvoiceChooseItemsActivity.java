package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateInvoiceChooseItemsActivity extends AppCompatActivity implements View.OnClickListener {

    String requestUrl = "https://caiman.ru.com/php/items.php", dbName, dbUser, dbPassword, item;
    String[] itemsList;
    ListView listViewItems;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefItemsList;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_ItemsListToInvoice = "itemsToInvoice";
    SharedPreferences.Editor e;
    ArrayList<String> tmp;
    Button btnCreateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_choose_items);

        listViewItems = findViewById(R.id.listViewItemsToSelect);

//        myList = new ArrayList<>();
        btnCreateList = findViewById(R.id.buttonNext);
        btnCreateList.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefItemsList = getSharedPreferences(SAVED_ItemsListToInvoice, Context.MODE_PRIVATE);

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)) {
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        }

        receiveItemsList();

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                item = ((TextView) view).getText().toString();
                addItemsToInvoice();
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:
                Intent intent = new Intent(this, CreateInvoiceSetItemsQuantitiesActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void receiveItemsList(){
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Toast.makeText(getApplicationContext(), "Query successful", Toast.LENGTH_SHORT).show();
                    itemsList = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemsList[i] = obj.getString("Наименование");
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Something went wrong with DB query", Toast.LENGTH_SHORT).show();
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, itemsList);
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

    private void addItemsToInvoice() {
        tmp = new ArrayList<>();
        SparseBooleanArray chosen = listViewItems.getCheckedItemPositions();
        for (int i = 0; i < listViewItems.getCount(); i++) {
            if (chosen.get(i) == true) {
                tmp.add(listViewItems.getItemAtPosition(i).toString());
            }
        }
        setStringArrayPref(getApplicationContext(), SAVED_ItemsListToInvoice, tmp);
    }
    public static void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
    }
}
