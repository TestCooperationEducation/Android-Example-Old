package com.example.myapplicationtest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    private ArrayList<String> itemNames = new ArrayList<>();
    private ArrayList<Double> itemPrices = new ArrayList<>();
    private ArrayList<Double> itemQuantity = new ArrayList<>();
    private ArrayList<Double> itemExchange = new ArrayList<>();
    private ArrayList<Double> itemReturn = new ArrayList<>();
    private Context mContext;

    public ItemsAdapter(ArrayList<String> itemNames, ArrayList<Double> itemPrices,
                        ArrayList<Double> itemQuantity, ArrayList<Double> itemExchange,
                        ArrayList<Double> itemReturn, Context mContext) {
        this.itemNames = itemNames;
        this.itemPrices = itemPrices;
        this.itemQuantity = itemQuantity;
        this.itemExchange = itemExchange;
        this.itemReturn = itemReturn;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends  RecyclerView.ViewHolder {
        private TextView textViewItem;
        private TextView textViewPrice;
        private EditText editTextQuantity;
        private EditText editTextExchange;
        private EditText editTextReturn;
        private ConstraintLayout parent_layout;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewItem = itemView.findViewById(R.id.textViewItem);
            textViewPrice = itemView.findViewById(R.id.editTextViewPrice);
            editTextQuantity = itemView.findViewById(R.id.editTextQuantity);
            editTextExchange = itemView.findViewById(R.id.editTextExchange);
            editTextReturn = itemView.findViewById(R.id.editTextReturn);
            parent_layout = itemView.findViewById(R.id.parent_layout);

        }
    }
}
