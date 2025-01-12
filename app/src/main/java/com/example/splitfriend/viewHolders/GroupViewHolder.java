package com.example.splitfriend.viewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.google.android.material.chip.ChipGroup;

public class GroupViewHolder extends RecyclerView.ViewHolder {
    public TextView groupName;
    public TextView memberCount;
    public ChipGroup memberChips;

    public GroupViewHolder(@NonNull View itemView) {
        super(itemView);
        groupName = itemView.findViewById(R.id.groupName);
        memberCount = itemView.findViewById(R.id.memberCount);
        memberChips = itemView.findViewById(R.id.memberChipGroup);
    }
}