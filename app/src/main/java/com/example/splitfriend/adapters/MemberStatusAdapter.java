package com.example.splitfriend.adapters;

import android.graphics.Color;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class MemberStatusAdapter extends RecyclerView.Adapter<MemberStatusAdapter.MemberStatusViewHolder> {

    private final List<Map<String, String>> paymentStatuses;
    private final String currentUserId;
    private final double totalAmount; // Total amount
    private final int participantCount; // Number of participant
    private final OnPayButtonClickListener onPayButtonClickListener;
    private final String activityId;

    public MemberStatusAdapter(List<Map<String, String>> paymentStatuses, String currentUserId, String activityId, double totalAmount, int participantCount, OnPayButtonClickListener listener) {
        this.paymentStatuses = paymentStatuses;
        this.currentUserId = currentUserId;
        this.activityId = activityId;
        this.totalAmount = totalAmount;
        this.participantCount = participantCount;
        this.onPayButtonClickListener = listener;
    }

    @NonNull
    @Override
    public MemberStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_status, parent, false);
        return new MemberStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberStatusViewHolder holder, int position) {
        Map<String, String> statusMap = paymentStatuses.get(position);
        // Get the current member's payment status data
        String userId = statusMap.get("userId");
        String status = statusMap.get("status");

        // Fetch the member's name from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Calculate the per-person amount and display it with the member's name
                        String userName = documentSnapshot.getString("name");
                        double amountPerPerson = totalAmount / participantCount;
                        holder.memberNameTextView.setText(userName != null ? userName + " - " + String.format("%.2f ₫", amountPerPerson) : "Unknown");
                    } else {
                        holder.memberNameTextView.setText("Unknown");
                    }
                });

        // Show "Pay" button if the current user is unpaid
        if (userId.equals(currentUserId) && !"paid".equals(status)) {
            holder.payButton.setVisibility(View.VISIBLE);
            holder.statusTextView.setVisibility(View.GONE);
            holder.statusIcon.setVisibility(View.GONE);

            // Handle "Pay" button click
            holder.payButton.setOnClickListener(v -> {
                onPayButtonClickListener.onPayButtonClick(userId);
            });
        } else {
            // Show payment status and icon if the user is not the current user or is already paid
            holder.payButton.setVisibility(View.GONE);
            holder.statusTextView.setVisibility(View.VISIBLE);
            holder.statusIcon.setVisibility(View.VISIBLE);

            // Update the status text and icon based on payment status
            if ("paid".equals(status)) {
                holder.statusIcon.setImageResource(R.drawable.baseline_check_24); // 초록색 체크 아이콘
                holder.statusTextView.setText("Paid");
                holder.statusTextView.setTextColor(Color.GREEN);
            } else {
                holder.statusIcon.setImageResource(R.drawable.baseline_close_red_24); // 빨간색 엑스 아이콘
                holder.statusTextView.setText("Unpaid");
                holder.statusTextView.setTextColor(Color.RED);
            }
        }
    }



    @Override
    public int getItemCount() {
        return paymentStatuses.size();
    }

    public static class MemberStatusViewHolder extends RecyclerView.ViewHolder {
        TextView memberNameTextView, statusTextView;
        ImageView statusIcon;
        Button payButton;

        public MemberStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            memberNameTextView = itemView.findViewById(R.id.memberNameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            payButton = itemView.findViewById(R.id.payButton);
        }
    }

    public interface OnPayButtonClickListener {
        void onPayButtonClick(String userId);
    }
}

