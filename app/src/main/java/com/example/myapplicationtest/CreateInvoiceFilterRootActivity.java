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

public class CreateInvoiceFilterRootActivity extends AppCompatActivity implements View.OnClickListener{

    ListView listViewDayOfTheWeek;
    Button btnNext, btnChooseAccountingType, btnChooseArea;
    SharedPreferences sPrefDayOfTheWeek;
    final String SAVED_DAYOFTHEWEEK = "DayOfTheWeek";
    SharedPreferences.Editor e;
    String dayOfTheWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_filter_root);

        listViewDayOfTheWeek = findViewById(R.id.listViewDayOfTheWeek);

        btnNext = findViewById(R.id.buttonNext);
        btnNext.setOnClickListener(this);
        btnChooseArea = findViewById(R.id.buttonChooseArea);
        btnChooseArea.setOnClickListener(this);
        btnChooseAccountingType = findViewById(R.id.buttonChooseAccountingType);
        btnChooseAccountingType.setOnClickListener(this);

        loadListDayOfTheWeek();

        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DAYOFTHEWEEK, Context.MODE_PRIVATE);

        listViewDayOfTheWeek.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                dayOfTheWeek = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Маршрут: " + dayOfTheWeek, Toast.LENGTH_SHORT).show();

                e = sPrefDayOfTheWeek.edit();
                e.putString(SAVED_DAYOFTHEWEEK, dayOfTheWeek);
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
            case R.id.buttonChooseArea:
                goAreaTab();
                break;
            case R.id.buttonChooseAccountingType:
                goAccountingTab();
                break;
            default:
                break;
        }
    }

    private void loadListDayOfTheWeek(){
        String[] dayOfTheWeek = new String[5];
        dayOfTheWeek[0] = "понедельник-четверг";
        dayOfTheWeek[1] = "вторник-пятница";
        dayOfTheWeek[2] = "среда";
        dayOfTheWeek[3] = "любой";
        dayOfTheWeek[4] = "север";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dayOfTheWeek);
        listViewDayOfTheWeek.setAdapter(arrayAdapter);
    }

    private void goNext(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceChooseSalesPartnerActivity.class);
        startActivity(intent);
    }

    private void goAreaTab(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAreaActivity.class);
        startActivity(intent);
    }

    private void goAccountingTab(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAccountingTypeActivity.class);
        startActivity(intent);
    }
}
