package com.example.splitfriend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final List<String> participantIds;
    private final FirebaseFirestore db;
    private final String creatorId;    // constructor's userId

    public MemberAdapter(List<String> participantIds, String creatorId, FirebaseFirestore db) {
        this.participantIds = participantIds;
        this.creatorId = creatorId;
        this.db = db;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String userId = participantIds.get(position);

        // Skip the creator's userId from being displayed
        if (userId.equals(creatorId)) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0)); // 공간도 제거
            return;
        }

        // Fetch the user's name from Firestore and display it
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = documentSnapshot.getString("name");
                    holder.memberNameTextView.setText(userName != null ? userName : "Unknown");
                })
                .addOnFailureListener(e -> {
                    holder.memberNameTextView.setText("Error loading");
                });
    }

    @Override
    public int getItemCount() {
        return participantIds.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView memberNameTextView; // TextView to display the participant's name

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberNameTextView = itemView.findViewById(R.id.memberNameTextView);
        }
    }
}
