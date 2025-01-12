package com.example.splitfriend.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.example.splitfriend.viewHolders.ActivityViewHolder;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

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

        holder.participantChips.removeAllViews(); // Clear all chips
        for (String participantId : activity.getParticipantsId()) {
            UserHelper userHelper = new UserHelper();
            userHelper.getUserById(participantId).addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    Chip chip = new Chip(holder.itemView.getContext());
                    chip.setText(user.getName());
                    chip.setClickable(false);
                    chip.setFocusable(false);
                    chip.setChipBackgroundColorResource(R.color.dark_gray);
                    chip.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
                    holder.participantChips.addView(chip);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(holder.itemView.getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            });
        }

        // Set up the onClickListener for the closeButton
        holder.itemView.findViewById(R.id.closeButton).setOnClickListener(v -> {
            boolean isCreator, unfinishedPayment;
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() == null) {
                return;
            }
            isCreator = activity.getCreatorId().equals(mAuth.getCurrentUser().getUid());
            unfinishedPayment = false;
            if (activity.getPaymentStatusesId() != null) {
                for (int i = 0; i < activity.getPaymentStatusesId().size(); i++) {
                    if (activity.getPaymentStatusesId().get(i).get("userId").equals(currentUserId) &&
                            activity.getPaymentStatusesId().get(i).get("status").equals("unpaid")) {
                        unfinishedPayment = true;
                        break;
                    }
                }
            } else if (activity.getCreatorId().equals(currentUserId)) {
                for (int i = 0; i < activity.getParticipantsId().size(); i++) {
                    if (activity.getPaymentStatusesId().get(i).get("status").equals("unpaid")) {
                        unfinishedPayment = true;
                        break;
                    }
                }
            }


            // Inflate the popup_leave_activity layout
            LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
            View popupView = inflater.inflate(R.layout.popup_leave_activity, null);

            // Create the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setView(popupView);
            AlertDialog alertDialog = builder.create();

            // Set the leavePromptText
            TextView leavePromptText = popupView.findViewById(R.id.leavePromptText);
            if (isCreator) {
                leavePromptText.setText("Are you sure you want to delete this activity?");
            } else {
                leavePromptText.setText("Are you sure you want to leave this activity?");
            }

            // Set up the leaveButton
            Button leaveButton = popupView.findViewById(R.id.leaveButton);
            if (isCreator) {
                leaveButton.setText("Delete");
            }
            boolean finalUnfinishedPayment = unfinishedPayment;
            leaveButton.setOnClickListener(view -> {
                ActivityHelper activityHelper = new ActivityHelper();
                if (isCreator) {
                    if (finalUnfinishedPayment) {
                        Toast.makeText(holder.itemView.getContext(), "You cannot delete the activity with unpaid bills", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    activityHelper.deleteActivity(activity.getId()).addOnSuccessListener(aVoid -> {
                        onActivityActionListener.onActivityDelete(activity.getId());
                        onActivityActionListener.reloadActivities();
                        alertDialog.dismiss();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "Failed to delete activity", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    if (finalUnfinishedPayment) {
                        Toast.makeText(holder.itemView.getContext(), "You cannot leave the activity with unpaid bills", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<String> participantsId = activity.getParticipantsId();
                    participantsId.remove(currentUserId);
                    activity.setTotalAmount(activity.getTotalAmount() * (activity.getParticipantsId().size() / participantsId.size()));
                    activity.setParticipantsId(participantsId);
                    activityHelper.updateActivity(activity).addOnSuccessListener(aVoid -> {
                        onActivityActionListener.onActivityLeave(activity.getId());
                        onActivityActionListener.reloadActivities();
                        alertDialog.dismiss();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "Failed to leave activity", Toast.LENGTH_SHORT).show();
                    });
                }
            });

            // Set up the closeButton in the popup
            ImageView closeButton = popupView.findViewById(R.id.closeButton);
            closeButton.setOnClickListener(view -> alertDialog.dismiss());

            // Show the AlertDialog
            alertDialog.show();
        });

        // move to activity detail page
        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (activity.getCreatorId().equals(currentUserId)) {
                // If current user is the creator
                intent = new Intent(holder.itemView.getContext(), ActivityDetailPayeeActivity.class);
            } else {
                // If current user is a member
                intent = new Intent(holder.itemView.getContext(), ActivityDetailSenderActivity.class);
            }
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("groupId", activity.getGroupId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public interface OnActivityActionListener {
        void reloadActivities();

        void onActivityDelete(String activityId);
        void onActivityLeave(String activityId);
    }
}