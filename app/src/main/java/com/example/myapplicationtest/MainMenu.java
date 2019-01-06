package com.example.myapplicationtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoice, btnPayments, btnSalesAgents;
    public static final String EXTRA_AGENTNAMENEXT = "com.example.myapplicationtest.AGENTNAMENEXT";

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
                Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
                createInvoice();
                //Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonPayments:
                makePayments();
                //Toast.makeText(this, dbName + " " + dbUser + " " + dbPassword, Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonSalesPartners:
                manageSalesPartners();
                break;
            default:
                break;
        }
    }

    private void createInvoice(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterFirstActivity.class);
        TextView textView = findViewById(R.id.textViewAgent);
        String agentName = textView.getText().toString();
        intent.putExtra(EXTRA_AGENTNAMENEXT, agentName);
        Toast.makeText(this, agentName, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    private void makePayments(){
        Intent intent = new Intent(getApplicationContext(), MakePaymentsActivity.class);
        TextView textView = findViewById(R.id.textViewAgent);
        String agentName = textView.getText().toString();
        intent.putExtra(EXTRA_AGENTNAMENEXT, agentName);
        Toast.makeText(this, agentName, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    private void manageSalesPartners(){
        Intent intent = new Intent(getApplicationContext(), ManageSalesPartnersActivity.class);
        TextView textView = findViewById(R.id.textViewAgent);
        String agentName = textView.getText().toString();
        intent.putExtra(EXTRA_AGENTNAMENEXT, agentName);
        Toast.makeText(this, agentName, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}
