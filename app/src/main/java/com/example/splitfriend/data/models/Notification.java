package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

public class Notification implements Serializable {
    @DocumentId private String id;
    private String triggeredBy;
    private List<String> targetUsersId;
    private String message;

    public Notification() {}

    public Notification(String triggeredBy, List<String> targetUsersId, String message) {
        this.triggeredBy = triggeredBy;
        this.targetUsersId = targetUsersId;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public List<String> getTargetUsersId() {
        return targetUsersId;
    }

    public void setTargetUsersId(List<String> targetUsersId) {
        this.targetUsersId = targetUsersId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
