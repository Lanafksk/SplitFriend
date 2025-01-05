package com.example.splitfriend.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.GroupHelper;
import com.example.splitfriend.data.models.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Random;

public class CreateGroupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String userId;
    private EditText groupNameInput;
    private TextView inviteCodeText;
    private Button saveButton;
    private GroupHelper groupHelper;
    private FirebaseFirestore db;
    private String inviteCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        groupNameInput = findViewById(R.id.groupNameInput);
        inviteCodeText = findViewById(R.id.inviteCodeText);
        saveButton = findViewById(R.id.saveButton);
        groupHelper = new GroupHelper();
        db = FirebaseFirestore.getInstance();
        inviteCode = generateInviteCode();
        inviteCodeText.setText(inviteCode);

        saveButton.setOnClickListener(v -> createGroup());
    }

    private void createGroup() {
        String groupName = groupNameInput.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
            return;
        }

        Group group = new Group(groupName, userId, inviteCode);
        groupHelper.createGroup(group)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String generateInviteCode() {
        String inviteCode;
        do {
            inviteCode = formatInviteCode(generateRandomNumber());
        } while (!isInviteCodeUnique(inviteCode));
        return inviteCode;
    }

    private boolean isInviteCodeUnique(String inviteCode) {
        QuerySnapshot snapshots = db.collection("groups").whereEqualTo("inviteCode", inviteCode).get().getResult();
        return snapshots == null || snapshots.isEmpty();
    }

    private String generateRandomNumber() {
        Random random = new Random();
        int number = random.nextInt(90000000) + 10000000; // Ensure 8 digits
        return String.valueOf(number);
    }

    private String formatInviteCode(String number) {
        return number.substring(0, 4) + "-" + number.substring(4);
    }
}