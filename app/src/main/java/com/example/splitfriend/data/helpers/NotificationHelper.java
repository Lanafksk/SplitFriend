package com.example.splitfriend.data.helpers;

import com.example.splitfriend.data.models.Notification;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class NotificationHelper {
    private final FirebaseFirestore db;
    private ListenerRegistration listener;

    public NotificationHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // listeners
    public void setupRealtimeUpdates(EventListener<QuerySnapshot> listener) {
        this.listener = db.collection("notifications").addSnapshotListener(listener);
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    //crud
    public Task<DocumentReference> createNotification(Notification notification) {
        return db.collection("notifications").add(notification).addOnFailureListener(
                e -> System.out.println("Error creating notification: " + e.getMessage()));
    }

    public Task<Void> updateNotification(Notification notification) {
        return db.collection("notifications").document(notification.getId()).update(
                "triggeredBy", notification.getTriggeredBy(),
                "targetUsersId", notification.getTargetUsersId(),
                "message", notification.getMessage()
        ).addOnFailureListener(
                e -> System.out.println("Error updating notification: " + e.getMessage()));
    }

    public Task<Void> deleteNotification(String notificationId) {
        return db.collection("notifications").document(notificationId).delete().addOnFailureListener(
                e -> System.out.println("Error deleting notification: " + e.getMessage()));
    }
}
