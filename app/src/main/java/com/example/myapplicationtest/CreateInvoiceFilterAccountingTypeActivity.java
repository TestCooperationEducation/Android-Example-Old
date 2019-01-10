package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CreateInvoiceFilterAccountingTypeActivity extends AppCompatActivity implements View.OnClickListener{

    ListView listViewAccountingType;
    Button btnNext, btnChooseRoot, btnChooseArea;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDayOfTheWeek;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DAYOFTHEWEEK = "DayOfTheWeek";
    SharedPreferences.Editor e;
    String areaStr, dayOfTheWeek, accountingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_filter_accounting_type);

        btnNext = findViewById(R.id.buttonNext);
        btnNext.setOnClickListener(this);
        btnChooseRoot = findViewById(R.id.buttonChooseRoot);
        btnChooseRoot.setOnClickListener(this);
        btnChooseArea = findViewById(R.id.buttonChooseArea);
        btnChooseArea.setOnClickListener(this);

        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DAYOFTHEWEEK, Context.MODE_PRIVATE);

        listViewAccountingType = findViewById(R.id.listViewAccountingType);

        loadListAccountingType();

        listViewAccountingType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                accountingType = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Тип учёта: " + accountingType, Toast.LENGTH_SHORT).show();

                e = sPrefAccountingType.edit();
                e.putString(SAVED_ACCOUNTINGTYPE, accountingType);
                e.apply();

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:
                goNext();
                break;
            case R.id.buttonChooseRoot:
                goRootTab();
                break;
            case R.id.buttonChooseArea:
                goAreaTab();
                break;
            default:
                break;
        }
    }

    private void loadListAccountingType(){
        String[] accountingType = new String[2];
        accountingType[0] = "провод";
        accountingType[1] = "непровод";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountingType);
        listViewAccountingType.setAdapter(arrayAdapter);
    }

    private void goNext(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceChooseSalesPartnerActivity.class);
        startActivity(intent);
    }

    private void goAreaTab(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAreaActivity.class);
        startActivity(intent);
    }

    private void goRootTab(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterRootActivity.class);
        startActivity(intent);
    }
}
