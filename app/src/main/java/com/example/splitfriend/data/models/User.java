package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {
    @DocumentId private String id;
    private String email;
    private String userId;
    private String name;
    private String role;

    public User() {}
    public User(String email, String userId, String password, String name) {
        this.email = email;
        this.userId = userId;
        this.name = name;
        this.role = "user";
    }

    public User(String id,String email, String userId, String password, String name) {
        this.id = id;
        this.email = email;
        this.userId = userId;
        this.name = name;
        this.role = "user";
    }

    public User(String email, String userId,String password, String name, String bankAccountNumber, String bankName, String role) {
        this.email = email;
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}


