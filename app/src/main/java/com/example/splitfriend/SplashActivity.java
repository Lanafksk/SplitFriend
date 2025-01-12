package com.example.splitfriend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.user.group.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check login status after 2 seconds
        new Handler().postDelayed(this::checkLoginStatus, 2000);
    }

    private void checkLoginStatus() {
        if (mAuth.getCurrentUser() != null) {
            // If a user is logged in, check their role
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists() && document.contains("role")) {
                            String role = document.getString("role");
                            if ("admin".equals(role)) {
                                // Navigate to AdminActivity
                                startActivity(new Intent(SplashActivity.this, AdminActivity.class));
                            } else {
                                // Navigate to MainActivity
                                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                            }
                        } else {
                            // If role information is missing, navigate to the login screen
                            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // If there is a Firestore error, navigate to the login screen
                        startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                        finish();
                    });
        } else {
            // If no user is logged in, navigate to the login screen
            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            finish();
        }
    }
}
