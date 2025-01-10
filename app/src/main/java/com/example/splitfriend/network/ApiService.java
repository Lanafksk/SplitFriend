package com.example.splitfriend.network;

import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    // [POST] /create-payment-intent
    @POST("create-payment-intent")
    Call<CreatePaymentIntentResponse> createPaymentIntent(
            @Body CreatePaymentIntentRequest request
    );
}
