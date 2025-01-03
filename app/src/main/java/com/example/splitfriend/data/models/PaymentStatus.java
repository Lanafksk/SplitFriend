package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class PaymentStatus implements Serializable {
    @DocumentId private String id;
    private String userId;
    private StatusEnum status;

    public PaymentStatus() {}

    public PaymentStatus(String userId) {
        this.userId = userId;
        this.status = StatusEnum.PENDING;
    }

    public PaymentStatus(String userId, StatusEnum status) {
        this.userId = userId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }
}

