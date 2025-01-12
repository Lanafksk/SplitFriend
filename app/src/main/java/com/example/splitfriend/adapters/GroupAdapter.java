package com.example.splitfriend.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.ActivityHelper;
import com.example.splitfriend.data.helpers.GroupHelper;
import com.example.splitfriend.data.helpers.UserHelper;
import com.example.splitfriend.data.models.Activity;
import com.example.splitfriend.data.models.Group;
import com.example.splitfriend.data.models.User;
import com.example.splitfriend.user.activity.GroupDetailActivity;
import com.example.splitfriend.viewHolders.GroupViewHolder;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        String leaderId = group.getLeaderId();
        holder.memberChips.removeAllViews();

        List<String> addedMembers = new ArrayList<>();

        for (String memberId : group.getMembersId()) {
            if (addedMembers.contains(memberId)) {
                continue;
            }

            UserHelper userHelper = new UserHelper();
            userHelper.getUserById(memberId).addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    if (addedMembers.contains(memberId)) return;

                    Chip chip = new Chip(holder.itemView.getContext());
                    chip.setText(user.getName());
                    chip.setClickable(false);
                    chip.setFocusable(false);

                    if (Objects.equals(memberId, leaderId)) {
                        chip.setChipBackgroundColorResource(R.color.dark_blue);
                    } else {
                        chip.setChipBackgroundColorResource(R.color.dark_gray);
                    }
                    chip.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));

                    holder.memberChips.addView(chip);
                    addedMembers.add(memberId);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(holder.itemView.getContext(), "Error getting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        ImageView closeButton = holder.itemView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(holder.itemView.getContext(), "You are not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = mAuth.getCurrentUser().getUid();
            boolean isOwner = group.getLeaderId().equals(userId);

            LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
            View popupView = inflater.inflate(R.layout.popup_leave_group, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setView(popupView);
            AlertDialog dialog = builder.create();

            TextView leaveGroupPromt = popupView.findViewById(R.id.leavePromptText);
            Button leaveGroupButton = popupView.findViewById(R.id.leaveButton);

            if (isOwner){
                leaveGroupPromt.setText("Are you sure you want to delete this group?");
                leaveGroupButton.setText("Delete");
            } else {
                leaveGroupPromt.setText("Are you sure you want to leave this group?");
                leaveGroupButton.setText("Leave");
            }

            leaveGroupButton.setOnClickListener(v1 -> {
                    ActivityHelper activityHelper = new ActivityHelper();
                    List<Activity> relatedActivities = new ArrayList<>();
                    activityHelper.geActivityList(group.getId()).addOnSuccessListener(queryDocumentSnapshots -> {
                        for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                            relatedActivities.add(queryDocumentSnapshots.getDocuments().get(i).toObject(Activity.class));
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "Error getting activities: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    for (Activity activity : relatedActivities) {
                        if (activity.getPaymentStatusesId() != null) {
                            for (int i = 0; i < activity.getPaymentStatusesId().size(); i++) {
                                if (activity.getPaymentStatusesId().get(i).get("userId").equals(currentUserId) &&
                                        activity.getPaymentStatusesId().get(i).get("status").equals("unpaid")) {
                                    Toast.makeText(holder.itemView.getContext(), "You cannot leave the group with unpaid bills", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        } else if (activity.getCreatorId().equals(currentUserId)) {
                            for (int i = 0; i < activity.getParticipantsId().size(); i++) {
                                if (activity.getPaymentStatusesId().get(i).get("status").equals("unpaid")) {
                                    Toast.makeText(holder.itemView.getContext(), "You cannot leave the group with unpaid bills", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                    }

                    if (isOwner) {
                        groupHelper.deleteGroup(group.getId()).addOnSuccessListener(aVoid -> {
                            for (Activity activity : relatedActivities) {
                                activityHelper.deleteActivity(activity.getId()).addOnFailureListener(e -> {
                                    Toast.makeText(holder.itemView.getContext(), "Error deleting activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                            onGroupActionListener.onGroupDeleted();
                            dialog.dismiss();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(holder.itemView.getContext(), "Error deleting group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        for (Activity activity : relatedActivities) {
                            List<String> participantsId = activity.getParticipantsId();
                            participantsId.remove(userId);
                            activity.setTotalAmount(activity.getTotalAmount() * (activity.getParticipantsId().size() / participantsId.size()));
                            activity.setParticipantsId(participantsId);
                            List<Map<String, String>> paymentStatusesId = activity.getPaymentStatusesId();
                            paymentStatusesId.removeIf(map -> Objects.equals(map.get("userId"), userId));
                            activity.setPaymentStatusesId(paymentStatusesId);
                            activityHelper.updateActivity(activity).addOnFailureListener(e -> {
                                Toast.makeText(holder.itemView.getContext(), "Error updating activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                        List<String> newGroupList = group.getMembersId();
                        newGroupList.remove(userId);
                        group.setMembersId(newGroupList);
                        groupHelper.updateGroup(group).addOnSuccessListener(aVoid -> {
                            onGroupActionListener.onGroupLeft();
                            dialog.dismiss();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(holder.itemView.getContext(), "Error leaving group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }

            });

            ImageView closePopup = popupView.findViewById(R.id.closeButton);
            closePopup.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });

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