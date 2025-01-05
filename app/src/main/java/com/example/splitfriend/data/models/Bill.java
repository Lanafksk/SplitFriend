package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

// Bill for now wont have Image or we can use image URL instead for now
public class Bill implements Serializable {
    @DocumentId private String id;
    private double billAmount;
    private String paidBy;
    private List<String> sharedBy;

    public Bill() {}

    public Bill(double billAmount, String paidBy, List<String> sharedBy) {
        this.billAmount = billAmount;
        this.paidBy = paidBy;
        this.sharedBy = sharedBy;
    }

    public String getId() {
        return id;
    }

    public double getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(double billAmount) {
        this.billAmount = billAmount;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public List<String> getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(List<String> sharedBy) {
        this.sharedBy = sharedBy;
    }
}
