package com.example.splitfriend.data.helpers;

import com.example.splitfriend.data.models.GroupInvite;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class GroupInviteHelper {
    private FirebaseFirestore db;
    private ListenerRegistration listener;

    public GroupInviteHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // listener
    public void setupRealtimeUpdates(EventListener<QuerySnapshot> listener) {
        this.listener = db.collection("groupInvites").addSnapshotListener(listener);
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    // crud
    public Task<DocumentReference> createGroupInvite(GroupInvite groupInvite) {
        return db.collection("groupInvites").add(groupInvite).addOnFailureListener(
                e -> System.out.println("Error creating group invite: " + e.getMessage()));
    }

    public Task<Void> updateGroupInvite(GroupInvite groupInvite) {
        return db.collection("groupInvites").document(groupInvite.getId()).update(
                "groupId", groupInvite.getGroupId(),
                "sendUserId", groupInvite.getSendUserId(),
                "status", groupInvite.getStatus()
        ).addOnFailureListener(
                e -> System.out.println("Error updating group invite: " + e.getMessage()));
    }

    public Task<Void> deleteGroupInvite(String groupInviteId) {
        return db.collection("groupInvites").document(groupInviteId).delete().addOnFailureListener(
                e -> System.out.println("Error deleting group invite: " + e.getMessage()));
    }
}