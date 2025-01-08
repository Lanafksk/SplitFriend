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
import com.example.splitfriend.data.models.Group;
import com.example.splitfriend.user.activity.GroupDetailActivity;
import com.example.splitfriend.viewHolders.GroupViewHolder;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupViewHolder> {
    private final List<Group> groupList;
    private final GroupHelper groupHelper;
    private final String currentUserId;
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
        holder.groupName.setText(group.getName());
        holder.memberCount.setText(String.valueOf(group.getMembersId().size()));

        if (group.getLeaderId().equals(currentUserId)) {
            holder.deleteText.setText("Delete");
        } else {
            holder.deleteText.setText("Leave");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), GroupDetailActivity.class);
            intent.putExtra("groupId", group.getId());
            intent.putExtra("groupName", group.getName());
            intent.putExtra("memberCount", String.valueOf(group.getMembersId().size()));
            holder.itemView.getContext().startActivity(intent);
        });

        holder.deleteButtonLayout.setVisibility(View.GONE); // Hide delete button initially

        holder.deleteButtonLayout.setOnClickListener(v -> {
            if (group.getLeaderId().equals(currentUserId)) {
                groupHelper.deleteGroup(group.getId())
                        .addOnSuccessListener(aVoid -> {
                            groupList.remove(position);
                            notifyItemRemoved(position);
                            onGroupActionListener.onGroupDeleted();
                        })
                        .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(), "Error deleting group: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
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
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public interface OnGroupActionListener {
        void onGroupDeleted();
        void onGroupLeft();
    }
}