package com.example.splitfriend.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Node 서버 주소 (에뮬레이터에서 접근 시 10.0.2.2 사용)
    private static final String BASE_URL = "http://10.0.2.2:4242/";
    // 실제 기기에서 테스트하려면 PC의 IP:포트로 변경

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
