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

        db = FirebaseFirestore.getInstance();

        activityId = getIntent().getStringExtra("activityId");
        groupId = getIntent().getStringExtra("groupId");

        activityNameTextView = findViewById(R.id.activityNameTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
//        amountTextView = findViewById(R.id.amountTextView);
        otherMembersRecyclerView = findViewById(R.id.otherMembersRecyclerView);
//        actionButton = findViewById(R.id.actionButton);

        otherMembersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

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
                        String activityName = documentSnapshot.getString("name");
                        Double totalAmount = documentSnapshot.getDouble("totalAmount");
                        List<Map<String, String>> paymentStatuses =
                                (List<Map<String, String>>) documentSnapshot.get("paymentStatusesId");
                        List<String> participantsId =
                                (List<String>) documentSnapshot.get("participantsId");

                        TextView activityNameTextView = findViewById(R.id.activityNameTextView);
                        TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
                        activityNameTextView.setText(activityName != null ? activityName : "Unknown Activity");
                        totalAmountTextView.setText(totalAmount != null
                                ? String.format("%.2f ₫", totalAmount) : "0 ₫");

                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        List<Map<String, String>> filteredList = new ArrayList<>();
                        if (paymentStatuses != null) {
                            for (Map<String, String> status : paymentStatuses) {
                                String userId = status.get("userId");
                                if (userId != null && participantsId.contains(userId)) {
                                    filteredList.add(status);
                                }
                            }
                        }

                        filteredList.sort((map1, map2) -> {
                            String userId1 = map1.get("userId");
                            String userId2 = map2.get("userId");

                            if (userId1.equals(currentUserId)) {
                                return -1;
                            }
                            if (userId2.equals(currentUserId)) {
                                return 1;
                            }
                            return 0;
                        });

                        MemberStatusAdapter adapter = new MemberStatusAdapter(
                                filteredList,
                                currentUserId,
                                activityId,
                                totalAmount != null ? totalAmount : 0,
                                participantsId.size(),
                                userId -> {
                                    pendingPayUserId = userId;

                                    double splittedAmount = (totalAmount != null ? totalAmount : 0) / participantsId.size();

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

                        List<Map<String, String>> paymentStatuses = (List<Map<String, String>>) data.get("paymentStatusesId");

                        if (paymentStatuses != null) {
                            for (Map<String, String> status : paymentStatuses) {
                                if (status.get("userId").equals(userId)) {
                                    status.put("status", "paid");
                                    break;
                                }
                            }

                            db.collection("activities").document(activityId)
                                    .update("paymentStatusesId", paymentStatuses)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Payment status updated", Toast.LENGTH_SHORT).show();
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

    private void startStripePayment(double amount) {

        int amountInCents = (int) (amount * 100);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        CreatePaymentIntentRequest requestBody = new CreatePaymentIntentRequest(amountInCents, "usd");

        api.createPaymentIntent(requestBody).enqueue(new Callback<CreatePaymentIntentResponse>() {
            @Override
            public void onResponse(Call<CreatePaymentIntentResponse> call, Response<CreatePaymentIntentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    paymentIntentClientSecret = response.body().clientSecret;
                    presentPaymentSheet(paymentIntentClientSecret);
                } else {
                    Toast.makeText(ActivityDetailSenderActivity.this, "Server response failed", Toast.LENGTH_SHORT).show();
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


    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        Log.d(TAG, "onPaymentSheetResult: " + paymentSheetResult);

        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show();

            updatePaymentStatusToPaid(activityId, pendingPayUserId);

        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "The payment has been cancelled.", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            Toast.makeText(this, "Payment failed: " + failedResult.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }


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
                            db.collection("activities").document(activityId)
                                    .update("paymentStatusesId", paymentStatuses)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Payment status updated (paid)", Toast.LENGTH_SHORT).show();
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