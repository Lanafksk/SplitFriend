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

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2초 후 로그인 상태 확인
        new Handler().postDelayed(this::checkLoginStatus, 2000);
    }

    private void checkLoginStatus() {
        if (mAuth.getCurrentUser() != null) {
            // 로그인된 사용자가 있으면 사용자 역할 확인
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists() && document.contains("role")) {
                            String role = document.getString("role");
                            if ("admin".equals(role)) {
                                // AdminActivity로 이동
                                startActivity(new Intent(SplashActivity.this, AdminActivity.class));
                            } else {
                                // MainActivity로 이동
                                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                            }
                        } else {
                            // 역할 정보가 없을 경우 로그인 화면으로 이동
                            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Firestore 오류 시 로그인 화면으로 이동
                        startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                        finish();
                    });
        } else {
            // 로그인된 사용자가 없으면 로그인 화면으로 이동
            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            finish();
        }
    }
}
