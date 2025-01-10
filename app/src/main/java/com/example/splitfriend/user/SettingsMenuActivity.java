package com.example.splitfriend.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitfriend.R;
import com.example.splitfriend.WelcomeActivity;
import com.example.splitfriend.network.ApiClient;
import com.example.splitfriend.network.ApiService;
import com.example.splitfriend.network.CreatePaymentIntentRequest;
import com.example.splitfriend.network.CreatePaymentIntentResponse;
import com.example.splitfriend.user.group.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsMenuActivity extends AppCompatActivity {

    private ListView menuListView;
    private String[] menuItems = {"Edit Profile", "Find Group", "Log out"};
    private FirebaseAuth firebaseAuth;

    // [1] Stripe PaymentSheet 관련 필드 추가
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private static final int PREMIUM_AMOUNT_CENTS = 500000;
    private static final String PREMIUM_CURRENCY = "vnd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        // FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            startActivity(new Intent(SettingsMenuActivity.this, HomeActivity.class));
            finish();
        });

        // ListView (Edit Profile, Find Group, Log out)
        menuListView = findViewById(R.id.menuListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems);
        menuListView.setAdapter(adapter);
        menuListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (menuItems[position]) {
                case "Edit Profile":
                    Intent editProfileIntent = new Intent(SettingsMenuActivity.this, SettingsProfileActivity.class);
                    startActivity(editProfileIntent);
                    break;
                case "Find Group":
                    Intent findGroupIntent = new Intent(SettingsMenuActivity.this, FindGroupActivity.class);
                    startActivity(findGroupIntent);
                    break;
                case "Log out":
                    firebaseAuth.signOut();
                    Toast.makeText(SettingsMenuActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent logoutIntent = new Intent(SettingsMenuActivity.this, WelcomeActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;
                default:
                    Toast.makeText(SettingsMenuActivity.this, "Unknown menu item", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        RelativeLayout premiumButton = findViewById(R.id.premiumButton);
        premiumButton.setOnClickListener(v -> {
            createPaymentIntentForPremium();
        });
    }


    private void createPaymentIntentForPremium() {
        // 1) Retrofit API Service
        ApiService api = ApiClient.getClient().create(ApiService.class);

        CreatePaymentIntentRequest requestBody =
                new CreatePaymentIntentRequest(PREMIUM_AMOUNT_CENTS, PREMIUM_CURRENCY);

        api.createPaymentIntent(requestBody)
                .enqueue(new Callback<CreatePaymentIntentResponse>() {
                    @Override
                    public void onResponse(Call<CreatePaymentIntentResponse> call, Response<CreatePaymentIntentResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            paymentIntentClientSecret = response.body().clientSecret;
                            presentPaymentSheet(paymentIntentClientSecret);
                        } else {
                            Toast.makeText(SettingsMenuActivity.this, "Server response failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CreatePaymentIntentResponse> call, Throwable t) {
                        Toast.makeText(SettingsMenuActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void presentPaymentSheet(String clientSecret) {
        if (clientSecret == null || clientSecret.isEmpty()) {
            Toast.makeText(this, "clientSecret is null/empty", Toast.LENGTH_SHORT).show();
            return;
        }

        PaymentSheet.Configuration configuration =
                new PaymentSheet.Configuration("SplitFriend Premium");

        paymentSheet.presentWithPaymentIntent(
                clientSecret,
                configuration
        );
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Payment successful
            Toast.makeText(this, "Premium payment successful! Thank you!", Toast.LENGTH_LONG).show();

            // [1] Get FirebaseFirestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // [2] Retrieve the current user ID
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();

                // [3] Update the "role" field to "superuser"
                db.collection("users")
                        .document(userId)
                        .update("role", "superuser")
                        .addOnSuccessListener(aVoid -> {
                            // Logic after successful update
                            Toast.makeText(this, "The role has been updated to superuser.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Error handling
                            Toast.makeText(this, "Failed to update role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment was canceled.", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            Toast.makeText(this, "Payment failed: " + failedResult.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
