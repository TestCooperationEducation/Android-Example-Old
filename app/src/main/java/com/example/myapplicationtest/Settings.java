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
    EditText dbName, dbUser, dbPassword;
    Button btnSave;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser;
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
        btnSave = findViewById(R.id.buttonSave);
        btnSave.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                saveDBSettings();
                Toast.makeText(this, "DB Settings Saved", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void saveDBSettings(){
        e = sPrefDBName.edit();
        e.putString(SAVED_DBName, dbUser.getText().toString());
        e.apply();
        e = sPrefDBPassword.edit();
        e.putString(SAVED_DBPassword, dbPassword.getText().toString());
        e.apply();
        e = sPrefDBUser.edit();
        e.putString(SAVED_DBUser, dbUser.getText().toString());
        e.apply();
    }
}
