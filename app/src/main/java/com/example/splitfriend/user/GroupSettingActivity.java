package com.example.splitfriend.user;

import android.content.Intent;
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
        populateMembers();
        if (isEditing) {
            if (groupNameTextView.getText().toString().equals(currentGroup.getName())) {
                editGroupNameButton.setVisibility(View.VISIBLE);
            } else {
                editGroupNameButton.setVisibility(View.GONE);
            }
            editGroupNameButton.setOnClickListener(v -> editGroupName());
            groupNameTextView.setEnabled(true);
        } else {
            editGroupNameButton.setVisibility(View.GONE);
            groupNameTextView.setEnabled(false);
        }
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
    }
}