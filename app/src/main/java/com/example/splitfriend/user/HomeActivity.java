package com.example.splitfriend.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupList = new ArrayList<>();
        groupHelper = new GroupHelper();
        groupAdapter = new GroupAdapter(groupList, groupHelper, userId, this);
        recyclerView.setAdapter(groupAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                groupAdapter.notifyItemChanged(position);
                groupAdapter.onBindViewHolder((GroupViewHolder) viewHolder, position);
                viewHolder.itemView.findViewById(R.id.deleteButtonLayout).setVisibility(View.VISIBLE);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        loadGroups();

        // Set up the floating button click listener
        findViewById(R.id.fabInclude).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateGroupActivity.class);
            startActivity(intent);
        });
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
    }

    @Override
    public void onGroupLeft() {
        Toast.makeText(this, "Left group successfully", Toast.LENGTH_SHORT).show();
    }
}