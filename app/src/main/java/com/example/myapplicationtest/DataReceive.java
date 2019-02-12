package com.example.myapplicationtest;

public class DataReceive {

    private Integer itemID, agentID;
    private String dateTimeDoc;
    private Double quantity;

    public DataReceive(Integer itemID, Integer agentID, String dateTimeDoc, Double quantity) {
        this.itemID = itemID;
        this.agentID = agentID;
        this.dateTimeDoc = dateTimeDoc;
        this.quantity = quantity;
    }

    public Integer getItemID() {
        return itemID;
    }

    public void setItemID(Integer itemID) {
        this.itemID = itemID;
    }

    public Integer getAgentID() {
        return agentID;
    }

    public void setAgentID(Integer agentID) {
        this.agentID = agentID;
    }

    public String getDateTimeDoc() {
        return dateTimeDoc;
    }

    public void setDateTimeDoc(String dateTimeDoc) {
        this.dateTimeDoc = dateTimeDoc;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
}
