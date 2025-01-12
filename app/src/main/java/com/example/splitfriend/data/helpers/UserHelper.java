package com.example.splitfriend.data.helpers;

import com.example.splitfriend.data.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class UserHelper {
    private final FirebaseFirestore db;
    private ListenerRegistration listener;

    public UserHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // listeners
    public void setupRealtimeUpdates(EventListener<QuerySnapshot> listener) {
        this.listener = db.collection("users").addSnapshotListener(listener);
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    //crud
    public Task<DocumentReference> createUser(User user) {
        return db.collection("users").add(user).addOnFailureListener(
                e -> System.out.println("Error creating user: " + e.getMessage()));
    }

    public Task<Void> updateUser(User user) {
        return db.collection("users").document(user.getId()).update(
                "email", user.getEmail(),
                "userId", user.getUserId(),
                "name", user.getName(),
                "role", user.getRole()
        ).addOnFailureListener(
                e -> System.out.println("Error updating user: " + e.getMessage()));
    }

    public Task<Void> deleteUser(String userId) {
        return db.collection("users").document(userId).delete().addOnFailureListener(
                e -> System.out.println("Error deleting user: " + e.getMessage()));
    }

    public Task<DocumentSnapshot> getUserById(String userId) {
            return db.collection("users").document(userId).get();
    }

}
