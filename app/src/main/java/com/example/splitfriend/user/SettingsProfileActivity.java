package com.example.splitfriend.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingsProfileActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, userIdEditText;
    private TextInputEditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private MaterialButton saveButton, savePasswordButton;
    private ImageView editButton, backButton, editPasswordButton;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(currentUser.getUid());

        // View 초기화
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        userIdEditText = findViewById(R.id.userIdEditText);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        saveButton = findViewById(R.id.saveButton);
        savePasswordButton = findViewById(R.id.savePasswordButton);

        editButton = findViewById(R.id.editButton);
        backButton = findViewById(R.id.backIcon);
        editPasswordButton = findViewById(R.id.editPasswordButton);

        // 초기 상태 설정
        setFieldsEditable(false);
        saveButton.setVisibility(View.GONE);
        setPasswordFieldsVisibility(false);

        loadUserData();

        // Edit 버튼 클릭 이벤트
        editButton.setOnClickListener(v -> {
            setFieldsEditable(true);
            saveButton.setVisibility(View.VISIBLE);
        });

        // Save 버튼 클릭 이벤트
        saveButton.setOnClickListener(v -> saveUserInfo());

        // Edit Password 버튼 클릭 이벤트
        editPasswordButton.setOnClickListener(v -> setPasswordFieldsVisibility(true));

        // Save Password 버튼 클릭 이벤트
        savePasswordButton.setOnClickListener(v -> {
            String currentPassword = currentPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (validatePassword(currentPassword, newPassword, confirmPassword)) {
                reauthenticateAndUpdatePassword(currentPassword, newPassword);
            }
        });

        // Back 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Map<String, Object> data = task.getResult().getData();
                nameEditText.setText((String) data.get("name"));
                emailEditText.setText((String) data.get("email"));
                userIdEditText.setText((String) data.get("userId"));
            } else {
                Toast.makeText(this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String userId = userIdEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required!");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required!");
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            userIdEditText.setError("User ID is required!");
            return;
        }

        // Firestore에 업데이트
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("userId", userId);

        userRef.update(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                setFieldsEditable(false);
                saveButton.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFieldsEditable(boolean isEditable) {
        nameEditText.setEnabled(isEditable);
        emailEditText.setEnabled(isEditable);
        userIdEditText.setEnabled(isEditable);
    }

    private void setPasswordFieldsVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        currentPasswordEditText.setEnabled(isVisible);
        newPasswordEditText.setEnabled(isVisible);
        confirmPasswordEditText.setEnabled(isVisible);
        savePasswordButton.setVisibility(visibility);
    }

    private boolean validatePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordEditText.setError("Current password cannot be empty.");
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("New password cannot be empty.");
            return false;
        } else if (newPassword.length() < 6) {
            newPasswordEditText.setError("New password must be at least 6 characters.");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match.");
            return false;
        }

        return true;
    }

    private void reauthenticateAndUpdatePassword(String currentPassword, String newPassword) {
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            // 사용자 인증용 Credential 생성
            AuthCredential credential = EmailAuthProvider.getCredential(userEmail, currentPassword);

            // 최근 인증(Re-authenticate)
            currentUser.reauthenticate(credential).addOnCompleteListener(authTask -> {
                if (authTask.isSuccessful()) {
                    // 인증 성공 시 비밀번호 업데이트
                    currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                            setPasswordFieldsVisibility(false);
                        } else {
                            Toast.makeText(this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Re-authentication failed: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
