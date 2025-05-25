package com.example.gamemology.api;

import com.example.gamemology.BuildConfig;
import com.example.gamemology.utils.Constants;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://api.rawg.io/api/";
    private static ApiClient instance;
    private final Retrofit retrofit;

    private ApiClient() {
        // Create logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create api key interceptor
        Interceptor apiKeyInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();

                // Add API key to every request
                // Di dalam ApiClient.java, dalam interceptor:
                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("key", Constants.RAWG_API_KEY)
                        .build();

                Request request = original.newBuilder()
                        .url(url)
                        .build();

                return chain.proceed(request);
            }
        };

        // Create OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        // Create Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }
}