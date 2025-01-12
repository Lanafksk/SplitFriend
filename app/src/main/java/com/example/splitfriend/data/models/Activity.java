package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity implements Serializable {
    @DocumentId
    private String id;
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
    private List<Map<String, String>> paymentStatusesId; // Updated type

    // Default constructor
    public Activity() {}

    // Constructor without paymentStatusesId
    public Activity(String id, String groupId, Date date, String name, String currency, String payee, String bankName,
                    String bankAccount, List<Bill> bills, String creatorId) {
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

    // Constructor with paymentStatusesId
    public Activity(String id, String groupId, Date date, String name, String currency, String payee, String bankName,
                    String bankAccount, List<Bill> bills, Double totalAmount, String creatorId,
                    List<String> participantsId, List<Map<String, String>> paymentStatusesId) {
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

    // Getters and setters
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

    public List<Map<String, String>> getPaymentStatusesId() {
        return paymentStatusesId;
    }

    public void setPaymentStatusesId(List<Map<String, String>> paymentStatusesId) {
        this.paymentStatusesId = paymentStatusesId;
    }

    // Add a participant with a default status
    public void addParticipantWithStatus(String userId, String status) {
        if (this.paymentStatusesId == null) {
            this.paymentStatusesId = new ArrayList<>();
        }

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("userId", userId);
        statusMap.put("status", status);
        this.paymentStatusesId.add(statusMap);
    }

    // Update payment status for a specific user
    public void updatePaymentStatus(String userId, String status) {
        if (this.paymentStatusesId != null) {
            for (Map<String, String> statusMap : this.paymentStatusesId) {
                if (statusMap.get("userId").equals(userId)) {
                    statusMap.put("status", status);
                    break;
                }
            }
        }
    }

    // Calculate total amount
    private Double calculateTotalAmount() {
        if (bills == null || bills.isEmpty()) {
            return 0.0;
        }
        return bills.stream().mapToDouble(Bill::getPrice).sum();
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
