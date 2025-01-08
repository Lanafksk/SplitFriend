package com.example.splitfriend.user.activity;

import android.content.Intent;
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
import com.example.splitfriend.data.models.Activity;
import com.example.splitfriend.data.models.Group;
import com.example.splitfriend.user.group.CreateGroupActivity;
import com.example.splitfriend.user.group.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class GroupDetailActivity extends AppCompatActivity implements ActivityAdapter.OnActivityActionListener {

    private FirebaseAuth mAuth;
    private String userId;
    private String groupId;
    private TextView groupNameTextView, memberCountTextView;
    private ImageButton backButton;
    private ActivityAdapter activityAdapter;
    private ActivityHelper activityHelper;
    private List<Activity> activityList;


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

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");

        groupNameTextView = findViewById(R.id.groupNameTextView);
        groupNameTextView.setText(i.getStringExtra("groupName"));

        memberCountTextView = findViewById(R.id.memberCountTextView);
        memberCountTextView.setText(" " + i.getStringExtra("memberCount"));

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