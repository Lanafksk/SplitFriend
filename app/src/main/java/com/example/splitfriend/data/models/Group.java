package com.example.splitfriend.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;


public class Group {
    @DocumentId private String id;
    private String name;
    private List<String> membersId;
    private String leaderId;
    private List<String> activitiesId;
    private String inviteCode;

    public Group() {}
    public Group(String name, String leaderId, String inviteCode) {
        this.name = name;
        this.membersId = new ArrayList<>();
        membersId.add(leaderId);
        this.leaderId = leaderId;
        this.activitiesId = new ArrayList<>();
        this.inviteCode = inviteCode;
    }
    public Group(String name, List<String> membersId, String leaderId, List<String> activitiesId, String inviteCode) {
        this.name = name;
        this.membersId = membersId;
        this.leaderId = leaderId;
        this.activitiesId = activitiesId;
        this.inviteCode = inviteCode;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembersId() {
        return membersId;
    }

    public void setMembersId(List<String> membersId) {
        this.membersId = membersId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public List<String> getActivitiesId() {
        return activitiesId;
    }

    public void setActivitiesId(List<String> activitiesId) {
        this.activitiesId = activitiesId;
    }
}
