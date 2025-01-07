package com.example.splitfriend.user.group;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.GroupHelper;
import com.example.splitfriend.data.models.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        groupNameInput = findViewById(R.id.groupNameInput);
        inviteCodeText = findViewById(R.id.inviteCodeText);
        saveButton = findViewById(R.id.saveButton);
        groupHelper = new GroupHelper();
        db = FirebaseFirestore.getInstance();

        generateInviteCode(new InviteCodeCallback() {
            @Override
            public void onInviteCodeGenerated(String code) {
                inviteCode = code;
                inviteCodeText.setText(inviteCode);
            }
        });

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

    private void generateInviteCode(InviteCodeCallback callback) {
        String inviteCode = formatInviteCode(generateRandomNumber());
        isInviteCodeUnique(inviteCode, new InviteCodeUniqueCallback() {
            @Override
            public void onResult(boolean isUnique) {
                if (isUnique) {
                    callback.onInviteCodeGenerated(inviteCode);
                } else {
                    generateInviteCode(callback);
                }
            }
        });
    }

    private void isInviteCodeUnique(String inviteCode, InviteCodeUniqueCallback callback) {
        db.collection("groups")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            callback.onResult(task.getResult().isEmpty());
                        } else {
                            callback.onResult(false);
                        }
                    }
                });
    }

    private String generateRandomNumber() {
        Random random = new Random();
        int number = random.nextInt(90000000) + 10000000; // Ensure 8 digits
        return String.valueOf(number);
    }

    private String formatInviteCode(String number) {
        return number.substring(0, 4) + "-" + number.substring(4);
    }

    interface InviteCodeCallback {
        void onInviteCodeGenerated(String code);
    }

    interface InviteCodeUniqueCallback {
        void onResult(boolean isUnique);
    }
}