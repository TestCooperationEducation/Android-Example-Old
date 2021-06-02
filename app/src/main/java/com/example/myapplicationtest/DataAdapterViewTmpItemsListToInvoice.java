package com.example.myapplicationtest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class DataAdapterViewTmpItemsListToInvoice extends RecyclerView.Adapter<DataAdapterViewTmpItemsListToInvoice.ViewHolder> {
    private LayoutInflater inflater;
    private List<DataItemsListTmp> listTmp;

    DataAdapterViewTmpItemsListToInvoice(Context context, List<DataItemsListTmp> listTmp) {
        this.listTmp = listTmp;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public DataAdapterViewTmpItemsListToInvoice.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_tmp, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataItemsListTmp itemsListTmp = listTmp.get(position);
        holder.itemNameView.setText(itemsListTmp.getItemName());
        holder.itemNameView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        holder.exchangeView.setText(itemsListTmp.getExchange().toString());
        holder.exchangeView.setTextColor(Color.parseColor("Black"));
        holder.priceView.setText(itemsListTmp.getPrice().toString());
        holder.quantityView.setText(itemsListTmp.getQuantity().toString());
        holder.quantityView.setTextColor(Color.parseColor("Black"));
        holder.surplusView.setText(itemsListTmp.getSurplus().toString());
        holder.surplusView.setTextColor(Color.parseColor("Black"));
        holder.totalView.setText(itemsListTmp.getTotal().toString());
        holder.returnQuantityView.setText(itemsListTmp.getReturnQuantity().toString());
    }

    @Override
    public int getItemCount() {
        return listTmp.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView itemNameView, exchangeView, priceView, quantityView, surplusView, totalView, returnQuantityView;
        ViewHolder(View view){
            super(view);
            itemNameView = view.findViewById(R.id.itemName);
            exchangeView = view.findViewById(R.id.exchange);
            priceView = view.findViewById(R.id.price);
            quantityView = view.findViewById(R.id.quantity);
            surplusView = view.findViewById(R.id.surplus);
            totalView = view.findViewById(R.id.total);
            returnQuantityView = view.findViewById(R.id.returnQuantity);
        }
    }
}
