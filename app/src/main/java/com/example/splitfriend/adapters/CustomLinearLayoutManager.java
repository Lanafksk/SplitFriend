package com.example.splitfriend.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CustomLinearLayoutManager extends LinearLayoutManager {

    public CustomLinearLayoutManager(Context context) {
        super(context);
    }

    public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean canScrollVertically() {
        return false; // Disable vertical scrolling
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        int totalHeight = 0;

        // Check if there are items in the adapter
        if (getItemCount() > 0) {
            for (int i = 0; i < getItemCount(); i++) {
                try {
                    View view = recycler.getViewForPosition(i);
                    measureChild(view, widthSpec, heightSpec);
                    totalHeight += view.getMeasuredHeight();
                } catch (IndexOutOfBoundsException e) {
                    // Safeguard against data being unavailable during measurement
                    break;
                }
            }
        }

        // Set measured dimensions based on calculated height
        setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), totalHeight);
    }
}