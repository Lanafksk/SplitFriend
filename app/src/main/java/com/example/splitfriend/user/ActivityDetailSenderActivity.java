package com.example.splitfriend.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.splitfriend.R;

public class ActivityDetailSenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_sender); // XML 파일 이름

        LinearLayout totalAmountSection = findViewById(R.id.totalAmountSection);

        totalAmountSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityDetailSenderActivity.this, ActivityTotalAmountActivity.class);
                startActivity(intent);
            }
        });
    }

}