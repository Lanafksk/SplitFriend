package com.example.splitfriend.user.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.R;
import com.example.splitfriend.adapters.ActivityAdapter;
import com.example.splitfriend.data.helpers.ActivityHelper;
import com.example.splitfriend.data.helpers.GroupHelper;
import com.example.splitfriend.data.helpers.UserHelper;
import com.example.splitfriend.data.models.Activity;
import com.example.splitfriend.data.models.Group;
import com.example.splitfriend.data.models.User;
import com.example.splitfriend.user.GroupSettingActivity;
import com.example.splitfriend.user.group.CreateGroupActivity;
import com.example.splitfriend.user.group.HomeActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.shape.CornerTreatment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class GroupDetailActivity extends AppCompatActivity implements ActivityAdapter.OnActivityActionListener {

    private FirebaseAuth mAuth;
    private String userId;
    private String groupId;
    private TextView groupNameTextView, memberCountTextView;
    private ImageButton backButton;
    private ImageButton menuButton;
    private ActivityAdapter activityAdapter;
    private ActivityHelper activityHelper;
    private List<Activity> activityList;
    private ChipGroup memberChipGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_detail);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        backButton = findViewById(R.id.backButton);
//        backButton.setOnClickListener(v -> finish());

        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupDetailActivity.this, GroupSettingActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });

        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");

        updateGroupInfo();

        // RecyclerView 초기화
        RecyclerView recyclerView = findViewById(R.id.activityRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityList = new ArrayList<>();
        activityHelper = new ActivityHelper();
        activityAdapter = new ActivityAdapter(activityList, activityHelper, userId, this);
        recyclerView.setAdapter(activityAdapter);

        loadActivities();

        // Set up the floating button click listener
        findViewById(R.id.floatingButton).setOnClickListener(v -> {
            Intent intent = new Intent(GroupDetailActivity.this, CreateActivityActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActivities();
        loadMemberChip();
        updateGroupInfo();
    }

    private void updateGroupInfo() {
        GroupHelper groupHelper = new GroupHelper();
        groupHelper.getGroupById(groupId).addOnSuccessListener(documentSnapshot -> {
            Group group = documentSnapshot.toObject(Group.class);
            if (group != null) {
                groupNameTextView = findViewById(R.id.groupNameTextView);
                groupNameTextView.setText(group.getName());
                memberCountTextView = findViewById(R.id.memberCountTextView);
                memberCountTextView.setText(String.valueOf(group.getMembersId().size()));
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error getting group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMemberChip() {
        Intent i = getIntent();
        String groupId = i.getStringExtra("groupId");
        GroupHelper groupHelper = new GroupHelper();
        groupHelper.getGroupById(groupId).addOnSuccessListener(documentSnapshot -> {
            Group group = documentSnapshot.toObject(Group.class);
            if (group != null) {
                populateMemberChips(group);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error getting group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void populateMemberChips(Group group) {
        ChipGroup memberChipGroup = findViewById(R.id.memberChips);
        memberChipGroup.removeAllViews();
        Map<String, Boolean> processedUserIds = new HashMap<>();
        for (String memberId : group.getMembersId()) {
            if (!processedUserIds.containsKey(memberId)) {
                processedUserIds.put(memberId, true);
                UserHelper userHelper = new UserHelper();
                userHelper.getUserById(memberId).addOnSuccessListener(documentSnapshot1 -> {
                    User user = documentSnapshot1.toObject(User.class);
                    if (user != null) {
                        Chip chip = new Chip(memberChipGroup.getContext());
                        chip.setText(user.getName());
                        chip.setTextSize(12);
                        chip.setClickable(false);
                        chip.setFocusable(false);
                        chip.setTextColor(Color.DKGRAY);
                        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#33FFFFFF")));
                        chip.setChipStrokeWidth(1);
                        chip.setChipStrokeColor(ColorStateList.valueOf(Color.DKGRAY));

                        memberChipGroup.addView(chip);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
    private void loadActivities() {
        activityHelper.geActivityList(groupId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots != null) {
                            activityList.clear();
                            activityList.addAll(snapshots.toObjects(Activity.class));
                            System.out.println("Activity list size before notifyDataSetChanged: " + activityList.size());

                            activityAdapter.notifyDataSetChanged();
                            System.out.println("Loaded activities: " + snapshots.getDocuments());
                        }
                    } else {
                        System.out.println("Error loading groups: " + task.getException().getMessage());
                    }
                });
    }

    @Override
    public void onActivityDelete(String activityId) {
        Toast.makeText(this, "Activity deleted successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityLeave(String activityId) {
        Toast.makeText(this, "Left Activity successfully", Toast.LENGTH_SHORT).show();
    }
}