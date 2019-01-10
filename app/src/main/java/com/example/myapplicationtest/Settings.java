package com.example.myapplicationtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends AppCompatActivity implements View.OnClickListener{
    EditText dbName, dbUser, dbPassword, login, password;
    Button btnSaveDBSettings, btnSaveLoginPassword;
    SharedPreferences sPrefLogin, sPrefPassword, sPrefDBName, sPrefDBPassword, sPrefDBUser;
    final String SAVED_LOGIN = "Login";
    final String SAVED_PASSWORD = "Password";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbName = findViewById((R.id.editTextDBName));
        dbPassword = findViewById((R.id.editTextDBPassword));
        dbUser = findViewById(R.id.editTextDBUser);
        btnSaveDBSettings = findViewById(R.id.buttonSaveDBSettings);
        btnSaveDBSettings.setOnClickListener(this);
        btnSaveLoginPassword = findViewById(R.id.buttonSaveLoginPassword);
        btnSaveLoginPassword.setOnClickListener(this);

        login = findViewById((R.id.editTextLogin));
        password = findViewById((R.id.editTextPassword));

        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefPassword = getSharedPreferences(SAVED_PASSWORD, Context.MODE_PRIVATE);
        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);

        if (sPrefLogin.contains(SAVED_LOGIN) && sPrefPassword.contains(SAVED_PASSWORD)){
            login.setText(sPrefLogin.getString(SAVED_LOGIN, ""));
            password.setText(sPrefPassword.getString(SAVED_PASSWORD, ""));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSaveDBSettings:
                saveDBSettings();
                Toast.makeText(this, "Настройки БД сохранены", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonSaveLoginPassword:
                saveLoginPassword();
                Toast.makeText(this, "Логин и пароль сохранены", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void saveDBSettings(){
        e = sPrefDBName.edit();
        e.putString(SAVED_DBName, dbName.getText().toString());
        e.apply();
        e = sPrefDBPassword.edit();
        e.putString(SAVED_DBPassword, dbPassword.getText().toString());
        e.apply();
        e = sPrefDBUser.edit();
        e.putString(SAVED_DBUser, dbUser.getText().toString());
        e.apply();
    }

    private void saveLoginPassword() {
        e = sPrefLogin.edit();
        e.putString(SAVED_LOGIN, login.getText().toString());
        e.apply();
        e = sPrefPassword.edit();
        e.putString(SAVED_PASSWORD, password.getText().toString());
        e.apply();
    }
}
