package com.example.splitfriend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmailVerificationActivity extends AppCompatActivity {

    private Button btnCheckVerification, btnResendEmail;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String userId, name, userIdCustom, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Intent로 전달받은 데이터
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        name = intent.getStringExtra("name");
        userIdCustom = intent.getStringExtra("userIdCustom");
        email = intent.getStringExtra("email");

        // 뷰 연결
        btnCheckVerification = findViewById(R.id.btnCheckVerification);
        btnResendEmail = findViewById(R.id.btnResendEmail);

        // "이메일을 확인하세요" 메시지 표시
        Toast.makeText(this, "Please verify your email. A verification email has been sent to " + email, Toast.LENGTH_LONG).show();

        // 이메일 인증 확인 버튼 클릭 이벤트
        btnCheckVerification.setOnClickListener(v -> checkEmailVerification());

        // 이메일 재전송 버튼 클릭 이벤트
        btnResendEmail.setOnClickListener(v -> resendVerificationEmail());
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            btnCheckVerification.setEnabled(false); // 버튼 비활성화
            Toast.makeText(this, "Checking email verification status...", Toast.LENGTH_SHORT).show();

            user.reload()
                    .addOnCompleteListener(task -> {
                        btnCheckVerification.setEnabled(true); // 버튼 다시 활성화
                        if (task.isSuccessful()) {
                            if (user.isEmailVerified()) {
                                saveUserDataToFirestore();
                            } else {
                                Toast.makeText(this, "Email not verified yet. Please verify your email.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Failed to reload user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserDataToFirestore() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("userId", userIdCustom);
        userData.put("email", email);
        userData.put("role", "user");
        userData.put("isVerified", true);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User data saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Verification email resent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to resend verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}
