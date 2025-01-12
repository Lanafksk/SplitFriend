package com.example.splitfriend.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.GroupHelper;
import com.example.splitfriend.data.helpers.UserHelper;
import com.example.splitfriend.data.models.Group;
import com.example.splitfriend.data.models.User;
import com.example.splitfriend.user.activity.GroupDetailActivity;
import com.example.splitfriend.viewHolders.GroupViewHolder;
import com.google.android.material.chip.Chip;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupViewHolder> {
    private final List<Group> groupList;
    private final GroupHelper groupHelper;
    private final String currentUserId;
    private OnItemClickListener onItemClickListener;
    private final OnGroupActionListener onGroupActionListener;

    public GroupAdapter(List<Group> groupList, GroupHelper groupHelper, String currentUserId, OnGroupActionListener onGroupActionListener) {
        this.groupList = groupList;
        this.groupHelper = groupHelper;
        this.currentUserId = currentUserId;
        this.onGroupActionListener = onGroupActionListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);

        // Display the group name and member count
        holder.groupName.setText(group.getName());
        holder.memberCount.setText(String.valueOf(group.getMembersId().size()));

        // Set action text based on whether the user is the group leader
        if (group.getLeaderId().equals(currentUserId)) {
            holder.deleteText.setText("Delete");
        } else {
            holder.deleteText.setText("Leave");
        }

        // Handle navigation to group detail page
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(group);
            } else {
                Intent intent = new Intent(holder.itemView.getContext(), GroupDetailActivity.class);
                intent.putExtra("groupId", group.getId());
                intent.putExtra("groupName", group.getName());
                intent.putExtra("memberCount", String.valueOf(group.getMembersId().size()));
                holder.itemView.getContext().startActivity(intent);
            }
        });

        // Hide delete button by default
        holder.deleteButtonLayout.setVisibility(View.GONE); // Hide delete button initially

        // Set up delete/leave button logic
        holder.deleteButtonLayout.setOnClickListener(v -> {
            if (group.getLeaderId().equals(currentUserId)) {
                // Handle group deletion for leaders
                groupHelper.deleteGroup(group.getId())
                        .addOnSuccessListener(aVoid -> {
                            groupList.remove(position);
                            notifyItemRemoved(position);
                            onGroupActionListener.onGroupDeleted();
                        })
                        .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(), "Error deleting group: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // Handle leaving the group for members
                group.getMembersId().remove(currentUserId);
                groupHelper.updateGroup(group)
                        .addOnSuccessListener(aVoid -> {
                            groupList.remove(position);
                            notifyItemRemoved(position);
                            onGroupActionListener.onGroupLeft();
                        })
                        .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(), "Error leaving group: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        // Dynamically create chips for each group member
        holder.memberChips.removeAllViews();
        for (String memberId : group.getMembersId()) {
            UserHelper userHelper = new UserHelper();
            userHelper.getUserById(memberId).addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    Chip chip = new Chip(holder.itemView.getContext());
                    chip.setText(user.getName());
                    chip.setClickable(false);
                    chip.setFocusable(false);
                    chip.setChipBackgroundColorResource(R.color.dark_gray);
                    chip.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
                    holder.memberChips.addView(chip);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(holder.itemView.getContext(), "Error getting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Group group);
    }

    public interface OnGroupActionListener {
        void onGroupDeleted();
        void onGroupLeft();
    }
}