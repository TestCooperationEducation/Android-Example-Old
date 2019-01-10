package com.example.myapplicationtest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class Login extends AppCompatActivity implements View.OnClickListener {

    Button btnLogin, btnSettings;
    SharedPreferences sPrefLogin, sPrefPassword, sPrefDBName, sPrefDBPassword, sPrefDBUser;
    final String SAVED_LOGIN = "Login";
    final String SAVED_PASSWORD = "Password";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    SharedPreferences.Editor e;
    String loginUrl = "https://caiman.ru.com/php/login.php", dbName, dbUser, dbPassword, Login, Password;
    public static final String EXTRA_AGENTNAME = "com.example.myapplicationtest.AGENTNAME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.buttonLogin);;
        btnSettings = findViewById(R.id.buttonSettings);
        btnLogin.setOnClickListener(this);
        btnSettings.setOnClickListener(this);

        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefPassword = getSharedPreferences(SAVED_PASSWORD, Context.MODE_PRIVATE);
        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword) &&
                sPrefLogin.contains(SAVED_LOGIN) && sPrefPassword.contains(SAVED_PASSWORD)){
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
            Login = sPrefLogin.getString(SAVED_LOGIN, "");
            Password = sPrefPassword.getString(SAVED_PASSWORD, "");
            login();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ошибка")
                    .setMessage("Настройте Учётку для входа")
                    .setCancelable(true)
                    .setNegativeButton("Назад",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLogin:
                login();
                break;
            case R.id.buttonSettings:
                dbSettings();
                break;
            default:
                break;
        }
    }

    private void dbSettings(){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    private void login() {
        StringRequest request = new StringRequest(Request.Method.POST,
                loginUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    String[] agentName = new String[jsonArray.length()];
                    if (jsonArray.length() == 1){
                        Toast.makeText(getApplicationContext(), "Успешный вход", Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            agentName[i] = obj.getString("secondname") + " " + obj.getString("firstname")
                                    + " " + obj.getString("middlename");
                        }
                        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
//                        intent.putExtra(EXTRA_AGENTNAME, agentName[0]);
                        startActivity(intent);
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
                parameters.put("Login", sPrefLogin.getString(SAVED_LOGIN, ""));
                parameters.put("Password", sPrefPassword.getString(SAVED_PASSWORD, ""));
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }
}
