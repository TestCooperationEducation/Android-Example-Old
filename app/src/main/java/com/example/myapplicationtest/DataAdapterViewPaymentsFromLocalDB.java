package com.example.myapplicationtest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class DataAdapterViewPaymentsFromLocalDB extends RecyclerView.Adapter<DataAdapterViewPaymentsFromLocalDB.ViewHolder> {
    private LayoutInflater inflater;
    private List<DataPaymentLocal> listTmp;

    DataAdapterViewPaymentsFromLocalDB(Context context, List<DataPaymentLocal> listTmp) {
        this.listTmp = listTmp;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public DataAdapterViewPaymentsFromLocalDB.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_payments_local, parent, false);
        return new DataAdapterViewPaymentsFromLocalDB.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapterViewPaymentsFromLocalDB.ViewHolder holder, int position) {
        DataPaymentLocal paymentLocal = listTmp.get(position);
        holder.salesPartnerNameView.setText(paymentLocal.getSalesPartnerName());
        holder.accountingTypeView.setText(paymentLocal.getAccountingType());
        holder.invoiceNumberView.setText(paymentLocal.getInvoiceNumber().toString());
        holder.paymentIDLocalView.setText(paymentLocal.getPaymentIDLocal().toString());
        holder.dateTimeDocLocalView.setText(paymentLocal.getDateTimeDocLocal());
        holder.invoiceSumView.setText(paymentLocal.getInvoiceSum().toString());
        holder.paymentSumView.setText(paymentLocal.getPaymentSum().toString());
    }

    @Override
    public int getItemCount() {
        return listTmp.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView salesPartnerNameView, accountingTypeView, invoiceNumberView, paymentIDLocalView, dateTimeDocServerView,
                dateTimeDocLocalView, invoiceSumView, paymentSumView;
        ViewHolder(View view){
            super(view);
            salesPartnerNameView = view.findViewById(R.id.salesPartner);
            accountingTypeView = view.findViewById(R.id.accountingType);
            invoiceNumberView = view.findViewById(R.id.invoiceNumber);
            paymentIDLocalView = view.findViewById(R.id.paymentIDLocal);
            dateTimeDocServerView = view.findViewById(R.id.dateTimeDocServer);
            dateTimeDocLocalView = view.findViewById(R.id.dateTimeDocLocal);
            invoiceSumView = view.findViewById(R.id.invoiceSum);
            paymentSumView = view.findViewById(R.id.paymentSum);
        }
    }
}
