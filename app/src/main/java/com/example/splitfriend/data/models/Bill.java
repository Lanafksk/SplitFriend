package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

// Bill for now wont have Image or we can use image URL instead for now
public class Bill implements Serializable {
    @DocumentId private String id;
    private String note;
    private double price;
    private String category;
    private String paidBy;
    private List<String> sharedBy;

    public Bill() {}

//    public Bill(String id, String note, double price, String category, String paidBy, List<String> sharedBy) {
//        this.id = id;
//        this.note = note;
//        this.price = price;
//        this.category = category;
//        this.paidBy = paidBy;
//        this.sharedBy = sharedBy;
//    }
//
    public Bill(String note, double price, String category, String paidBy) {
        this.note = note;
        this.price = price;
        this.category = category;
        this.paidBy = paidBy;
    }

//    public Bill(String note, double price, String paidBy) {
//        this.note = note;
//        this.price = price;
//        this.paidBy = paidBy;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
