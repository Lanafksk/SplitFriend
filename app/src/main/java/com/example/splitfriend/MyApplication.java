package com.example.splitfriend;

import android.app.Application;

import com.stripe.android.PaymentConfiguration;

// 예: MyApplication.java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Stripe 대시보드에서 받은 Publishable Key 설정
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51Qf351CrcAaK1Q335YCesPsXL2WTpcENTnz354eeReK0Z6GNb1rfI1SNLhsshw9Qi8XxzA1W9ANiBABJp9S04wJx00NaWtecNW" // Publishable key
        );
    }
}
