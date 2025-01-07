package com.example.splitfriend.viewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;

public class ActivityViewHolder extends RecyclerView.ViewHolder {
    public TextView activityName;
    public TextView participantCount;
    public TextView activityDate;
    public TextView activityAmount;
    public View deleteButtonLayout;
    public TextView deleteText;


    public ActivityViewHolder(@NonNull View itemView) {
        super(itemView);
        activityName = itemView.findViewById(R.id.activityName);
        participantCount = itemView.findViewById(R.id.participantCount);
        activityDate = itemView.findViewById(R.id.activityDate);
        activityAmount = itemView.findViewById(R.id.activityAmount);
        deleteButtonLayout = itemView.findViewById(R.id.deleteButtonLayout);
        deleteText = itemView.findViewById(R.id.deleteText);
    }

}
