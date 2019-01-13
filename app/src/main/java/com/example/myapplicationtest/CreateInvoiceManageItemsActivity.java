package com.example.myapplicationtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class CreateInvoiceManageItemsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_manage_items);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewItems);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
