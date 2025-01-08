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
    public View deleteButtonLayout;
    public TextView deleteText;
    public ChipGroup memberChipGroup;

    public GroupViewHolder(@NonNull View itemView) {
        super(itemView);
        groupName = itemView.findViewById(R.id.groupName);
        memberCount = itemView.findViewById(R.id.memberCount);
        deleteButtonLayout = itemView.findViewById(R.id.deleteButtonLayout);
        deleteText = itemView.findViewById(R.id.deleteText);
        memberChipGroup = itemView.findViewById(R.id.memberChipGroup);
    }
}