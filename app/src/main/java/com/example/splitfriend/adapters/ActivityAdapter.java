package com.example.splitfriend.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.ActivityHelper;
import com.example.splitfriend.data.models.Activity;
import com.example.splitfriend.user.group.HomeActivity;
import com.example.splitfriend.viewHolders.ActivityViewHolder;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityViewHolder> {
    private final List<Activity> activityList;
    private final ActivityHelper activityHelper;
    private final String currentUserId;
    private final OnActivityActionListener onActivityActionListener;

    public ActivityAdapter(List<Activity> activityList, ActivityHelper activityHelper, String currentUserId, OnActivityActionListener onActivityActionListener) {
        this.activityList = activityList;
        this.activityHelper = activityHelper;
        this.currentUserId = currentUserId;
        this.onActivityActionListener = onActivityActionListener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        holder.activityName.setText(activity.getName());
        holder.participantCount.setText(String.valueOf(activity.getParticipantsId().size()));

        // Format the activity date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String formattedDate = dateFormat.format(activity.getDate());
        holder.activityDate.setText(formattedDate);

        holder.activityAmount.setText(String.valueOf(activity.getTotalAmount()));

        if (activity.getCreatorId().equals(currentUserId)) {
            holder.deleteText.setText("Delete");
        } else {
            holder.deleteText.setText("Leave");
        }

        // move to activity detail page
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
            intent.putExtra("activityId", activity.getId());
            holder.itemView.getContext().startActivity(intent);
        });
        holder.deleteButtonLayout.setVisibility(View.GONE); // Hide delete button initially
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public interface OnActivityActionListener {
        void onActivityDelete(String activityId);
        void onActivityLeave(String activityId);
    }
}
