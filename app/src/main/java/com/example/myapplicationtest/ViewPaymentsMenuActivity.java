package com.example.myapplicationtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ViewPaymentsMenuActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnPaymentsSynced, btnPaymentsNotSynced, btnPaymentsShowAll, btnMakePayment,
            btnPaymentsFilterSP, btnPaymentsFilterComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_payments_menu);

        btnPaymentsSynced = findViewById(R.id.buttonPaymentsSynced);
        btnPaymentsNotSynced = findViewById(R.id.buttonPaymentsNotSynced);
        btnPaymentsShowAll = findViewById(R.id.buttonPaymentsShowAll);
        btnMakePayment = findViewById(R.id.buttonMakePayment);
        btnPaymentsFilterSP = findViewById(R.id.buttonPaymentsFilterSP);
        btnPaymentsFilterComplete = findViewById(R.id.buttonPaymentFilterComplete);
        btnPaymentsSynced.setOnClickListener(this);
        btnPaymentsNotSynced.setOnClickListener(this);
        btnPaymentsShowAll.setOnClickListener(this);
        btnMakePayment.setOnClickListener(this);
        btnPaymentsFilterSP.setOnClickListener(this);
        btnPaymentsFilterComplete.setOnClickListener(this);
}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonPaymentsSynced:
                paymentsSynced();
                break;
            case R.id.buttonPaymentsNotSynced:
                paymentsNotSynced();
                break;
            case R.id.buttonPaymentsShowAll:
                paymentsShowAll();
                break;
            case R.id.buttonMakePayment:
                makePayment();
                break;
            case R.id.buttonPaymentsFilterSP:
                paymentsFilterSP();
                break;
            case R.id.buttonPaymentFilterComplete:
                paymentsFilterComplete();
                break;
            default:
                break;
        }
    }

    private void paymentsNotSynced(){
        Intent intent = new Intent(getApplicationContext(), ViewPaymentsNotSyncedActivity.class);
        startActivity(intent);
    }

    private void paymentsSynced(){
        Intent intent = new Intent(getApplicationContext(), ViewPaymentsSyncedActivity.class);
        startActivity(intent);
    }

    private void makePayment(){
        Intent intent = new Intent(getApplicationContext(), ViewPaymentsMakePayment.class);
        startActivity(intent);
    }

    private void paymentsShowAll(){
        Intent intent = new Intent(getApplicationContext(), ViewPaymentsShowAllActivity.class);
        startActivity(intent);
    }

    private void paymentsFilterSP(){
        Intent intent = new Intent(getApplicationContext(), ViewPaymentsFilterSP.class);
        startActivity(intent);
    }

    private void paymentsFilterComplete(){
        Intent intent = new Intent(getApplicationContext(), ViewPaymentsFilterComplete.class);
        startActivity(intent);
    }
}
