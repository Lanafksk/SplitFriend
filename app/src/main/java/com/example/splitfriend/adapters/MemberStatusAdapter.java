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
    private final double totalAmount; // 총 금액
    private final int participantCount; // 참가자 수
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
        String userId = statusMap.get("userId");
        String status = statusMap.get("status");

        // Firestore에서 userId를 기반으로 사용자 이름 가져오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        double amountPerPerson = totalAmount / participantCount; // 분담 금액 계산
                        holder.memberNameTextView.setText(userName != null ? userName + " - " + String.format("%.2f ₫", amountPerPerson) : "Unknown");
                    } else {
                        holder.memberNameTextView.setText("Unknown");
                    }
                });

        // PAY 버튼 동작
        if (userId.equals(currentUserId) && !"paid".equals(status)) {
            holder.payButton.setVisibility(View.VISIBLE);
            holder.statusTextView.setVisibility(View.GONE);
            holder.statusIcon.setVisibility(View.GONE);

            holder.payButton.setOnClickListener(v -> {
                // Firestore 업데이트
                db.collection("activities").document(activityId)
                        .get()
                        .addOnSuccessListener(activitySnapshot -> {
                            if (activitySnapshot.exists()) {
                                List<Map<String, String>> updatedStatuses = (List<Map<String, String>>) activitySnapshot.get("paymentStatusesId");
                                for (Map<String, String> updatedStatus : updatedStatuses) {
                                    if (updatedStatus.get("userId").equals(userId)) {
                                        updatedStatus.put("status", "paid"); // 상태를 paid로 변경
                                        break;
                                    }
                                }

                                // Firestore에 업데이트된 상태 저장
                                db.collection("activities").document(activityId)
                                        .update("paymentStatusesId", updatedStatuses)
                                        .addOnSuccessListener(aVoid -> {
                                            // 상태 업데이트 후 UI 새로고침
                                            statusMap.put("status", "paid"); // 로컬 데이터 업데이트
                                            notifyItemChanged(holder.getAdapterPosition()); // RecyclerView 새로고침
                                            Toast.makeText(holder.itemView.getContext(), "Payment status updated to Paid", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(holder.itemView.getContext(), "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
            });
        } else {
            holder.payButton.setVisibility(View.GONE);
            holder.statusTextView.setVisibility(View.VISIBLE);
            holder.statusIcon.setVisibility(View.VISIBLE);

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

