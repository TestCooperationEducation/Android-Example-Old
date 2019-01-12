package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoice, btnPayments, btnSalesAgents;
    public static final String EXTRA_AGENTNAMENEXT = "com.example.myapplicationtest.AGENTNAMENEXT";
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDayOfTheWeek;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DAYOFTHEWEEK = "DayOfTheWeek";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        btnInvoice = findViewById(R.id.buttonInvoice);
        btnPayments = findViewById(R.id.buttonPayments);
        btnSalesAgents = findViewById(R.id.buttonSalesPartners);
        btnInvoice.setOnClickListener(this);
        btnPayments.setOnClickListener(this);
        btnSalesAgents.setOnClickListener(this);

//        Intent intent = getIntent();
//        String agentName = intent.getStringExtra(Login.EXTRA_AGENTNAME);
//        TextView textView = findViewById(R.id.textViewAgent);
//        textView.setText(agentName);

        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DAYOFTHEWEEK, Context.MODE_PRIVATE);

        sPrefArea.edit().clear().apply();
        sPrefAccountingType.edit().clear().apply();
        sPrefDayOfTheWeek.edit().clear().apply();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoice:
                Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
                createInvoice();
                break;
            case R.id.buttonPayments:
                makePayments();
                break;
            case R.id.buttonSalesPartners:
                manageSalesPartners();
                break;
            default:
                break;
        }
    }

    private void createInvoice(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAreaActivity.class);
//        TextView textView = findViewById(R.id.textViewAgent);
//        String agentName = textView.getText().toString();
//        intent.putExtra(EXTRA_AGENTNAMENEXT, agentName);
//        Toast.makeText(this, agentName, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    private void makePayments(){
        Intent intent = new Intent(getApplicationContext(), MakePaymentsActivity.class);
//        TextView textView = findViewById(R.id.textViewAgent);
//        String agentName = textView.getText().toString();
//        intent.putExtra(EXTRA_AGENTNAMENEXT, agentName);
//        Toast.makeText(this, agentName, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    private void manageSalesPartners(){
        Intent intent = new Intent(getApplicationContext(), ManageSalesPartnersActivity.class);
//        TextView textView = findViewById(R.id.textViewAgent);
//        String agentName = textView.getText().toString();
//        intent.putExtra(EXTRA_AGENTNAMENEXT, agentName);
//        Toast.makeText(this, agentName, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}
