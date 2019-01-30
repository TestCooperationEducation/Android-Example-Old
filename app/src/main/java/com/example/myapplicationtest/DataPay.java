package com.example.myapplicationtest;

public class DataPay {
    private Integer invoiceNumber;
    private Double payment;
    private Integer paymentID;

    public DataPay(Integer invoiceNumber, Double payment, Integer paymentID) {
        this.invoiceNumber = invoiceNumber;
        this.payment = payment;
        this.paymentID = paymentID;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Double getPayment() {
        return payment;
    }

    public void setPayment(Double payment) {
        this.payment = payment;
    }

    public Integer getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(Integer paymentID) {
        this.paymentID = paymentID;
    }
}
