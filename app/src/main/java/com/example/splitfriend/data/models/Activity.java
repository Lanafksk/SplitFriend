package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Activity implements Serializable {
    @DocumentId private String id;
    private String name;
    private Double amount;
    private Date date;
    private String creatorId;
    private List<String> participantsId;
    private List<String> categoriesId;
    private List<String> paymentStatusesId;

    public Activity() {}

    public Activity(String name, Double amount, Date date, String creatorId) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.creatorId = creatorId;
        this.participantsId = new ArrayList<>();
        this.categoriesId = new ArrayList<>();
        this.paymentStatusesId = new ArrayList<>();
    }

    public Activity(String name, Double amount, Date date, String creatorId, List<String> participantsId, List<String> categoriesId, List<String> paymentStatusesId) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.creatorId = creatorId;
        this.participantsId = participantsId;
        this.categoriesId = categoriesId;
        this.paymentStatusesId = paymentStatusesId;
    }

    public String getId() {
        return id;
    }

    public String getName() {return name;}

    public void setName(String name) {
        this.name = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getDate() {return date;}

    public void setDate(Date date) {this.date = date;}

    public String getCreatorId() {return creatorId;}

    public void setCreatorId(String creatorId) {this.creatorId = creatorId;}

    public List<String> getParticipantsId() {
        return participantsId;
    }

    public void setParticipantsId(List<String> participantsId) {
        this.participantsId = participantsId;
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
