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

import java.util.Date;

public class MakePaymentsActivity extends AppCompatActivity implements View.OnClickListener{

    ListView listViewAccountingType, listViewDebtors;
    String accountingType, dbName, dbUser, dbPassword, debtor,
            requestUrl = "https://caiman.ru.com/php/loadDebtors.php";
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    Button btnReceiveList;
    Date dateStart, dateEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payments);

        Intent intent = getIntent();
        String agentName = intent.getStringExtra(MainMenu.EXTRA_AGENTNAMENEXT);
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);

        btnReceiveList = findViewById(R.id.buttonReceiveList);
        btnReceiveList.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");

        listViewAccountingType = findViewById(R.id.listViewAccountingType);
        listViewDebtors  = findViewById(R.id.listViewDebtors);

        loadListAccountingType();

        listViewAccountingType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                accountingType = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Тип учета :" + accountingType, Toast.LENGTH_SHORT).show();
            }
        });

        listViewDebtors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                debtor = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), "Контрагент :" + debtor, Toast.LENGTH_SHORT).show();
                String[] tmpDebtor = new String[1];
                tmpDebtor[0] = debtor;
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, tmpDebtor);
                listViewAccountingType.setAdapter(arrayAdapter);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonReceiveList:
                receiveList();
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

    private void receiveList(){

    }
}
