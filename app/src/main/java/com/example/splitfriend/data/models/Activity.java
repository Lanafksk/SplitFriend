package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Activity {
    @DocumentId private String id;
    private String name;
    private Double amount;
    private List<String> categoriesId;
    private List<String> paymentStatusesId;

    public Activity() {}

    public Activity(String name, Double amount) {
        this.name = name;
        this.amount = amount;
        this.categoriesId = new ArrayList<>();
        this.paymentStatusesId = new ArrayList<>();
    }

    public Activity(String name, Double amount, List<String> categoriesId, List<String> paymentStatusesId) {
        this.name = name;
        this.amount = amount;
        this.categoriesId = categoriesId;
        this.paymentStatusesId = paymentStatusesId;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public List<String> getCategoriesId() {
        return categoriesId;
    }

    public void setCategoriesId(List<String> categoriesId) {
        this.categoriesId = categoriesId;
    }

    public List<String> getPaymentStatusesId() {
        return paymentStatusesId;
    }

    public void setPaymentStatusesId(List<String> paymentStatusesId) {
        this.paymentStatusesId = paymentStatusesId;
    }
}
