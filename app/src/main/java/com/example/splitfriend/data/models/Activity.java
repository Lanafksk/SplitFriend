package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Activity implements Serializable {
    @DocumentId private String id;
    private String groupId;
    private Date date;
    private String name;
    private String currency;
    private String payee;
    private String bankName;
    private String bankAccount;
    private List<Bill> bills;

    private Double totalAmount;
    private String creatorId;
    private List<String> participantsId;
    private List<String> paymentStatusesId;

    // Default constructor
    public Activity() {}

    public Activity(String id, String groupId, Date date, String name, String currency, String payee, String bankName, String bankAccount, List<Bill> bills, String creatorId) {
        this.id = id;
        this.groupId = groupId;
        this.date = date;
        this.name = name;
        this.currency = currency;
        this.payee = payee;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.bills = bills;
        this.totalAmount = calculateTotalAmount();
        this.creatorId = creatorId;
        this.participantsId = new ArrayList<>();
        this.paymentStatusesId = new ArrayList<>();
    }

    public Activity(String id, String groupId, Date date, String name, String currency, String payee, String bankName, String bankAccount, List<Bill> bills, Double totalAmount, String creatorId, List<String> participantsId, List<String> paymentStatusesId) {
        this.id = id;
        this.groupId = groupId;
        this.date = date;
        this.name = name;
        this.currency = currency;
        this.payee = payee;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.bills = bills;
        this.totalAmount = totalAmount;
        this.creatorId = creatorId;
        this.participantsId = participantsId;
        this.paymentStatusesId = paymentStatusesId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public List<String> getParticipantsId() {
        return participantsId;
    }

    public void setParticipantsId(List<String> participantsId) {
        this.participantsId = participantsId;
    }

    public List<String> getPaymentStatusesId() {
        return paymentStatusesId;
    }

    public void setPaymentStatusesId(List<String> paymentStatusesId) {
        this.paymentStatusesId = paymentStatusesId;
    }

    private Double calculateTotalAmount() {
        if (bills == null || bills.isEmpty()) {
            return 0.0;
        }
        return bills.stream().mapToDouble(Bill::getPrice).sum();
    }
}
