package com.example.splitfriend.user;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitfriend.R;
import com.example.splitfriend.adapters.BillAdapter;
import com.example.splitfriend.data.models.Bill;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityTotalAmountActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView activityTitle;
    private ImageView closeIcon;
    private TextView totalAmountTextView;
    private PieChart pieChart;
    private LinearLayout legendLayout;
    private RecyclerView recyclerView;

    private BillAdapter billAdapter;
    private List<Bill> billList = new ArrayList<>();

    private static final String[] CATEGORIES = {
            "Food", "Glossary", "Activity", "Present", "Travel"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_amount);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();

        closeIcon.setOnClickListener(view -> finish());

        String docId = getIntent().getStringExtra("docId");
        if (docId == null || docId.isEmpty()) {
            Toast.makeText(this, "Invalid Activity ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 해당 docId로 Firestore에서 데이터 가져오기
        fetchDataFromFirestore(docId);

    }

    private void initViews() {
        activityTitle = findViewById(R.id.activityTitle);
        closeIcon = findViewById(R.id.closeIcon);
        totalAmountTextView = findViewById(R.id.totalAmount);
        pieChart = findViewById(R.id.pieChart);
        legendLayout = findViewById(R.id.legendLayout);
        recyclerView = findViewById(R.id.recyclerView);

        activityTitle.setText("(Activity Name)");
    }

    private void setupRecyclerView() {
        billAdapter = new BillAdapter(billList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(billAdapter);
    }

    private void fetchDataFromFirestore(String documentId) {
        db.collection("activities")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object totalObj = documentSnapshot.get("totalAmount");
                        double totalAmount = 0.0;
                        if (totalObj instanceof Number) {
                            totalAmount = ((Number) totalObj).doubleValue();
                        }
                        totalAmountTextView.setText(formatPrice(totalAmount) + " d");

                        // name
                        String name = documentSnapshot.getString("name");
                        if (name != null) {
                            activityTitle.setText(name);
                        }

                        List<Map<String, Object>> billsData =
                                (List<Map<String, Object>>) documentSnapshot.get("bills");

                        if (billsData != null) {
                            for (Map<String, Object> billMap : billsData) {
                                double price = 0.0;
                                Object priceObj = billMap.get("price");
                                if (priceObj instanceof Number) {
                                    price = ((Number) priceObj).doubleValue();
                                }
                                String category = (String) billMap.get("category");
                                String note = (String) billMap.get("note");

                                if (category == null) category = "";
                                if (note == null) note = "";

                                Bill bill = new Bill(note, price, category);
                                billList.add(bill);
                            }
                            billAdapter.notifyDataSetChanged();
                        }

                        updatePieChart();

                    } else {
                        Toast.makeText(this, "The document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to import data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String formatPrice(double price) {
        return String.format("%,.0f", price);
    }

    private void updatePieChart() {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (String cat : CATEGORIES) {
            categoryTotals.put(cat, 0.0);
        }

        for (Bill bill : billList) {
            String cat = bill.getCategory();
            double oldSum = categoryTotals.containsKey(cat) ? categoryTotals.get(cat) : 0.0;
            categoryTotals.put(cat, oldSum + bill.getPrice());
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (String cat : CATEGORIES) {
            double value = categoryTotals.get(cat);
            if (value > 0) {
                entries.add(new PieEntry((float) value, cat));
                colors.add(getCategoryColor(cat)); // 각 카테고리별 색상 추가
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setColors(colors); // 색상 리스트 설정
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();

        updateLegend(categoryTotals);
    }

    private void updateLegend(Map<String, Double> categoryTotals) {
        legendLayout.removeAllViews(); // 초기화

        for (String cat : CATEGORIES) {
            double value = categoryTotals.get(cat);
            if (value <= 0) continue; // 0이면 표시 X

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(0, 8, 0, 8);

            View colorBox = new View(this);
            colorBox.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
            colorBox.setBackgroundColor(getCategoryColor(cat));

            TextView legendText = new TextView(this);
            legendText.setText(cat + " - " + formatPrice(value) + " d");
            legendText.setTextSize(16f);
            legendText.setPadding(16, 0, 0, 0);

            itemLayout.addView(colorBox);
            itemLayout.addView(legendText);
            legendLayout.addView(itemLayout);
        }
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "Food":
                return Color.parseColor("#1E90FF");
            case "Glossary":
                return Color.parseColor("#9970CE");
            case "Activity":
                return Color.parseColor("#E190A3");
            case "Present":
                return Color.parseColor("#FF9800");
            case "Travel":
                return Color.parseColor("#EC407A");
            default:
                return Color.GRAY;
        }
    }
}
