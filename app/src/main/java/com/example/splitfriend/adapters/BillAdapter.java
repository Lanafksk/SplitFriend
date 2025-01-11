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
                .inflate(R.layout.item_billfortotal, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);
        String billInfo = bill.getCategory() + " "
                + bill.getNote() + " "
                + formatPrice(bill.getPrice());
        holder.tvBillInfo.setText(billInfo);
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tvBillInfo;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBillInfo = itemView.findViewById(R.id.tvBillInfo);
        }
    }

    private String formatPrice(double price) {
        return String.format("%,.0f", price);
    }
}
