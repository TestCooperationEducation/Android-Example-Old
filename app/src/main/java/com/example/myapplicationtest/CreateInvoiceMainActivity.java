package com.example.myapplicationtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CreateInvoiceMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_main);

        Intent intent = getIntent();
        String agentName = intent.getStringExtra(CreateInvoiceFilterSecondActivity.EXTRA_AGENTNAMENEXT);
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);
    }
}
