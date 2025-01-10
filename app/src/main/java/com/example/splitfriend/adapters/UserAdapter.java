package com.example.splitfriend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.UserHelper;
import com.example.splitfriend.data.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUserId.setText(user.getUserId());
        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());
        holder.etUserId.setText(user.getUserId());
        holder.etUserName.setText(user.getName());
        holder.etUserEmail.setText(user.getEmail());
        holder.tvUid.setText(user.getId());

        if (user.getRole().equals("admin")) {
            holder.displayLayout.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "You can't edit admin user", Toast.LENGTH_SHORT).show();
            });
        }

        holder.deleteButton.setOnClickListener(v -> {
            // delete user
            // Wait for implementation
        });

        holder.saveButton.setOnClickListener(v -> {
            String userId = holder.etUserId.getText().toString();
            String name = holder.etUserName.getText().toString();
            String email = holder.etUserEmail.getText().toString();
            saveUser(user, userId, name, email);
        });
    }

    public void saveUser(User user, String userId, String name, String email) {
        user.setUserId(userId);
        user.setName(name);
        user.setEmail(email);
        UserHelper userHelper = new UserHelper();
        userHelper.updateUser(user);
    }



    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserId, tvUserName, tvUserEmail, tvUid;
        EditText etUserId, etUserName, etUserEmail;
        LinearLayout editLayout, displayLayout;

        ImageButton closeEditButton;
        Button saveButton, deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUid = itemView.findViewById(R.id.tvUid);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            etUserId = itemView.findViewById(R.id.etUserId);
            etUserName = itemView.findViewById(R.id.etUserName);
            etUserEmail = itemView.findViewById(R.id.etUserEmail);
            editLayout = itemView.findViewById(R.id.editLayout);
            displayLayout = itemView.findViewById(R.id.displayLayout);
            saveButton = itemView.findViewById(R.id.btnSave);
            deleteButton = itemView.findViewById(R.id.btnDelete);
            closeEditButton = itemView.findViewById(R.id.closeEditButton);
            etUserEmail.setEnabled(false);

            closeEditButton.setOnClickListener(v -> {
                editLayout.setVisibility(View.GONE);
                displayLayout.setVisibility(View.VISIBLE);
            });

            displayLayout.setOnClickListener(v -> {
                editLayout.setVisibility(View.VISIBLE);
                displayLayout.setVisibility(View.GONE);
            });

        }
    }


}