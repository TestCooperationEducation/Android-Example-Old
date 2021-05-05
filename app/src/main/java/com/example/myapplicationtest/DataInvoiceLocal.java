package com.example.myapplicationtest;

public class DataInvoiceLocal {
    private String salesPartnerName;
    private String accountingType;
    private Integer invoiceNumberServer;
    private String dateTimeDocServer;
    private String dateTimeDocLocal;
    private Double invoiceSum;
    private Double surplus;
    private String paymentStatus;

    public DataInvoiceLocal(String salesPartnerName, String accountingType,
                            Integer invoiceNumberServer, String dateTimeDocServer,
                            String dateTimeDocLocal, Double invoiceSum, Double surplus, String paymentStatus) {
        this.salesPartnerName = salesPartnerName;
        this.accountingType = accountingType;
        this.invoiceNumberServer = invoiceNumberServer;
        this.dateTimeDocServer = dateTimeDocServer;
        this.dateTimeDocLocal = dateTimeDocLocal;
        this.invoiceSum = invoiceSum;
        this.surplus = surplus;
        this.paymentStatus = paymentStatus;
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

    public Integer getInvoiceNumberServer() {
        return invoiceNumberServer;
    }

    public void setInvoiceNumberServer(Integer invoiceNumberServer) {
        this.invoiceNumberServer = invoiceNumberServer;
    }

    public String getDateTimeDocServer() {
        return dateTimeDocServer;
    }

    public void setDateTimeDocServer(String dateTimeDocServer) {
        this.dateTimeDocServer = dateTimeDocServer;
    }

    public String getDateTimeDocLocal() {
        return dateTimeDocLocal;
    }

    public void setDateTimeDocLocal(String dateTimeDocLocal) {
        this.dateTimeDocLocal = dateTimeDocLocal;
    }

    public Double getInvoiceSum() {
        return invoiceSum;
    }

    public void setInvoiceSum(Double invoiceSum) {
        this.invoiceSum = invoiceSum;
    }

    public Double getSurplus() {
        return surplus;
    }

    public void setSurplus(Double surplus) {
        this.surplus = surplus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
