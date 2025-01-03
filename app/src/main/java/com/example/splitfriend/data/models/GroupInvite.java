package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class GroupInvite implements Serializable {
    @DocumentId private String id;
    private String groupId;
    private String sendUserId;
    private InviteEnum status;

    public GroupInvite() {}

    public GroupInvite(String groupId, String sendUserId) {
        this.groupId = groupId;
        this.sendUserId = sendUserId;
        this.status = InviteEnum.PENDING;
    }

    public GroupInvite(String groupId, String sendUserId, InviteEnum status) {
        this.groupId = groupId;
        this.sendUserId = sendUserId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public InviteEnum getStatus() {
        return status;
    }

    public void setStatus(InviteEnum status) {
        this.status = status;
    }
}
