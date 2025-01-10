package com.example.splitfriend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.splitfriend.R;

import java.util.List;

public class AdminMenuAdapter extends ArrayAdapter<String> {

    public AdminMenuAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_admin_menu, parent, false);
        }

        String menuItem = getItem(position);
        TextView textView = convertView.findViewById(R.id.menuItemText);
        textView.setText(menuItem);

        return convertView;
    }
}