package com.example.splitfriend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.user.group.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUserId, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        ImageView btnBack = findViewById(R.id.logoImageView);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // 뷰 연결
        etUserId = findViewById(R.id.etUserId); // userId 입력 필드
        etPassword = findViewById(R.id.etPassword); // 비밀번호 입력 필드
        btnLogin = findViewById(R.id.btnLogin);

        // ProgressDialog 초기화
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // 로그인 버튼 클릭 이벤트
        btnLogin.setOnClickListener(v -> {
            String userId = etUserId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // userId로 이메일을 찾고 로그인 시도
            loginWithUserId(userId, password);
        });
    }

    private void loginWithUserId(String userId, String password) {
        // "Logging in..." ProgressDialog 표시
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        // Firestore에서 userId로 이메일 조회
        db.collection("users")
                .whereEqualTo("userId", userId) // userId로 조회
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // 이메일 가져오기
                        String email = task.getResult().getDocuments().get(0).getString("email");
                        if (email != null) {
                            // 이메일과 비밀번호로 로그인
                            loginUser(email, password);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(this, "No user found with this User ID.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to find user with User ID: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // "Checking user role..." ProgressDialog 메시지 변경
                        progressDialog.setMessage("Checking user role...");

                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(document -> {
                                    progressDialog.dismiss(); // ProgressDialog 닫기

                                    if (document.exists() && document.contains("role")) {
                                        String role = document.getString("role");
                                        if ("admin".equals(role)) {
                                            // AdminActivity로 이동
                                            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                        } else {
                                            // MainActivity로 이동
                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Failed to fetch user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
