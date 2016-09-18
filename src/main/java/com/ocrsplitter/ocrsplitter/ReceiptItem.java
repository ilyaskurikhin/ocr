package com.ocrsplitter.ocrsplitter;

/**
 * Created by eson on 9/17/16.
 */
public class ReceiptItem {

    private String name;
    private double price;

    public ReceiptItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public void getName(){
        return name;
    }

    public void getPrice(){
        return price;
    }
}
