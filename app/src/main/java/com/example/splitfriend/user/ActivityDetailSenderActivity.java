package com.example.splitfriend.user;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.adapters.MemberAdapter;
import com.example.splitfriend.adapters.MemberStatusAdapter;
import com.example.splitfriend.data.helpers.ActivityHelper;
import com.example.splitfriend.data.models.Activity;
import com.example.splitfriend.network.ApiClient;
import com.example.splitfriend.network.ApiService;
import com.example.splitfriend.network.CreatePaymentIntentRequest;
import com.example.splitfriend.network.CreatePaymentIntentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityDetailSenderActivity extends AppCompatActivity {

    private static final String TAG = "ActivityDetailSender";
    private FirebaseFirestore db;
    private String activityId, groupId;

    private TextView activityNameTextView, totalAmountTextView, amountTextView;
    private RecyclerView otherMembersRecyclerView;
    private MaterialButton actionButton;

    private Activity activityDetails;
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private String pendingPayUserId;


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
//        amountTextView = findViewById(R.id.amountTextView);
        otherMembersRecyclerView = findViewById(R.id.otherMembersRecyclerView);
//        actionButton = findViewById(R.id.actionButton);

        // RecyclerView 설정
        otherMembersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // 데이터 로드
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
                        // 파이어베이스에서 데이터 가져오기
                        String activityName = documentSnapshot.getString("name");
                        Double totalAmount = documentSnapshot.getDouble("totalAmount");
                        List<Map<String, String>> paymentStatuses =
                                (List<Map<String, String>>) documentSnapshot.get("paymentStatusesId");
                        List<String> participantsId =
                                (List<String>) documentSnapshot.get("participantsId");

                        // TextView 업데이트
                        TextView activityNameTextView = findViewById(R.id.activityNameTextView);
                        TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
                        activityNameTextView.setText(activityName != null ? activityName : "Unknown Activity");
                        totalAmountTextView.setText(totalAmount != null
                                ? String.format("%.2f ₫", totalAmount) : "0 ₫");

                        // 로그인한 사용자 ID 가져오기
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // ─────────────────────────────────────────────────────────
                        // 1) participantsId에 없는 userId는 제외하기
                        // ─────────────────────────────────────────────────────────
                        List<Map<String, String>> filteredList = new ArrayList<>();
                        if (paymentStatuses != null) {
                            for (Map<String, String> status : paymentStatuses) {
                                String userId = status.get("userId");
                                if (userId != null && participantsId.contains(userId)) {
                                    filteredList.add(status);
                                }
                            }
                        }

                        // ─────────────────────────────────────────────────────────
                        // 2) 현재 로그인한 사용자가 맨 위로 오도록 정렬
                        // ─────────────────────────────────────────────────────────
                        filteredList.sort((map1, map2) -> {
                            String userId1 = map1.get("userId");
                            String userId2 = map2.get("userId");

                            // 현재 사용자가 userId1이면 우선순위 -1, 맨 위로
                            if (userId1.equals(currentUserId)) {
                                return -1;
                            }
                            // 현재 사용자가 userId2이면 우선순위 1, 아래로
                            if (userId2.equals(currentUserId)) {
                                return 1;
                            }
                            return 0; // 그 외는 기존 순서 유지
                        });

                        // RecyclerView에 Adapter 연결
                        MemberStatusAdapter adapter = new MemberStatusAdapter(
                                filteredList,
                                currentUserId,
                                activityId,
                                totalAmount != null ? totalAmount : 0,
                                participantsId.size(),
                                // 예시 수정 후 (정상 결제 창 띄우기)
                                userId -> {
                                    // 1) 결제를 누른 사용자 ID 저장
                                    pendingPayUserId = userId;

                                    // 2) 분담금(금액) 계산 예시
                                    double splittedAmount = (totalAmount != null ? totalAmount : 0) / participantsId.size();

                                    // 3) Stripe 결제 플로우 시작
                                    startStripePayment(splittedAmount);
                                }

                        );

                        RecyclerView otherMembersRecyclerView = findViewById(R.id.otherMembersRecyclerView);
                        otherMembersRecyclerView.setAdapter(adapter);
                    }
                });
    }







    private void updatePaymentStatus(String activityId, String userId) {
        db.collection("activities").document(activityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();

                        // paymentStatusesId 배열 가져오기
                        List<Map<String, String>> paymentStatuses = (List<Map<String, String>>) data.get("paymentStatusesId");

                        if (paymentStatuses != null) {
                            for (Map<String, String> status : paymentStatuses) {
                                if (status.get("userId").equals(userId)) {
                                    status.put("status", "paid"); // 상태를 paid로 변경
                                    break;
                                }
                            }

                            // 업데이트된 paymentStatusesId를 Firestore에 저장
                            db.collection("activities").document(activityId)
                                    .update("paymentStatusesId", paymentStatuses)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Payment status updated", Toast.LENGTH_SHORT).show();
                                        loadActivityDetails(); // UI 새로고침
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load activity", Toast.LENGTH_SHORT).show();
                });
    }

    private void startStripePayment(double amount) {
        // 분담금(예: 10000 VND 등)을 cents로 바꿔야 한다면, 여기서 변환 로직 수행
        // Stripe는 통화마다 최소 단위가 다릅니다 (usd=cent, vnd=동 그대로 가능 등)
        // 간단히 usd 가정: $10.00 -> 1000 cents
        int amountInCents = (int) (amount * 100); // 예: 10.00 -> 1000

        // 1) 서버에 결제 Intent 생성
        ApiService api = ApiClient.getClient().create(ApiService.class);
        CreatePaymentIntentRequest requestBody = new CreatePaymentIntentRequest(amountInCents, "usd");

        api.createPaymentIntent(requestBody).enqueue(new Callback<CreatePaymentIntentResponse>() {
            @Override
            public void onResponse(Call<CreatePaymentIntentResponse> call, Response<CreatePaymentIntentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    paymentIntentClientSecret = response.body().clientSecret;
                    presentPaymentSheet(paymentIntentClientSecret);
                } else {
                    Toast.makeText(ActivityDetailSenderActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreatePaymentIntentResponse> call, Throwable t) {
                Toast.makeText(ActivityDetailSenderActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void presentPaymentSheet(String clientSecret) {
        if (clientSecret == null || clientSecret.isEmpty()) {
            Toast.makeText(this, "clientSecret is null/empty", Toast.LENGTH_SHORT).show();
            return;
        }
        PaymentSheet.Configuration configuration =
                new PaymentSheet.Configuration("SplitFriend Payment");
        paymentSheet.presentWithPaymentIntent(
                clientSecret,
                configuration
        );
    }

    /**
     * PaymentSheet 결제 결과 콜백
     */
    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        Log.d(TAG, "onPaymentSheetResult: " + paymentSheetResult);

        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // 1) 결제 성공
            Toast.makeText(this, "결제 성공!", Toast.LENGTH_LONG).show();

            // 2) Firestore에 status="paid" 로 업데이트
            updatePaymentStatusToPaid(activityId, pendingPayUserId);

        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "결제가 취소되었습니다.", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            Toast.makeText(this, "결제 실패: " + failedResult.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Firestore에서 paymentStatusesId 배열 중 해당 userId의 status를 "paid"로 변경
     */
    private void updatePaymentStatusToPaid(String activityId, String userId) {
        db.collection("activities").document(activityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data == null) return;

                        List<Map<String, String>> paymentStatuses = (List<Map<String, String>>) data.get("paymentStatusesId");
                        if (paymentStatuses != null) {
                            for (Map<String, String> status : paymentStatuses) {
                                if (userId.equals(status.get("userId"))) {
                                    status.put("status", "paid");
                                    break;
                                }
                            }
                            // 업데이트
                            db.collection("activities").document(activityId)
                                    .update("paymentStatusesId", paymentStatuses)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Payment status updated (paid)", Toast.LENGTH_SHORT).show();
                                        // UI 새로고침
                                        loadActivityDetails();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load activity", Toast.LENGTH_SHORT).show();
                });
    }


}