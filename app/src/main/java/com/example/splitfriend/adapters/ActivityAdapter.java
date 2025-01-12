package com.example.splitfriend.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.ActivityHelper;
import com.example.splitfriend.data.helpers.UserHelper;
import com.example.splitfriend.data.models.Activity;
import com.example.splitfriend.data.models.User;

import com.example.splitfriend.user.ActivityDetailPayeeActivity;
import com.example.splitfriend.user.ActivityDetailSenderActivity;
import com.example.splitfriend.user.group.HomeActivity;
import com.example.splitfriend.viewHolders.ActivityViewHolder;
import com.google.android.material.chip.Chip;

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

        // Clear any existing chips and add a new Chip for each participant
        holder.participantChips.removeAllViews();
        for (String participantId : activity.getParticipantsId()) {
            UserHelper userHelper = new UserHelper();
            userHelper.getUserById(participantId).addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    // Dynamically create a Chip for each participant and set its properties
                    Chip chip = new Chip(holder.itemView.getContext());
                    chip.setText(user.getName());
                    chip.setClickable(false);
                    chip.setFocusable(false);
                    chip.setChipBackgroundColorResource(R.color.dark_gray);
                    chip.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
                    holder.participantChips.addView(chip);
                }
            }).addOnFailureListener(e -> {
                // Handle failure to load user data
                Toast.makeText(holder.itemView.getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            });
        }

        // Navigate to activity detail page
        holder.itemView.setOnClickListener(v -> {

            Intent intent;
            if (activity.getCreatorId().equals(currentUserId)) {
                // Navigate to payee's activity detail page
                intent = new Intent(holder.itemView.getContext(), ActivityDetailPayeeActivity.class);
            } else {
                // Navigate to sender's activity detail page
                intent = new Intent(holder.itemView.getContext(), ActivityDetailSenderActivity.class);
            }
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("groupId", activity.getGroupId());
            holder.itemView.getContext().startActivity(intent);
        });
        holder.deleteButtonLayout.setVisibility(View.GONE); // Hide delete button initially
    }

    @Override
    public int getItemCount() {
        // Returns the total number of activities in the list
        return activityList.size();
    }

    //  Interface for handling actions related to an activity item
    public interface OnActivityActionListener {
        void onActivityDelete(String activityId);
        void onActivityLeave(String activityId);
    }
}