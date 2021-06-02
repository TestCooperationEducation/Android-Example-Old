package com.example.myapplicationtest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class DataAdapterViewInvoicesFromLocalDB extends RecyclerView.Adapter<DataAdapterViewInvoicesFromLocalDB.ViewHolder> {
    private LayoutInflater inflater;
    private List<DataInvoiceLocal> listTmp;

    DataAdapterViewInvoicesFromLocalDB(Context context, List<DataInvoiceLocal> listTmp) {
        this.listTmp = listTmp;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public DataAdapterViewInvoicesFromLocalDB.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_invoices_local, parent, false);
        return new DataAdapterViewInvoicesFromLocalDB.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapterViewInvoicesFromLocalDB.ViewHolder holder, int position) {
        DataInvoiceLocal invoiceLocal = listTmp.get(position);
        holder.salesPartnerNameView.setText(invoiceLocal.getSalesPartnerName());
        holder.accountingTypeView.setText(invoiceLocal.getAccountingType());
        holder.invoiceNumberServerView.setText(invoiceLocal.getInvoiceNumberServer().toString());
        holder.dateTimeDocServerView.setText(invoiceLocal.getDateTimeDocServer());
        holder.dateTimeDocLocalView.setText(invoiceLocal.getDateTimeDocLocal());
        holder.invoiceSumView.setText(invoiceLocal.getInvoiceSum().toString());
        holder.surplusView.setText(invoiceLocal.getSurplus().toString());
        holder.paymentStatusView.setText(invoiceLocal.getPaymentStatus());
    }

    @Override
    public int getItemCount() {
        return listTmp.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView salesPartnerNameView, accountingTypeView, invoiceNumberServerView, dateTimeDocServerView,
                dateTimeDocLocalView, invoiceSumView, surplusView, paymentStatusView;
        ViewHolder(View view){
            super(view);
            salesPartnerNameView = view.findViewById(R.id.salesPartner);
            accountingTypeView = view.findViewById(R.id.accountingType);
            invoiceNumberServerView = view.findViewById(R.id.invoiceNumberFromServer);
            dateTimeDocServerView = view.findViewById(R.id.dateTimeDocServer);
            dateTimeDocLocalView = view.findViewById(R.id.dateTimeDocLocal);
            invoiceSumView = view.findViewById(R.id.invoiceSum);
            surplusView = view.findViewById(R.id.surplusLocal);
            paymentStatusView = view.findViewById(R.id.paymentStatus);
        }
    }
}
