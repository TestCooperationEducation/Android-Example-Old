package com.example.myapplicationtest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;

public class CreateInvoiceFilterSecondActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnNext;
    RequestQueue requestQueue;
    SharedPreferences sPrefSalesPartners, sPrefAccountingType;
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    SharedPreferences.Editor e;
    public static final String EXTRA_AGENT = "com.example.myapplicationtest.AGENT";

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
    }

    @Override
    public void onClick(View v) {

    }
}
