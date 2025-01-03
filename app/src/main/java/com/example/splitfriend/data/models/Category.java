package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Category implements Serializable {
    @DocumentId private String id;
    private String name;
    private double totalAmount;
    private List<String> billsId;

    public Category() {}

    public Category(String name) {
        this.name = name;
        this.totalAmount = 0;
        this.billsId = new ArrayList<>();
    }

    public Category(String name, double totalAmount, List<String> billsId) {
        this.name = name;
        this.totalAmount = totalAmount;
        this.billsId = billsId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<String> getBillsId() {
        return billsId;
    }

    public void setBillsId(List<String> billsId) {
        this.billsId = billsId;
    }
}
