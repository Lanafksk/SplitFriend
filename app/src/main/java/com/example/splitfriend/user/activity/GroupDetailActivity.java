package com.example.splitfriend.user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.R;
import com.example.splitfriend.adapters.ActivityAdapter;
import com.example.splitfriend.user.group.CreateGroupActivity;
import com.example.splitfriend.user.group.HomeActivity;

public class GroupDetailActivity extends AppCompatActivity implements ActivityAdapter.OnActivityActionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_detail);

        // Set up the floating button click listener
        findViewById(R.id.floatingButton).setOnClickListener(v -> {
            Intent intent = new Intent(GroupDetailActivity.this, CreateActivityActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onActivityDelete(String activityId) {
        Toast.makeText(this, "Activity deleted successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityLeave(String activityId) {
        Toast.makeText(this, "Left Activity successfully", Toast.LENGTH_SHORT).show();
    }
}