package com.example.splitfriend.data.helpers;

import com.example.splitfriend.data.models.Activity;
import com.example.splitfriend.data.models.Bill;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ActivityHelper {
    private final FirebaseFirestore db;
    private ListenerRegistration listener;

    public ActivityHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Listeners
    public void setupRealtimeUpdates(EventListener<QuerySnapshot> listener) {
        this.listener = db.collection("activities").addSnapshotListener(listener);
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    // CRUD
    public Task<DocumentReference> createActivity(Activity activity) {
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("name", activity.getName());
        activityData.put("groupId", activity.getGroupId());
        activityData.put("totalAmount", activity.getTotalAmount());
        activityData.put("date", activity.getDate());
        activityData.put("creatorId", activity.getCreatorId());
        activityData.put("participantsId", activity.getParticipantsId());
        activityData.put("paymentStatusesId", activity.getPaymentStatusesId());
        activityData.put("payee", activity.getPayee());
        activityData.put("bankName", activity.getBankName());
        activityData.put("bankAccount", activity.getBankAccount());
        activityData.put("bills", activity.getBills()); // Bills 추가

        return db.collection("activities").add(activityData).addOnFailureListener(
                e -> System.out.println("Error creating activity: " + e.getMessage()));
    }

    public Task<Void> updateActivity(Activity activity) {
        return db.collection("activities").document(activity.getId()).update(
                "name", activity.getName(),
                "totalAmount", activity.getTotalAmount(),
                "groupId", activity.getGroupId(),
                "date", activity.getDate(),
                "creatorId", activity.getCreatorId(),
                "participantsId", activity.getParticipantsId(),
                "paymentStatusesId", activity.getPaymentStatusesId(),
                "payee", activity.getPayee(),
                "bankName", activity.getBankName(),
                "bankAccount", activity.getBankAccount(),
                "bills", activity.getBills()
        ).addOnFailureListener(
                e -> System.out.println("Error updating activity: " + e.getMessage()));
    }

    public Task<Void> deleteActivity(String activityId) {
        return db.collection("activities").document(activityId).delete().addOnFailureListener(
                e -> System.out.println("Error deleting activity: " + e.getMessage()));
    }

    public Task<QuerySnapshot> geActivityList(String groupId) {
        return db.collection("activities")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        System.out.println("Activities fetched: " + task.getResult().size());
                    } else {
                        System.out.println("Error fetching activities: " + task.getException());
                    }
                });
    }
}
