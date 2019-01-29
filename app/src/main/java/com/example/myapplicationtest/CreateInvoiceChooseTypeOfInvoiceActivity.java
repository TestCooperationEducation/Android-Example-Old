package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CreateInvoiceChooseTypeOfInvoiceActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoiceTypeOne, btnInvoiceTypeTwo;
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_ItemsListSaveStatus = "itemsListSaveStatus";
    SharedPreferences sPrefAccountingType, sPrefItemsListSaveStatus;
    SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_choose_type_of_invoice);

        btnInvoiceTypeOne = findViewById(R.id.buttonInvoiceTypeOne);
        btnInvoiceTypeOne.setOnClickListener(this);
        btnInvoiceTypeTwo = findViewById(R.id.buttonInvoiceTypeTwo);
        btnInvoiceTypeTwo.setOnClickListener(this);

        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefItemsListSaveStatus = getSharedPreferences(SAVED_ItemsListSaveStatus, Context.MODE_PRIVATE);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoiceTypeOne:
                e = sPrefAccountingType.edit();
                e.putString(SAVED_ACCOUNTINGTYPE, "провод");
                e.apply();
                Intent intentOne = new Intent(getApplicationContext(), CreateInvoiceChooseItemsActivity.class);
                startActivity(intentOne);
                break;
            case R.id.buttonInvoiceTypeTwo:
                e = sPrefAccountingType.edit();
                e.putString(SAVED_ACCOUNTINGTYPE, "непровод");
                e.apply();
                Intent intentTwo = new Intent(getApplicationContext(), CreateInvoiceChooseItemsActivity.class);
                startActivity(intentTwo);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        String itemsListSaveStatus = sPrefItemsListSaveStatus.getString(SAVED_ItemsListSaveStatus, "");
        if (itemsListSaveStatus.equals("saved")){
            finish();
        } else {

        }
    }
}
