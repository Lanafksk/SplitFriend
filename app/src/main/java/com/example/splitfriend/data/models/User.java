package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {
    @DocumentId private String id;
    private String email;
    private String userId;
    private String password;
    private String name;
    private String bankAccountNumber;
    private String bankName;

    private String role;

    public User() {}
    public User(String email, String userId, String password, String name) {
        this.email = email;
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.bankAccountNumber = "";
        this.bankName = "";
        this.role = "user";
    }

    public User(String id,String email, String userId, String password, String name) {
        this.id = id;
        this.email = email;
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.bankAccountNumber = "";
        this.bankName = "";
        this.role = "user";
    }

    public User(String email, String userId,String password, String name, String bankAccountNumber, String bankName, String role) {
        this.email = email;
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.bankAccountNumber = bankAccountNumber;
        this.bankName = bankName;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}


