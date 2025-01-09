package com.example.splitfriend.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.splitfriend.R;
import com.example.splitfriend.adapters.ActivityAdapter;
import com.example.splitfriend.data.helpers.ActivityHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityDetailSenderActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userId;
    private String acitivityId;
    private String bankAccount, bankName;
    private TextView activityName, activityDate, activityAmount;
    private ImageButton backButton;
    private ActivityAdapter activityAdapter;
    private ActivityHelper activityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_sender);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Intent I = getIntent();
        acitivityId = I.getStringExtra("activityId");

        initializeViews();

        backButton.setOnClickListener(v -> finish());

        ActivityHelper activityHelper = new ActivityHelper();
        activityHelper.getActivityById(acitivityId).addOnSuccessListener(documentSnapshot -> {
            activityName.setText(documentSnapshot.getString("name"));

            // Date 처리
            Date activityDateValue = documentSnapshot.getDate("date");
            if (activityDateValue != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
                String formattedDate = dateFormat.format(activityDateValue);
                activityDate.setText(formattedDate);
            } else {
                activityDate.setText("No Date Available");
            }

            // Total Amount
            Double totalAmount = documentSnapshot.getDouble("totalAmount");
            if (totalAmount != null) {
                activityAmount.setText(String.format(Locale.getDefault(), "%.2f", totalAmount));
            } else {
                activityAmount.setText("0.00");
            }

            bankAccount = documentSnapshot.getString("bankAccount");
            bankName = documentSnapshot.getString("bankName");
        });
    }

    private void initializeViews() {
        activityName = findViewById(R.id.activityNameTextView);
        activityDate = findViewById(R.id.dateTextView);
        activityAmount = findViewById(R.id.totalAmountTextView);
        backButton = findViewById(R.id.backButton);
    }


}