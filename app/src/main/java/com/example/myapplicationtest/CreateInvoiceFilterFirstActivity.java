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

import org.json.JSONObject;

public class CreateInvoiceFilterFirstActivity extends AppCompatActivity implements View.OnClickListener{

    ListView listViewArea, listViewAccountingType, listViewDayOfTheWeek;
    Button btnNext;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDayOfTheWeek;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DAYOFTHEWEEK = "DayOfTheWeek";
    SharedPreferences.Editor e;
    public static final String EXTRA_AGENT = "com.example.myapplicationtest.AGENT";
    String areaStr, dayOfTheWeek, accountingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_filter_first);

        btnNext = findViewById(R.id.buttonNext);
        btnNext.setOnClickListener(this);

        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DAYOFTHEWEEK, Context.MODE_PRIVATE);

        //Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String agentName = intent.getStringExtra(MainMenu.EXTRA_AGENTNAMENEXT);

        //Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);

        listViewArea = findViewById(R.id.listViewArea);
        listViewAccountingType = findViewById(R.id.listViewAccountingType);
        listViewDayOfTheWeek = findViewById(R.id.listViewDayOfTheWeek);

        loadListArea();
        loadListAccountingType();
        loadListDayOfTheWeek();

        listViewArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                areaStr = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Selected Item :" + areaStr, Toast.LENGTH_SHORT).show();
            }
        });
        listViewDayOfTheWeek.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                dayOfTheWeek = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Selected Item :" + dayOfTheWeek, Toast.LENGTH_SHORT).show();
            }
        });
        listViewAccountingType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                accountingType = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Selected Item :" + accountingType, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadListArea(){
        Integer[] area = new Integer[5];
        for (int i = 0; i < 5; i++) {

            area[i] = (i + 1);
        }
        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, area);
        listViewArea.setAdapter(arrayAdapter);
    }

    private void loadListAccountingType(){
        String[] accountingType = new String[2];
        accountingType[0] = "провод";
        accountingType[1] = "непровод";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountingType);
        listViewAccountingType.setAdapter(arrayAdapter);
    }

    private void loadListDayOfTheWeek(){
        String[] dayOfTheWeek = new String[4];
        dayOfTheWeek[0] = "понедельник-четверг";
        dayOfTheWeek[1] = "вторник-пятница";
        dayOfTheWeek[2] = "среда";
        dayOfTheWeek[3] = "любой";
        dayOfTheWeek[2] = "север";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dayOfTheWeek);
        listViewDayOfTheWeek.setAdapter(arrayAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:
                filterFirst();
                break;
            default:
                break;
        }
    }

    private void filterFirst(){
        e = sPrefArea.edit();
        e.putString(SAVED_AREA, areaStr);
        e.apply();
        e = sPrefDayOfTheWeek.edit();
        e.putString(SAVED_DAYOFTHEWEEK, dayOfTheWeek);
        e.apply();
        e = sPrefAccountingType.edit();
        e.putString(SAVED_ACCOUNTINGTYPE, accountingType);
        e.apply();

        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterSecondActivity.class);
        TextView textView = findViewById(R.id.textViewAgent);
        String agentName = textView.getText().toString();
        intent.putExtra(EXTRA_AGENT, agentName);
        startActivity(intent);
    }
}
