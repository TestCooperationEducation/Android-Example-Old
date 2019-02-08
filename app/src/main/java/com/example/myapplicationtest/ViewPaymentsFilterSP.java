package com.example.myapplicationtest;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPaymentsFilterSP extends AppCompatActivity implements View.OnClickListener {

    TextView textViewDebtor;
    EditText editTextPaymentSum;
    Button btnNext, btnFilter, btnShowMore;
    ListView listViewDebts;
    ArrayAdapter<String> arrayAdapter;
    Boolean itemChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_payments_filter_sp);

        textViewDebtor = findViewById(R.id.textViewDebtor);
        editTextPaymentSum = findViewById(R.id.editTextPaymentSum);
        listViewDebts = findViewById(R.id.listViewDebts);

        btnNext = findViewById(R.id.buttonNext);
        btnNext.setOnClickListener(this);
        btnFilter = findViewById(R.id.buttonFilter);
        btnFilter.setOnClickListener(this);
        btnShowMore = findViewById(R.id.buttonShowMore);
        btnShowMore.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:

                break;
            case R.id.buttonFilter:
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Выберите для удаления")
                        .setCancelable(false)
                        .setNeutralButton("Назад",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton("Удалить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (itemChecked == true) {

                                            Toast.makeText(getApplicationContext(), "<<< Запись " +
                                                    sPListTmp[itemTmp] + " удалeна >>>", Toast.LENGTH_SHORT).show();
                                            itemChecked = false;
                                            dialog.cancel();
                                        }
                                    }
                                })
                        .setPositiveButton("Уйти",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                        dialog.cancel();
                                    }
                                })
                        .setSingleChoiceItems(sPListTmp, -1,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int item) {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "Вы выбрали: "
                                                        + sPListTmp[item],
                                                Toast.LENGTH_SHORT).show();
                                        itemTmp = item;
                                        itemChecked = true;
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.buttonShowMore:

                break;
            default:
                break;
        }
    }
}
