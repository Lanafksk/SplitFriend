package com.example.splitfriend.data.helpers;

import com.example.splitfriend.data.models.Bill;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class BillHelper {
    private final FirebaseFirestore db;
    private ListenerRegistration listener;

    public BillHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // listeners
    public void setupRealtimeUpdates(EventListener<QuerySnapshot> listener) {
        this.listener = db.collection("bills").addSnapshotListener(listener);
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    //crud
    public Task<DocumentReference> createBill(Bill bill) {
        return db.collection("bills").add(bill).addOnFailureListener(
                e -> System.out.println("Error creating bill: " + e.getMessage()));
    }

    public Task<Void> updateBill(Bill bill) {
        return db.collection("bills").document(bill.getId()).update(
                "billAmount", bill.getBillAmount(),
                "paidBy", bill.getPaidBy(),
                "sharedBy", bill.getSharedBy()
        ).addOnFailureListener(
                e -> System.out.println("Error updating bill: " + e.getMessage()));
    }

    public Task<Void> deleteBill(String billId) {
        return db.collection("bills").document(billId).delete().addOnFailureListener(
                e -> System.out.println("Error deleting bill: " + e.getMessage()));
    }
}
