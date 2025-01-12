package com.example.splitfriend.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Node server address (use 10.0.2.2 when accessed from emulator)
    private static final String BASE_URL = "http://10.0.2.2:4242/";
    // To test the actual device, change to the IP: port
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
