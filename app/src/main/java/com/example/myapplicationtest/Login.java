package com.example.myapplicationtest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.RequestQueue;

public class Login extends AppCompatActivity implements View.OnClickListener {

    EditText Login, Password, ServerName, ServerUser, ServerPassword;
    Button btnLogin, btnSave, btnSettings;
    RequestQueue requestQueue;
    SharedPreferences sPrefLogin, sPrefPassword, sPrefDBName, sPrefDBPassword, sPrefDBUser;
    final String SAVED_LOGIN = "Login";
    final String SAVED_PASSWORD = "Password";
    SharedPreferences.Editor e;
    String loginUrl = "https://caiman.ru.com/php/test1.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Login = findViewById((R.id.editTextLogin));
        Password = findViewById((R.id.editTextPassword));
        btnLogin = findViewById(R.id.buttonLogin);
        btnSave = findViewById(R.id.buttonSave);
        btnSettings = findViewById(R.id.buttonSettings);
        btnLogin.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnSettings.setOnClickListener(this);

        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefPassword = getSharedPreferences(SAVED_PASSWORD, Context.MODE_PRIVATE);

        if(sPrefLogin.contains(SAVED_LOGIN)) {
            Login.setText(sPrefLogin.getString(SAVED_LOGIN, ""));
            Password.setText(sPrefPassword.getString(SAVED_PASSWORD, ""));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                saveLoginPassword();
                Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonLogin:
                login();
                break;
            case R.id.buttonSettings:
                serverSettings();
                break;
            default:
                break;
        }
    }

    private void serverSettings(){
        Intent intent = new Intent(this, Settings.class);
        //EditText editText = (EditText) findViewById(R.id.editText2);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    private void login() {
    }

    private void saveLoginPassword() {
        e = sPrefLogin.edit();
        e.putString(SAVED_LOGIN, Login.getText().toString());
        e.apply();
        e = sPrefPassword.edit();
        e.putString(SAVED_PASSWORD, Password.getText().toString());
        e.apply();
        //Toast.makeText(this, "Login and Password saved", Toast.LENGTH_SHORT).show();
    }
}
