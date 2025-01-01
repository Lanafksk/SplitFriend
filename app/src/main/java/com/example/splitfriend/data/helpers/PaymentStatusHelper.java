package com.example.splitfriend.data.helpers;

import com.example.splitfriend.data.models.PaymentStatus;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class PaymentStatusHelper {
    private final FirebaseFirestore db;
    private ListenerRegistration listener;

    public PaymentStatusHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // listeners
    public void setupRealtimeUpdates(EventListener<QuerySnapshot> listener) {
        this.listener = db.collection("paymentStatus").addSnapshotListener(listener);
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    //crud
    public Task<DocumentReference> createPaymentStatus(PaymentStatus paymentStatus) {
        return db.collection("paymentStatus").add(paymentStatus).addOnFailureListener(
                e -> System.out.println("Error creating paymentStatus: " + e.getMessage()));
    }

    public Task<Void> updatePaymentStatus(PaymentStatus paymentStatus) {
        return db.collection("paymentStatus").document(paymentStatus.getId()).update(
                "userId", paymentStatus.getUserId(),
                "status", paymentStatus.getStatus()
        ).addOnFailureListener(
                e -> System.out.println("Error updating paymentStatus: " + e.getMessage()));
    }

    public Task<Void> deletePaymentStatus(String paymentStatusId) {
        return db.collection("paymentStatus").document(paymentStatusId).delete().addOnFailureListener(
                e -> System.out.println("Error deleting paymentStatus: " + e.getMessage()));
    }
}
