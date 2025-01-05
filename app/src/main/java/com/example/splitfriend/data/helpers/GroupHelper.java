package com.example.splitfriend.data.helpers;

import com.example.splitfriend.data.models.Group;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class GroupHelper {
    private final FirebaseFirestore db;
    private ListenerRegistration listener;

    public GroupHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // listeners
    public void setupRealtimeUpdates(EventListener<QuerySnapshot> listener) {
        this.listener = db.collection("groups").addSnapshotListener(listener);
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    //crud
    public Task<DocumentReference> createGroup(Group group) {
        return db.collection("groups").add(group).addOnFailureListener(
                e -> System.out.println("Error creating group: " + e.getMessage()));
    }

    public Task<Void> updateGroup(Group group) {
        return db.collection("groups").document(group.getId()).update(
                "name", group.getName(),
                "membersId", group.getMembersId(),
                "leaderId", group.getLeaderId(),
                "activitiesId", group.getActivitiesId()
        ).addOnFailureListener(
                e -> System.out.println("Error updating group: " + e.getMessage()));
    }

    public Task<Void> deleteGroup(String groupId) {
        return db.collection("groups").document(groupId).delete().addOnFailureListener(
                e -> System.out.println("Error deleting group: " + e.getMessage()));
    }

    public Task<QuerySnapshot> getGroupsByMemberId(String memberId) {
        return db.collection("groups")
                .whereArrayContains("membersId", memberId)
                .get();
    }
}
