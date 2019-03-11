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
    SharedPreferences sPrefLogin, sPrefPassword, sPrefDBName, sPrefDBPassword, sPrefDBUser,
            sPrefAreaDefault, sPrefConnectionStatus, sPrefAgent;
    final String SAVED_LOGIN = "Login";
    final String SAVED_PASSWORD = "Password";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_CONNSTATUS = "connectionStatus";
    final String SAVED_AGENT = "agent";
    SharedPreferences.Editor e;
    String loginUrl = "https://caiman.ru.com/php/login.php", dbName, dbUser, dbPassword, Login,
            Password, agent;
    String[] area;

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
        sPrefAreaDefault = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
        sPrefAgent = getSharedPreferences(SAVED_AGENT, Context.MODE_PRIVATE);

        sPrefConnectionStatus.edit().clear().apply();

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
                    .setPositiveButton("Настроить",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(getApplicationContext(), Settings.class);
                                    startActivity(intent);
                                }
                            })
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
                    area = new String[jsonArray.length()];
                    if (jsonArray.length() == 1){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            area[i] = obj.getString("Район");
                            agent = obj.getString("Фамилия") + " "
                                    + obj.getString("Имя") + " " + obj.getString("Отчество");
                        }
                        if (!area[0].equals("accountant") && !area[0].equals("ceo")) {
                            loadMainMenu();
                        }
                        if (area[0].equals("accountant")){
                            loadAccounting();
                        }
                        if (area[0].equals("ceo")){
                            loadAnalytics();
                        }
                        e = sPrefAgent.edit();
                        e.putString(SAVED_AGENT, agent);
                        e.apply();
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
                onConnectionFailed();

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

    private void loadMainMenu(){
        e = sPrefAreaDefault.edit();
        e.putString(SAVED_AREADEFAULT, area[0]);
        e.apply();
        e = sPrefConnectionStatus.edit();
        e.putString(SAVED_CONNSTATUS, "success");
        e.apply();
        Toast.makeText(getApplicationContext(), "Успешный вход. Ваш район: " + area[0], Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
        startActivity(intent);
    }

    private void loadAccounting(){
        Intent intent = new Intent(getApplicationContext(), AccountingActivity.class);
        startActivity(intent);
    }

    private void loadAnalytics(){
        Intent intent = new Intent(getApplicationContext(), StatsAnalyticsActivity.class);
        startActivity(intent);
    }

    private void onConnectionFailed(){
        e = sPrefConnectionStatus.edit();
        e.putString(SAVED_CONNSTATUS, "failed");
        e.apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Внимание")
                .setMessage("Вход через Интернет провалился")
                .setCancelable(true)
                .setPositiveButton("Попробовать снова",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Войти без Интернета",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                                startActivity(intent);
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
