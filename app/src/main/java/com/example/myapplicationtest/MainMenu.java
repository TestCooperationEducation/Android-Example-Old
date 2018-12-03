package com.example.myapplicationtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String agentName = intent.getStringExtra(Login.EXTRA_AGENTNAME);

        //Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoice:
                createInvoice();
                //Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonPayments:
                //makePayments();
                //Toast.makeText(this, dbName + " " + dbUser + " " + dbPassword, Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonSalesPartners:
                //addSalesPartner();
                break;
            default:
                break;
        }
    }

    private void createInvoice(){
        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
        startActivity(intent);
    }
}
