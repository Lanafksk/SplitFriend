package com.example.splitfriend.network;

import com.google.gson.annotations.SerializedName;

// 2) 서버 응답
public class CreatePaymentIntentResponse {
    @SerializedName("clientSecret")
    public String clientSecret;
}
