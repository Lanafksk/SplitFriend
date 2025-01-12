package com.example.splitfriend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.data.models.Bill;

import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    private List<Bill> billList;

    public BillAdapter(List<Bill> billList) {
        this.billList = billList;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_total_amount, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);

        // Set sequential numbering for activityLabel
        String activityLabelText = (position + 1) + ". Bill";
        holder.activityLabel.setText(activityLabelText);

        // Set category tag
        holder.categoryTag.setText(bill.getCategory());
        holder.categoryTag.setBackgroundResource(getCategoryBackground(bill.getCategory()));

        // Set memo and price
        holder.memoText.setText(bill.getNote());
        holder.priceText.setText(formatPrice(bill.getPrice()));
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView activityLabel;
        TextView categoryTag;
        TextView memoText;
        TextView priceText;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            activityLabel = itemView.findViewById(R.id.activityLabel);
            categoryTag = itemView.findViewById(R.id.categoryTag);
            memoText = itemView.findViewById(R.id.memoText);
            priceText = itemView.findViewById(R.id.priceText);
        }
    }

    private String formatPrice(double price) {
        return String.format("%,.0f", price) + " d";
    }

    private int getCategoryBackground(String category) {
        switch (category) {
            case "Food":
                return R.color.category1;
            case "Glossary":
                return R.color.category2;
            case "Activity":
                return R.color.category3;
            case "Present":
                return R.color.category4;
            case "Travel":
                return R.color.category5;
            default:
                return R.drawable.default_category_background;
        }
    }
}