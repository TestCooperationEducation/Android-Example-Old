package com.example.myapplicationtest;

public class DataInvoice {
    private String salesPartner, accountingType, item;
    private Double price, quantity, totalCost, exchange, returns, invoiceSum;

    public DataInvoice(String salesPartner, String accountingType, String item, Double price,
                       Double quantity, Double totalCost, Double exchange, Double returns, Double invoiceSum) {
        this.salesPartner = salesPartner;
        this.accountingType = accountingType;
        this.item = item;
        this.price = price;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.exchange = exchange;
        this.returns = returns;
        this.invoiceSum = invoiceSum;
    }

    public String getSalesPartner() {
        return salesPartner;
    }

    public void setSalesPartner(String salesPartner) {
        this.salesPartner = salesPartner;
    }

    public String getAccountingType() {
        return accountingType;
    }

    public void setAccountingType(String accountingType) {
        this.accountingType = accountingType;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Double getExchange() {
        return exchange;
    }

    public void setExchange(Double exchange) {
        this.exchange = exchange;
    }

    public Double getReturns() {
        return returns;
    }

    public void setReturns(Double returns) {
        this.returns = returns;
    }

    public Double getInvoiceSum() {
        return invoiceSum;
    }

    public void setInvoiceSum(Double invoiceSum) {
        this.invoiceSum = invoiceSum;
    }
}
