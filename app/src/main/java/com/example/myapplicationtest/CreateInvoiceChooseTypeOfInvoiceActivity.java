package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CreateInvoiceChooseTypeOfInvoiceActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoiceTypeOne, btnInvoiceTypeTwo;
    final String SAVED_ACCOUNTINGTYPEDocFilter = "AccountingTypeDocFilter";
    final String SAVED_ItemsListSaveStatus = "itemsListSaveStatus";
    SharedPreferences sPrefAccountingTypeDocFilter, sPrefItemsListSaveStatus;
    SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_choose_type_of_invoice);

        btnInvoiceTypeOne = findViewById(R.id.buttonInvoiceTypeOne);
        btnInvoiceTypeOne.setOnClickListener(this);
        btnInvoiceTypeTwo = findViewById(R.id.buttonInvoiceTypeTwo);
        btnInvoiceTypeTwo.setOnClickListener(this);

        sPrefAccountingTypeDocFilter = getSharedPreferences(SAVED_ACCOUNTINGTYPEDocFilter, Context.MODE_PRIVATE);
        sPrefItemsListSaveStatus = getSharedPreferences(SAVED_ItemsListSaveStatus, Context.MODE_PRIVATE);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoiceTypeOne:
                e = sPrefAccountingTypeDocFilter.edit();
                e.putString(SAVED_ACCOUNTINGTYPEDocFilter, "провод");
                e.apply();
                Intent intentOne = new Intent(getApplicationContext(), CreateInvoiceChooseItemsActivity.class);
                startActivity(intentOne);
                break;
            case R.id.buttonInvoiceTypeTwo:
                e = sPrefAccountingTypeDocFilter.edit();
                e.putString(SAVED_ACCOUNTINGTYPEDocFilter, "непровод");
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
