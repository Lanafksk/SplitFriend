package com.example.splitfriend.user;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.adapters.MemberAdapter;
import com.example.splitfriend.data.helpers.ActivityHelper;
import com.example.splitfriend.data.models.Activity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ActivityDetailSenderActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String activityId, groupId;

    private TextView activityNameTextView, totalAmountTextView, amountTextView;
    private RecyclerView otherMembersRecyclerView;
    private MaterialButton actionButton;

    private Activity activityDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_sender);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();

        // Intent에서 데이터 가져오기
        activityId = getIntent().getStringExtra("activityId");
        groupId = getIntent().getStringExtra("groupId");

        // View 초기화
        activityNameTextView = findViewById(R.id.activityNameTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        amountTextView = findViewById(R.id.amountTextView);
        otherMembersRecyclerView = findViewById(R.id.otherMembersRecyclerView);
        actionButton = findViewById(R.id.actionButton);

        // RecyclerView 설정
        otherMembersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 데이터 로드
        loadActivityDetails();
    }

    private void loadActivityDetails() {
        db.collection("activities").document(activityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    activityDetails = documentSnapshot.toObject(Activity.class);
                    if (activityDetails != null) {
                        // 액티비티 이름 설정
                        activityNameTextView.setText(activityDetails.getName());

                        // 총 금액 설정
                        double totalAmount = activityDetails.getTotalAmount();
                        totalAmountTextView.setText(String.format("%.2f d", totalAmount));

                        // 개인 금액 설정
                        List<String> participants = activityDetails.getParticipantsId();
                        if (participants != null && !participants.isEmpty()) {
                            double individualAmount = totalAmount / participants.size();
                            amountTextView.setText(String.format("%.2f d", individualAmount));
                        }

                        // 멤버 리스트 어댑터 설정 (creatorId 전달)
                        MemberAdapter memberAdapter = new MemberAdapter(
                                activityDetails.getParticipantsId(),
                                activityDetails.getCreatorId(),
                                db
                        );
                        otherMembersRecyclerView.setAdapter(memberAdapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load activity details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}