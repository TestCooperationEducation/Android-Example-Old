package com.example.myapplicationtest;

public class DataInvoice {
    private String accountingTypeDoc, accountingTypeSP, itemName, dateTimeDocLocal, comment;
    private Integer salesPartnerID, invoiceNumber, agentID, areaSP;
    private Double price, quantity, totalCost, exchange, returns, surplus, invoiceSum;

    public String getAccountingTypeDoc() {
        return accountingTypeDoc;
    }

    public void setAccountingTypeDoc(String accountingTypeDoc) {
        this.accountingTypeDoc = accountingTypeDoc;
    }

    public String getAccountingTypeSP() {
        return accountingTypeSP;
    }

    public void setAccountingTypeSP(String accountingTypeSP) {
        this.accountingTypeSP = accountingTypeSP;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDateTimeDocLocal() {
        return dateTimeDocLocal;
    }

    public void setDateTimeDocLocal(String dateTimeDocLocal) {
        this.dateTimeDocLocal = dateTimeDocLocal;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getSalesPartnerID() {
        return salesPartnerID;
    }

    public void setSalesPartnerID(Integer salesPartnerID) {
        this.salesPartnerID = salesPartnerID;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Integer getAgentID() {
        return agentID;
    }

    public void setAgentID(Integer agentID) {
        this.agentID = agentID;
    }

    public Integer getAreaSP() {
        return areaSP;
    }

    public void setAreaSP(Integer areaSP) {
        this.areaSP = areaSP;
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

    public Double getSurplus() {
        return surplus;
    }

    public void setSurplus(Double surplus) {
        this.surplus = surplus;
    }

    public Double getInvoiceSum() {
        return invoiceSum;
    }

    public void setInvoiceSum(Double invoiceSum) {
        this.invoiceSum = invoiceSum;
    }

    public DataInvoice(String accountingTypeDoc, String accountingTypeSP, String itemName, String dateTimeDocLocal,
                       String comment, Integer salesPartnerID, Integer invoiceNumber, Integer agentID,
                       Integer areaSP, Double price, Double quantity, Double totalCost, Double exchange,
                       Double returns, Double surplus, Double invoiceSum) {
        this.accountingTypeDoc = accountingTypeDoc;
        this.accountingTypeSP = accountingTypeSP;
        this.itemName = itemName;
        this.dateTimeDocLocal = dateTimeDocLocal;
        this.comment = comment;
        this.salesPartnerID = salesPartnerID;
        this.invoiceNumber = invoiceNumber;
        this.agentID = agentID;
        this.areaSP = areaSP;
        this.price = price;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.exchange = exchange;
        this.returns = returns;
        this.surplus = surplus;
        this.invoiceSum = invoiceSum;

    }
}
