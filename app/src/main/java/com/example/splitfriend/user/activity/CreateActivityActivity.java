package com.example.splitfriend.user.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;


import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.R;
import com.example.splitfriend.data.helpers.ActivityHelper;
import com.example.splitfriend.data.models.Activity;
import com.example.splitfriend.data.models.Bill;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateActivityActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String userId;
    private String groupId;
    private String userName;
    private EditText activityNameInput, payeeInput, bankNameInput, bankAccountInput;
    private TextView dateTextView;
    private Spinner currencySpinner;
    private String selectedCurrency;
    private Button saveButton;
    private ActivityHelper activityHelper;

    private LinearLayout billsContainer;
    private List<Bill> billList;
    private Map<String, String> currencySymbols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_activity);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userName = currentUser.getDisplayName();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");

        // Initialize views
        activityNameInput = findViewById(R.id.activityNameInput);
        payeeInput = findViewById(R.id.payeeInput);
        bankNameInput = findViewById(R.id.bankNameInput);
        bankAccountInput = findViewById(R.id.bankAccountInput);
        currencySpinner = findViewById(R.id.currencySpinner);
        saveButton = findViewById(R.id.bottomButton);
        activityHelper = new ActivityHelper();
        billList = new ArrayList<>();

        // Initialize currency symbols
        initializeCurrencySymbols();

        datePickerSetting();
        currencySpinnerSetting();
        billCreateSetting();


        // Set up Save Button click listener
        saveButton.setOnClickListener(v -> saveActivity());
//        saveButton.setOnClickListener(v -> saveBill());
    }

    private void datePickerSetting(){
        // Initialize views for date picker
        LinearLayout datePickerLayout = findViewById(R.id.datePickerLayout);
        dateTextView = findViewById(R.id.dateTextView);

        // Set initial date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());
        dateTextView.setText(formattedDate); // 초기 날짜 설정

        updateDateInView(calendar);

        // Set click listener for DatePicker
        datePickerLayout.setOnClickListener(v -> showDatePicker(calendar));
    }

    private void showDatePicker(Calendar calendar) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    updateDateInView(calendar); // Update the displayed date
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateInView(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        dateTextView.setText(dateFormat.format(calendar.getTime()));

    }

    private void initializeCurrencySymbols() {
        currencySymbols = new HashMap<>();
        currencySymbols.put("Vietnam (dong)", "₫");
        currencySymbols.put("Korea (won)", "₩");
        currencySymbols.put("United States (Dollar)", "$");
        currencySymbols.put("Japanese (Yen)", "¥");
    }

    private void currencySpinnerSetting() {
        // Initialize Spinner
        currencySpinner = findViewById(R.id.currencySpinner);

        // Create a list of currencies
        String[] currencies = {"Vietnam (dong)", "Korea (won)", "United States (Dollar)", "Japanese (Yen)"};

        // Set up Spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        // Set Spinner item selection listener
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCurrency = currencies[position];
                String symbol = currencySymbols.get(selectedCurrency); // Get symbol from the map

                // Update all existing bill items with the new currency symbol
                updateCurrencySymbol(symbol);
            }

            private void updateCurrencySymbol(String symbol) {
                // Iterate through billsContainer to update all currency symbols
                for (int i = 0; i < billsContainer.getChildCount(); i++) {
                    View billItem = billsContainer.getChildAt(i);

                    TextView currencySymbol = billItem.findViewById(R.id.currencySymbol);
                    if (currencySymbol != null) {
                        currencySymbol.setText(symbol); // Update the currency symbol
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCurrency = null;
            }
        });
    }

    private void billCreateSetting(){
        // Initialize Views
        billsContainer = findViewById(R.id.billsContainer);
        Button addBillButton = findViewById(R.id.addBillButton);

        // Add Bill Button Listener
        addBillButton.setOnClickListener(v -> addNewBill());
    }

    private void addNewBill() {
        // Inflate a new bill item
        View billItem = LayoutInflater.from(this).inflate(R.layout.bill_item, billsContainer, false);

        // Set the index for the new item
        TextView itemIndex = billItem.findViewById(R.id.itemIndex); // TextView의 ID가 itemIndex라고 가정
        int index = billsContainer.getChildCount() + 1; // 현재 아이템 개수 + 1
        itemIndex.setText(String.valueOf(index)); // 인덱스를 TextView에 설정

        // Set click listener for category button
        ImageButton categoryButton = billItem.findViewById(R.id.categoryButton); // ID 변경
        categoryButton.setOnClickListener(v -> categoryPopupHandle(billItem)); // billItem 전달

        // Add the new bill item to the container
        billsContainer.addView(billItem);
    }

    private void categoryPopupHandle(View billItem) {
        // BottomSheetDialog 초기화
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);

        // 팝업 레이아웃 설정
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_category_list, null);
        bottomSheetDialog.setContentView(popupView);

        // 닫기 버튼 설정
        ImageButton closeButton = popupView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        popupView.findViewById(R.id.category_food).setOnClickListener(v -> {
            setCategory(billItem, "Food");
            bottomSheetDialog.dismiss();
        });

        popupView.findViewById(R.id.category_glossary).setOnClickListener(v -> {
            setCategory(billItem, "Glossary");
            bottomSheetDialog.dismiss();
        });

        popupView.findViewById(R.id.category_activity).setOnClickListener(v -> {
            setCategory(billItem, "Activity");
            bottomSheetDialog.dismiss();
        });

        popupView.findViewById(R.id.category_present).setOnClickListener(v -> {
            setCategory(billItem, "Present");
            bottomSheetDialog.dismiss();
        });

        popupView.findViewById(R.id.category_travel).setOnClickListener(v -> {
            setCategory(billItem, "Travel");
            bottomSheetDialog.dismiss();
        });

        // BottomSheetDialog 표시
        bottomSheetDialog.show();
    }

    private void setCategory(View billItem, String category) {
        TextView categoryTitle = billItem.findViewById(R.id.categoryTitle);
        if (categoryTitle != null) {
            categoryTitle.setText(category);
            Toast.makeText(this, "Selected category: " + category, Toast.LENGTH_SHORT).show();
            Log.d("PopupDebug", "카테고리 설정됨: " + category);
        } else {
            Log.e("PopupDebug", "categoryTitle TextView를 찾을 수 없음");
        }
    }

    private void saveBill(){
        billList.clear(); // Clear previous bills to avoid duplicates

        // Iterate through billsContainer to collect data
        for (int i = 0; i < billsContainer.getChildCount(); i++) {
            View billItem = billsContainer.getChildAt(i);

            // Get fields from the current bill item
            EditText noteInput = billItem.findViewById(R.id.noteInput);
            EditText priceInput = billItem.findViewById(R.id.priceInput);
            TextView categoryTitle = billItem.findViewById(R.id.categoryTitle);

            // Get values
            String note = noteInput.getText().toString().trim();
            String priceText = priceInput.getText().toString().replaceAll("[^\\d.]", "").trim();
            double price = priceText.isEmpty() ? 0.0 : Double.parseDouble(priceText);
            String category = categoryTitle.getText().toString().trim();

            // Validate fields (optional)
            if (note.isEmpty() || price <= 0 ) {
                Toast.makeText(this, "Please fill all fields in Bill " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new Bill object and add to the list
            Bill bill = new Bill(note, price, category);
            billList.add(bill);
        }
    }

    private void saveActivity() {
        // Get input values
        String activityName = activityNameInput.getText().toString().trim();
        String payee = payeeInput.getText().toString().trim();
        String bankName = bankNameInput.getText().toString().trim();
        String bankAccount = bankAccountInput.getText().toString().trim();
        Date activityDate;
        try {
            activityDate = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(dateTextView.getText().toString());
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate inputs
        if (activityName.isEmpty() || payee.isEmpty() || bankName.isEmpty() || bankAccount.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        saveBill();
        if (billList.isEmpty()) {
            Toast.makeText(this, "Please add at least one bill", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect participant data
        List<String> participants = new ArrayList<>();
        participants.add(userId); // Add the current user as the default participant

        // Create a new Activity object
        Activity activity = new Activity(
                null, // ID will be generated by Firestore
                groupId,
                activityDate,
                activityName,
                selectedCurrency,
                payee,
                bankName,
                bankAccount,
                billList,
                userId
        );
        activity.setParticipantsId(participants);


        // Save activity to Firestore
        activityHelper.createActivity(activity)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Activity created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
