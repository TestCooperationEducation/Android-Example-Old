package com.example.myapplicationtest;

public class DataPay {
    private Integer invoiceNumber;
    private Double payment;

    public DataPay(Integer invoiceNumber, Double payment) {
        this.invoiceNumber = invoiceNumber;
        this.payment = payment;
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
}
