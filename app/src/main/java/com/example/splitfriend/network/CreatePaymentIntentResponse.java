package com.example.splitfriend.network;

import com.google.gson.annotations.SerializedName;

// 2) Server response
public class CreatePaymentIntentResponse {
    @SerializedName("clientSecret")
    public String clientSecret;
}
