package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

// Bill for now wont have Image or we can use image URL instead for now
public class Bill  {
    private String note;
    private double price;
    private String category;

    public Bill() {}

    public Bill(String note, double price, String category) {
        this.note = note;
        this.price = price;
        this.category = category;
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
}
