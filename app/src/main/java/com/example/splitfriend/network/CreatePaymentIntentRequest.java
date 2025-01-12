package com.example.splitfriend.network;

import com.google.gson.annotations.SerializedName;

// 1) PaymentIntent Creation Request Body
public class CreatePaymentIntentRequest {
    @SerializedName("amount")
    public int amount;
    @SerializedName("currency")
    public String currency;

    public CreatePaymentIntentRequest(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
}
