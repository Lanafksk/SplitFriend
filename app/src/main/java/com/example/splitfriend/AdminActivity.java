package com.example.splitfriend;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.adapters.UserAdapter;
import com.example.splitfriend.data.helpers.UserHelper;
import com.example.splitfriend.data.models.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView itemUserRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private UserHelper userHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        userHelper = new UserHelper();

        itemUserRecyclerView = findViewById(R.id.itemUserRecyclerView);
        itemUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList);
        itemUserRecyclerView.setAdapter(userAdapter);

        loadUsers();

        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminMenu.class);
            startActivity(intent);
        });

        View fabInclude = findViewById(R.id.fabInclude);
        fabInclude.setOnClickListener(v -> showAddUserPopup());
    }

    private void loadUsers() {
        userHelper.setupRealtimeUpdates((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots != null) {
                userList.clear();
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showAddUserPopup() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_bottom_add_user, null);
        bottomSheetDialog.setContentView(popupView);

        ImageButton closeButton = popupView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Handle adding user logic here

        bottomSheetDialog.show();
    }
}