package com.example.splitfriend.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.Locale;
import java.util.Map;

public class ActivityDetailPayeeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String activityId;
    private RecyclerView otherMembersRecyclerView;
    private TextView activityNameTextView, totalAmountTextView;
    private LinearLayout totalAmountSection;
    private ImageButton backButton;

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

        totalAmountSection = findViewById(R.id.totalAmountSection);
        totalAmountSection.setOnClickListener(v -> {
            // ActivityTotalAmountActivity로 이동, docId로 activityId를 넘김
            Intent intent = new Intent(ActivityDetailPayeeActivity.this, ActivityTotalAmountActivity.class);
            intent.putExtra("docId", activityId);
            startActivity(intent);
        });

        // 데이터 불러오기
        loadActivityDetails();

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.dark_blue)); // AppBar 배경색과 동일
        }
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
                        // Safely format totalAmount as an integer with commas
                        String formattedTotalAmount = totalAmount != null
                                ? String.format(Locale.getDefault(), "%,d ₫", Math.round(totalAmount))
                                : "0 ₫";
                        totalAmountTextView.setText(formattedTotalAmount);


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
