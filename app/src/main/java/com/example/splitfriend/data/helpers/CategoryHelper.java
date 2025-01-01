package com.example.splitfriend.data.helpers;

import com.example.splitfriend.data.models.Category;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class CategoryHelper {
    private FirebaseFirestore db;
    private ListenerRegistration listener;

    public CategoryHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // listeners
    public void setupRealtimeUpdates(EventListener<QuerySnapshot> listener) {
        this.listener = db.collection("categories").addSnapshotListener(listener);
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    //crud
    public Task<DocumentReference> createCategory(Category category) {
        return db.collection("categories").add(category).addOnFailureListener(
                e -> System.out.println("Error creating category: " + e.getMessage()));
    }

    public Task<Void> updateCategory(Category category) {
        return db.collection("categories").document(category.getId()).update(
                "name", category.getName(),
                "totalAmount", category.getTotalAmount(),
                "billsId", category.getBillsId()
        ).addOnFailureListener(
                e -> System.out.println("Error updating category: " + e.getMessage()));
    }

    public Task<Void> deleteCategory(String categoryId) {
        return db.collection("categories").document(categoryId).delete().addOnFailureListener(
                e -> System.out.println("Error deleting category: " + e.getMessage()));
    }
}
