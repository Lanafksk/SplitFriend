package com.example.splitfriend.user;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
    private int swipedPosition =-1;

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

        loadGroups();

        // Set up the floating button click listener
        findViewById(R.id.addGroupFab).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateGroupActivity.class);
            startActivity(intent);
        });

        setupSwipeToDelete(recyclerView);
    }

    private void setupSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final float deleteButtonWidth = 80 * Resources.getSystem().getDisplayMetrics().density;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (swipedPosition != position) {
                    swipedPosition = position;
                    groupAdapter.notifyItemChanged(position);
                } else {
                    swipedPosition = -1;
                    groupAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (position == swipedPosition) {
                    return ItemTouchHelper.RIGHT; // Only allow right swipe to close
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                CardView cardView = itemView.findViewById(R.id.groupCardView);
                View deleteButton = itemView.findViewById(R.id.deleteButtonLayout);

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    float itemWidth = itemView.getWidth();
                    float maxSwipeDistance = deleteButtonWidth;

                    if (dX < 0) { // Swiping to the left
                        float swipeDistance = Math.max(-maxSwipeDistance, dX);
                        cardView.setTranslationX(swipeDistance);
                        deleteButton.setEnabled(true);
                    } else { // Swiping to the right
                        float swipeDistance = Math.min(maxSwipeDistance, dX);
                        cardView.setTranslationX(swipeDistance - maxSwipeDistance);
                        if (swipeDistance == 0) {
                            deleteButton.setEnabled(false);
                        }
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                View itemView = viewHolder.itemView;
                CardView cardView = itemView.findViewById(R.id.groupCardView);
                View deleteButton = itemView.findViewById(R.id.deleteButtonLayout);

                int position = viewHolder.getAdapterPosition();
                float maxSwipeDistance = deleteButtonWidth;

                if (position == swipedPosition) {
                    cardView.setTranslationX(-maxSwipeDistance);
                    deleteButton.setEnabled(true);
                    setupDeleteButtonClickListener(deleteButton, position);
                } else {
                    cardView.setTranslationX(0);
                    deleteButton.setEnabled(false);
                }
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    private void setupDeleteButtonClickListener(View deleteButton, int position) {
        deleteButton.setOnClickListener(v -> {
            Group group = groupList.get(position);
            if (group.getLeaderId().equals(userId)) {
                // Delete group logic
                groupHelper.deleteGroup(group.getId())
                        .addOnSuccessListener(aVoid -> {
                            groupList.remove(position);
                            groupAdapter.notifyItemRemoved(position);
                            swipedPosition = -1;
                            onGroupDeleted();
                        })
                        .addOnFailureListener(e -> Toast.makeText(HomeActivity.this,
                                "Error deleting group: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            } else {
                // Leave group logic
                group.getMembersId().remove(userId);
                groupHelper.updateGroup(group)
                        .addOnSuccessListener(aVoid -> {
                            groupList.remove(position);
                            groupAdapter.notifyItemRemoved(position);
                            swipedPosition = -1;
                            onGroupLeft();
                        })
                        .addOnFailureListener(e -> Toast.makeText(HomeActivity.this,
                                "Error leaving group: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
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
    }

    @Override
    public void onGroupLeft() {
        Toast.makeText(this, "Left group successfully", Toast.LENGTH_SHORT).show();
    }
}