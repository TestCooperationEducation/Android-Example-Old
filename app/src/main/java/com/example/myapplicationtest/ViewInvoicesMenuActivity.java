package com.example.myapplicationtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ViewInvoicesMenuActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoiceNotSynced, btnInvoiceSynced, btnInvoiceFilterSPName, btnInvoiceFilterComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invoices_menu);

        btnInvoiceNotSynced = findViewById(R.id.buttonInvoicesNotSynced);
        btnInvoiceSynced = findViewById(R.id.buttonInvoicesSynced);
        btnInvoiceFilterSPName = findViewById(R.id.buttonInvoicesFilterSPName);
        btnInvoiceFilterComplete = findViewById(R.id.buttonInvoicesFilterComplete);
        btnInvoiceNotSynced.setOnClickListener(this);
        btnInvoiceSynced.setOnClickListener(this);
        btnInvoiceFilterSPName.setOnClickListener(this);
        btnInvoiceFilterComplete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoicesNotSynced:
                invoicesNotSynced();
                break;
            case R.id.buttonInvoicesSynced:
                invoicesSynced();
                break;
            case R.id.buttonInvoicesFilterSPName:
                invoicesFilterSPName();
                break;
            case R.id.buttonInvoicesFilterComplete:
                invoicesFilterComplete();
                break;
            default:
                break;
        }
    }

    private void invoicesNotSynced(){
        Intent intent = new Intent(getApplicationContext(), ViewInvoicesNotSyncedActivity.class);
        startActivity(intent);
    }

    private void invoicesSynced(){
        Intent intent = new Intent(getApplicationContext(), ViewInvoicesSyncedActivity.class);
        startActivity(intent);
    }

    private void invoicesFilterSPName(){
        Intent intent = new Intent(getApplicationContext(), ViewInvoicesFilterSPNameActivity.class);
        startActivity(intent);
    }

    private void invoicesFilterComplete(){
        Intent intent = new Intent(getApplicationContext(), ViewInvoicesFilterCompleteActivity.class);
        startActivity(intent);
    }
}
