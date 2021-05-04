package com.example.myapplicationtest;

public class DataItemsListTmp {

    private Double exchange;
    private String itemName;
    private Integer price;
    private Double quantity;
    private Double surplus;
    private Double total;
    private Double returnQuantity;

    public DataItemsListTmp(Double exchange, String itemName, Integer price, Double quantity, Double surplus, Double total, Double returnQuantity) {
        this.exchange = exchange;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
        this.surplus = surplus;
        this.total = total;
        this.returnQuantity = returnQuantity;
    }

    public Double getExchange() {
        return exchange;
    }

    public void setExchange(Double exchange) {
        this.exchange = exchange;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getSurplus() {
        return surplus;
    }

    public void setSurplus(Double surplus) {
        this.surplus = surplus;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(Double returnQuantity) {
        this.returnQuantity = returnQuantity;
    }
}
