package com.paige.speechtotext;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 서버 URL
    private static final String BASE_URL_LOCAL = "http://192.168.0.79:9090/";

    private Retrofit retrofit = null;
    private static RetrofitClient mInstance;

    private RetrofitClient() {

        // Create a new object from HttpLoggingInterceptor
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add Interceptor to HttpClient - 이거 안붙이면 인생에서 후회함. 개꿀 디버깅.
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_LOCAL)    // 빌드 설정에 따라 다른 URL로 연결 (2019. 12. 05)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }

    public Api getApi() {
        return retrofit.create(Api.class);
    }

}