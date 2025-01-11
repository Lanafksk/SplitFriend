package com.example.splitfriend.user;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.adapters.MemberStatusAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivityDetailPayeeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String activityId;
    private RecyclerView otherMembersRecyclerView;
    private TextView activityNameTextView, totalAmountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_payee);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // Intent로 activityId 받기
        activityId = getIntent().getStringExtra("activityId");

        // View 초기화
        activityNameTextView = findViewById(R.id.activityNameTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        otherMembersRecyclerView = findViewById(R.id.otherMembersRecyclerView);
        otherMembersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 데이터 불러오기
        loadActivityDetails();
    }

    private void loadActivityDetails() {
        db.collection("activities").document(activityId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to listen for updates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Firebase에서 데이터 가져오기
                        String activityName = documentSnapshot.getString("name");
                        Double totalAmount = documentSnapshot.getDouble("totalAmount");
                        List<Map<String, String>> paymentStatuses =
                                (List<Map<String, String>>) documentSnapshot.get("paymentStatusesId");
                        List<String> participantsId =
                                (List<String>) documentSnapshot.get("participantsId");

                        // TextView 업데이트
                        activityNameTextView.setText(activityName != null ? activityName : "Unknown Activity");
                        totalAmountTextView.setText(totalAmount != null
                                ? String.format("%.2f ₫", totalAmount)
                                : "0 ₫");

                        // RecyclerView에 Adapter 연결
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        List<Map<String, String>> filteredStatuses = new ArrayList<>();
                        if (paymentStatuses != null) {
                            for (Map<String, String> status : paymentStatuses) {
                                if (participantsId.contains(status.get("userId"))) {
                                    filteredStatuses.add(status);
                                }
                            }
                        }

                        MemberStatusAdapter adapter = new MemberStatusAdapter(
                                filteredStatuses,
                                currentUserId,
                                activityId,
                                totalAmount != null ? totalAmount : 0,
                                participantsId.size(),
                                userId -> {} // Payee 페이지에서는 클릭 이벤트 없음
                        );
                        otherMembersRecyclerView.setAdapter(adapter);
                    }
                });
    }
}
