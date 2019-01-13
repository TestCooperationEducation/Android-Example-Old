package com.example.myapplicationtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CreateInvoiceChooseTypeOfInvoiceActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoiceTypeOne, btnInvoiceTypeTwo;
    public static final String EXTRA_AccountingType = "com.example.myapplicationtest.AccountingType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_choose_type_of_invoice);

        btnInvoiceTypeOne = findViewById(R.id.buttonInvoiceTypeOne);
        btnInvoiceTypeOne.setOnClickListener(this);
        btnInvoiceTypeTwo = findViewById(R.id.buttonInvoiceTypeTwo);
        btnInvoiceTypeTwo.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoiceTypeOne:
                Intent intentOne = new Intent(getApplicationContext(), CreateInvoiceChooseItemsActivity.class);
//                String accountingTypeOne = "непровод";
//                intentOne.putExtra(EXTRA_AccountingType, accountingTypeOne);
//                Toast.makeText(this, "Вы создаёте: " + accountingTypeOne, Toast.LENGTH_SHORT).show();
                startActivity(intentOne);
                break;
            case R.id.buttonInvoiceTypeTwo:
                Intent intentTwo = new Intent(getApplicationContext(), CreateInvoiceChooseItemsActivity.class);
//                String accountingTypeTwo = "непровод";
//                intentTwo.putExtra(EXTRA_AccountingType, accountingTypeTwo);
//                Toast.makeText(this, "Вы создаёте: " + accountingTypeTwo, Toast.LENGTH_SHORT).show();
                startActivity(intentTwo);
                break;
            default:
                break;
        }
    }
}
