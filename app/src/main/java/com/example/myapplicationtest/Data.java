package com.example.myapplicationtest;

public class Data {
    String agentName, salesPartner, accountingType, item, price, quantity, totalCost,
            exchange, returns;

    public Data(String agentName, String salesPartner, String accountingType, String item,
                String price, String quantity, String totalCost, String exchange,
                String returns) {
        this.agentName = agentName;
        this.salesPartner = salesPartner;
        this.accountingType = accountingType;
        this.item = item;
        this.price = price;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.exchange = exchange;
        this.returns = returns;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getReturns() {
        return returns;
    }

    public void setReturns(String returns) {
        this.returns = returns;
    }
}
