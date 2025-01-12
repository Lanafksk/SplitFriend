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

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Acceptance Id receive an Integrated
        activityId = getIntent().getStringExtra("activityId");

        // View initialization
        activityNameTextView = findViewById(R.id.activityNameTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        otherMembersRecyclerView = findViewById(R.id.otherMembersRecyclerView);
        otherMembersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        totalAmountSection = findViewById(R.id.totalAmountSection);
        totalAmountSection.setOnClickListener(v -> {
            // Navigate to ActivityTotalAmountActivity
            Intent intent = new Intent(ActivityDetailPayeeActivity.this, ActivityTotalAmountActivity.class);
            // Pass the Firestore document ID to the next Activity
            intent.putExtra("docId", activityId);
            startActivity(intent);
        });

        // Import Data
        loadActivityDetails();

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.dark_blue));
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
                        // Retrieve data from Firebase
                        String activityName = documentSnapshot.getString("name");
                        Double totalAmount = documentSnapshot.getDouble("totalAmount");
                        List<Map<String, String>> paymentStatuses =
                                (List<Map<String, String>>) documentSnapshot.get("paymentStatusesId");
                        List<String> participantsId =
                                (List<String>) documentSnapshot.get("participantsId");

                        // Update the TextView
                        activityNameTextView.setText(activityName != null ? activityName : "Unknown Activity");
                        // Safely format totalAmount as an integer with commas
                        String formattedTotalAmount = totalAmount != null
                                ? String.format(Locale.getDefault(), "%,d ₫", Math.round(totalAmount))
                                : "0 ₫";
                        totalAmountTextView.setText(formattedTotalAmount);


                        // Connect RecyclerView with Adapter
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
                                userId -> {}  // No click event on the Payee page
                        );
                        otherMembersRecyclerView.setAdapter(adapter);
                    }
                });
    }
}
