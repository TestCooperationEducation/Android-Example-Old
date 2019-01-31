package com.example.myapplicationtest;

public class DataPaymentLocal {
    private String salesPartnerName;
    private String accountingType;
    private Integer invoiceNumber;
    private Integer paymentIDLocal;
    private Double invoiceSum;
    private Double paymentSum;
    private String dateTimeDocLocal;
    private String dateTimeDocServer;

    public DataPaymentLocal(String salesPartnerName, String accountingType, Integer invoiceNumber,
                            Integer paymentIDLocal, Double invoiceSum, Double paymentSum, String dateTimeDocLocal,
                            String dateTimeDocServer) {
        this.salesPartnerName = salesPartnerName;
        this.accountingType = accountingType;
        this.invoiceNumber = invoiceNumber;
        this.paymentIDLocal = paymentIDLocal;
        this.invoiceSum = invoiceSum;
        this.paymentSum = paymentSum;
        this.dateTimeDocLocal = dateTimeDocLocal;
        this.dateTimeDocServer = dateTimeDocServer;
    }

    public String getSalesPartnerName() {
        return salesPartnerName;
    }

    public void setSalesPartnerName(String salesPartnerName) {
        this.salesPartnerName = salesPartnerName;
    }

    public String getAccountingType() {
        return accountingType;
    }

    public void setAccountingType(String accountingType) {
        this.accountingType = accountingType;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Integer getPaymentIDLocal() {
        return paymentIDLocal;
    }

    public void setPaymentIDLocal(Integer paymentIDLocal) {
        this.paymentIDLocal = paymentIDLocal;
    }

    public Double getInvoiceSum() {
        return invoiceSum;
    }

    public void setInvoiceSum(Double invoiceSum) {
        this.invoiceSum = invoiceSum;
    }

    public Double getPaymentSum() {
        return paymentSum;
    }

    public void setPaymentSum(Double paymentSum) {
        this.paymentSum = paymentSum;
    }

    public String getDateTimeDocLocal() {
        return dateTimeDocLocal;
    }

    public void setDateTimeDocLocal(String dateTimeDocLocal) {
        this.dateTimeDocLocal = dateTimeDocLocal;
    }

    public String getDateTimeDocServer() {
        return dateTimeDocServer;
    }

    public void setDateTimeDocServer(String dateTimeDocServer) {
        this.dateTimeDocServer = dateTimeDocServer;
    }
}
