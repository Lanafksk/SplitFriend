package com.example.splitfriend.user.group;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.adapters.GroupAdapter;
import com.example.splitfriend.data.helpers.GroupHelper;
import com.example.splitfriend.data.models.Group;
import com.example.splitfriend.user.SettingsMenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.splitfriend.viewHolders.GroupViewHolder;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements GroupAdapter.OnGroupActionListener {
    private String userId;
    private GroupAdapter groupAdapter;
    private List<Group> groupList;
    private GroupHelper groupHelper;
    private ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }
        userId = mAuth.getCurrentUser().getUid();

        RecyclerView recyclerView = findViewById(R.id.itemGroupRecyclerView);
        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsMenuActivity.class);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupList = new ArrayList<>();
        groupHelper = new GroupHelper();
        groupAdapter = new GroupAdapter(groupList, groupHelper, userId, this);
        recyclerView.setAdapter(groupAdapter);


        loadGroups();

        // Set up the floating button click listener
        findViewById(R.id.floatingButton).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateGroupActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroups();
    }


    private void loadGroups() {
        groupHelper.getGroupsByMemberId(userId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots != null) {
                            groupList.clear();
                            groupList.addAll(snapshots.toObjects(Group.class));
                            groupAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "Error loading groups: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onGroupDeleted() {
        Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show();
        loadGroups();
    }

    @Override
    public void onGroupLeft() {
        Toast.makeText(this, "Left group successfully", Toast.LENGTH_SHORT).show();
        loadGroups();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        System.exit(0);
    }

}