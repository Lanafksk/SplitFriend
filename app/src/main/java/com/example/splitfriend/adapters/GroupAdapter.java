package com.example.splitfriend.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.GroupHelper;
import com.example.splitfriend.data.models.Group;
import com.example.splitfriend.user.GroupDetailActivity;
import com.example.splitfriend.viewHolders.GroupViewHolder;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupViewHolder> {
    private final List<Group> groupList;
    private final GroupHelper groupHelper;
    private final String currentUserId;
    private OnItemClickListener onItemClickListener;
    private final OnGroupActionListener onGroupActionListener;

    public GroupAdapter(List<Group> groupList, GroupHelper groupHelper, String currentUserId, OnGroupActionListener onGroupActionListener) {
        this.groupList = groupList;
        this.groupHelper = groupHelper;
        this.currentUserId = currentUserId;
        this.onGroupActionListener = onGroupActionListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);

        // 기본 그룹 정보 설정
        holder.groupName.setText(group.getName());
        holder.memberCount.setText(String.valueOf(group.getMembersId().size()));

        // 그룹 항목 클릭 시 OnItemClickListener 실행
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(group);
            } else {
                // 기본 액티비티 이동
                Intent intent = new Intent(holder.itemView.getContext(), GroupDetailActivity.class);
                intent.putExtra("groupId", group.getId());
                holder.itemView.getContext().startActivity(intent);
            }
        });

        // 삭제 버튼 숨기기
        holder.deleteButtonLayout.setVisibility(View.GONE);

        // 삭제 또는 나가기 버튼 클릭 처리
        holder.deleteButtonLayout.setOnClickListener(v -> {
            if (group.getLeaderId().equals(currentUserId)) {
                groupHelper.deleteGroup(group.getId())
                        .addOnSuccessListener(aVoid -> {
                            groupList.remove(position);
                            notifyItemRemoved(position);
                            onGroupActionListener.onGroupDeleted();
                        })
                        .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(), "Error deleting group: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                group.getMembersId().remove(currentUserId);
                groupHelper.updateGroup(group.getId(), "membersId", group.getMembersId())
                        .addOnSuccessListener(aVoid -> {
                            groupList.remove(position);
                            notifyItemRemoved(position);
                            onGroupActionListener.onGroupLeft();
                        })
                        .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(), "Error leaving group: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }


    @Override
    public int getItemCount() {
        return groupList.size();
    }

    // Set item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Group group);
    }

    public interface OnGroupActionListener {
        void onGroupDeleted();
        void onGroupLeft();
    }
}
