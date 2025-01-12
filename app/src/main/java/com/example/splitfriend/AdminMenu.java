package com.example.splitfriend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.adapters.AdminMenuAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AdminMenu extends AppCompatActivity {
    ListView menuListView;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_menu);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        menuListView = findViewById(R.id.menuListView);
        mAuth = FirebaseAuth.getInstance();

        List<String> menuItems = new ArrayList<>();
        menuItems.add("Logout");

        AdminMenuAdapter adapter = new AdminMenuAdapter(this, menuItems);
        menuListView.setAdapter(adapter);

        menuListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                mAuth.signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminMenu.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}