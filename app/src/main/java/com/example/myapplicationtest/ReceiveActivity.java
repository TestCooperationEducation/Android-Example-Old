package com.example.myapplicationtest;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ReceiveActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnReceiveNew, btnReceiveAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        btnReceiveAll = findViewById(R.id.buttonAllReceives);
        btnReceiveNew = findViewById(R.id.buttonNewReceive);
        btnReceiveAll.setOnClickListener(this);
        btnReceiveNew.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAllReceives:

                break;
            case R.id.buttonNewReceive:
                Intent intent = new Intent(getApplicationContext(), ReceiveNewActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
