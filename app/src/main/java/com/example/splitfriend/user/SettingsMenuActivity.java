package com.example.splitfriend.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.R;
import com.example.splitfriend.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsMenuActivity extends AppCompatActivity {

    private ListView menuListView;
    private String[] menuItems = {"Edit Profile", "Find Group", "Log out"};
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        // Initialize FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            startActivity(new Intent(SettingsMenuActivity.this, HomeActivity.class));
            finish();
        });

        menuListView = findViewById(R.id.menuListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems);
        menuListView.setAdapter(adapter);

        menuListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (menuItems[position]) {
                case "Edit Profile":
                    Intent editProfileIntent = new Intent(SettingsMenuActivity.this, SettingsProfileActivity.class);
                    startActivity(editProfileIntent);
                    break;
                case "Find Group":
                    Intent findGroupIntent = new Intent(SettingsMenuActivity.this, FindGroupActivity.class);
                    startActivity(findGroupIntent);
                    break;
                case "Log out":
                    // Firebase logout
                    firebaseAuth.signOut();
                    Toast.makeText(SettingsMenuActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    // Navigate to WelcomeActivity
                    Intent logoutIntent = new Intent(SettingsMenuActivity.this, WelcomeActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;
                default:
                    Toast.makeText(SettingsMenuActivity.this, "Unknown menu item", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
