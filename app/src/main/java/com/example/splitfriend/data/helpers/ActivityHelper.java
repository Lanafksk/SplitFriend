package com.example.splitfriend.data.helpers;

import com.example.splitfriend.data.models.Activity;
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

    // listeners
    public void setupRealtimeUpdates(EventListener<QuerySnapshot> listener) {
        this.listener = db.collection("activities").addSnapshotListener(listener);
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    //crud
    public Task<DocumentReference> createActivity(Activity activity) {
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("name", activity.getName());
        activityData.put("amount", activity.getAmount());
        activityData.put("date", activity.getDate());
        activityData.put("creatorId", activity.getCreatorId());
        activityData.put("participantsId", activity.getParticipantsId());
        activityData.put("categoriesId", activity.getCategoriesId());
        activityData.put("paymentStatusesId", activity.getPaymentStatusesId());
        activityData.put("payee", activity.getPayee()); // Add payee
        activityData.put("bankName", activity.getBankName()); // Add bank name
        activityData.put("bankAccount", activity.getBankAccount()); // Add bank account

        return db.collection("activities").add(activityData).addOnFailureListener(
                e -> System.out.println("Error creating activity: " + e.getMessage()));
    }


    public Task<Void> updateActivity(Activity activity) {
        return db.collection("activities").document(activity.getId()).update(
                "name", activity.getName(),
                "amount", activity.getAmount(),
                "categoriesId", activity.getCategoriesId(),
                "paymentStatusesId", activity.getPaymentStatusesId()
        ).addOnFailureListener(
                e -> System.out.println("Error updating activity: " + e.getMessage()));
    }

    public Task<Void> deleteActivity(String activityId) {
        return db.collection("activities").document(activityId).delete().addOnFailureListener(
                e -> System.out.println("Error deleting activity: " + e.getMessage()));
    }
}
