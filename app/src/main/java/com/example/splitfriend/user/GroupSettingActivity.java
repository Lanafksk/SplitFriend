package com.example.splitfriend.user;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.GroupHelper;
import com.example.splitfriend.data.helpers.UserHelper;
import com.example.splitfriend.data.models.Group;
import com.example.splitfriend.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collection;
import java.util.Objects;

public class GroupSettingActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String userId, groupId;
    private TextView inviteCodeTextView;
    private EditText groupNameTextView;
    private ImageButton editGroupNameButton, copyInviteCodeButton, backButton;
    private boolean isEditing = false;
    private Group currentGroup;
    private GroupHelper groupHelper;
    private LinearLayout membersLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_setting);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        Intent intent = getIntent();
        if (intent.hasExtra("groupId")) {
            groupId = intent.getStringExtra("groupId");
        }

        groupNameTextView = findViewById(R.id.groupNameTextView);
        editGroupNameButton = findViewById(R.id.editGroupNameButton);
        inviteCodeTextView = findViewById(R.id.inviteCodeTextView);
        copyInviteCodeButton = findViewById(R.id.copyInviteCodeButton);
        membersLayout = findViewById(R.id.memberListLayout);
        copyInviteCodeButton = findViewById(R.id.copyInviteCodeButton);
        copyInviteCodeButton.setOnClickListener(v -> copyInvCode());


        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        groupHelper = new GroupHelper();
        groupHelper.getGroupById(groupId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentGroup = task.getResult().toObject(Group.class);
                if (currentGroup != null) {
                    if (Objects.equals(userId, currentGroup.getLeaderId())) {
                        isEditing = true;
                    }
                    loadFields();
                }
            } else {
                Toast.makeText(this, "Error getting group: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void copyInvCode() {
        groupHelper.getGroupById(groupId).addOnSuccessListener(documentSnapshot -> {
            Group group = documentSnapshot.toObject(Group.class);
            if (group != null) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Invite Code", group.getInviteCode());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Invite code copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error getting group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentGroup != null) {
            loadFields();
        }
    }

    private void loadFields() {
        groupNameTextView.setText(currentGroup.getName());
        inviteCodeTextView.setText(currentGroup.getInviteCode());
        groupNameTextView.setEnabled(false);
        populateMembers();
        if (isEditing) {
            editGroupNameButton.setVisibility(View.VISIBLE);
            editGroupNameButton.setOnClickListener(v -> enableEdit());
        } else {
            editGroupNameButton.setVisibility(View.GONE);
        }
    }

    private void enableEdit() {
        groupNameTextView.setEnabled(true);
        Toast.makeText(this, "Click on the group name to edit!", Toast.LENGTH_SHORT).show();
        editGroupNameButton.setImageResource(R.drawable.baseline_check_24);
        editGroupNameButton.setOnClickListener(v -> {
            editGroupName();
            groupNameTextView.setEnabled(false);
            groupNameTextView.setBackgroundResource(Color.TRANSPARENT);
            editGroupNameButton.setImageResource(R.drawable.baseline_edit_24);
            editGroupNameButton.setOnClickListener(v1 -> enableEdit());
        });

    }

    private void populateMembers() {
        membersLayout.removeAllViews(); // Clear existing views

        for (String memberId : currentGroup.getMembersId()) {
            UserHelper userHelper = new UserHelper();
            userHelper.getUserById(memberId).addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    View memberItemView = getLayoutInflater().inflate(R.layout.member_item_group, membersLayout, false);

                    TextView memberNameTextView = memberItemView.findViewById(R.id.memberItemName);
                    memberNameTextView.setText(user.getName());

                    ImageButton removeMemberButton = memberItemView.findViewById(R.id.removeMemberButton);
                    if (isEditing && !memberId.equals(currentGroup.getLeaderId())) {
                        removeMemberButton.setVisibility(View.VISIBLE);
                        removeMemberButton.setOnClickListener(v -> {
                            // Handle member removal
                            currentGroup.getMembersId().remove(memberId);
                            groupHelper.updateMembersId(currentGroup.getId(), currentGroup.getMembersId())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Member removed successfully", Toast.LENGTH_SHORT).show();
                                        populateMembers(); // Refresh the member list
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error removing member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    } else {
                        removeMemberButton.setVisibility(View.GONE);
                    }

                    membersLayout.addView(memberItemView);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error getting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void editGroupName() {
        groupHelper.updateGroupName(groupId, groupNameTextView.getText().toString());
        Toast.makeText(this, "Group name updated successfully", Toast.LENGTH_SHORT).show();
    }
}