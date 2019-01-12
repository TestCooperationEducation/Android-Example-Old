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

public class CreateInvoiceFilterAreaActivity extends AppCompatActivity implements View.OnClickListener{

    ListView listViewArea;
    Button btnNext, btnChooseRoot, btnChooseAccountingType;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDayOfTheWeek;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DAYOFTHEWEEK = "DayOfTheWeek";
    SharedPreferences.Editor e;
    String areaStr, dayOfTheWeek, accountingType;
    String[] area = new String[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_filter_area);

        btnNext = findViewById(R.id.buttonNext);
        btnNext.setOnClickListener(this);
        btnChooseRoot = findViewById(R.id.buttonChooseRoot);
        btnChooseRoot.setOnClickListener(this);
        btnChooseAccountingType = findViewById(R.id.buttonChooseAccountingType);
        btnChooseAccountingType.setOnClickListener(this);

        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
//        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
//        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DAYOFTHEWEEK, Context.MODE_PRIVATE);

        listViewArea = findViewById(R.id.listViewArea);

        loadListArea();

        listViewArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                areaStr = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Вы выбрали: " + areaStr, Toast.LENGTH_SHORT).show();
                for (int i = 0; i < 5; i++){
                    if (areaStr.equals(area[i])){
                        String b = String.valueOf(i + 1);
                        e = sPrefArea.edit();
                        e.putString(SAVED_AREA, b);
                        e.apply();
                    }
                }
            }
        });

//        if (sPrefArea.contains(SAVED_AREA) && sPrefAccountingType.contains(SAVED_ACCOUNTINGTYPE) && sPrefDayOfTheWeek.contains(SAVED_DAYOFTHEWEEK)){
//            areaStr = sPrefArea.getString(SAVED_AREA, "");
//            dayOfTheWeek = sPrefDayOfTheWeek.getString(SAVED_DAYOFTHEWEEK, "");
//            accountingType = sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, "");
//        }
    }

    private void loadListArea(){
        area[0] = "Район №1";
        area[1] = "Район №2";
        area[2] = "Район №3";
        area[3] = "Район №4";
        area[4] = "Район №5";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, area);
        listViewArea.setAdapter(arrayAdapter);
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
            case R.id.buttonChooseAccountingType:
                goAccountingTab();
                break;
            default:
                break;
        }
    }

    private void goNext(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceChooseSalesPartnerActivity.class);
        startActivity(intent);
    }

    private void goAccountingTab(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAccountingTypeActivity.class);
        startActivity(intent);
    }

    private void goRootTab(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterRootActivity.class);
        startActivity(intent);
    }
}
