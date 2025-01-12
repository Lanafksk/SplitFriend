package com.example.splitfriend.user;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.adapters.GroupAdapter;
import com.example.splitfriend.data.helpers.GroupHelper;
import com.example.splitfriend.data.models.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FindGroupActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView groupRecyclerView;
    private GroupAdapter groupAdapter;
    private List<Group> groupList;
    private FirebaseFirestore db;
    private GroupHelper groupHelper;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group);

        // UI 초기화
        ImageButton backButton = findViewById(R.id.backButton);
        searchEditText = findViewById(R.id.searchEditText);
        groupRecyclerView = findViewById(R.id.groupRecyclerView);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();
        groupHelper = new GroupHelper();

        // Firebase Auth 초기화
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // RecyclerView 설정
        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(groupList, groupHelper, userId, null);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupRecyclerView.setAdapter(groupAdapter);

        // 뒤로가기 버튼 동작
        backButton.setOnClickListener(v -> finish());

        // 그룹 클릭 리스너 설정
        groupAdapter.setOnItemClickListener(group -> showJoinGroupDialog(group));

        // 검색 입력 감지
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 9) { // 초대 코드 길이가 "XXXX-XXXX"인지 확인
                    searchGroupByInviteCode(s.toString());
                } else {
                    groupList.clear();
                    groupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchGroupByInviteCode(String inviteCode) {
        db.collection("groups")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        groupList.clear();
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            groupList.addAll(task.getResult().toObjects(Group.class));
                            groupAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No group found with this invite code", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error searching groups: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showJoinGroupDialog(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join Group")
                .setMessage("Do you want to join \"" + group.getName() + "\"?")
                .setPositiveButton("Join", (dialog, which) -> joinGroup(group))
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void joinGroup(Group group) {
        if (group.getMembersId().contains(userId)) {
            Toast.makeText(this, "You are already a member of this group", Toast.LENGTH_SHORT).show();
            return;
        }

        group.getMembersId().add(userId);

        groupHelper.updateGroup(group.getId(), "membersId", group.getMembersId())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "You have successfully joined the group!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to join the group: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
