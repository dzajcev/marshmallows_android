package com.dzaitsev.marshmallow.service;


import com.dzaitsev.marshmallow.service.api.AuthorizationApi;
import com.dzaitsev.marshmallow.service.api.ClientsApi;
import com.dzaitsev.marshmallow.service.api.DeliveryApi;
import com.dzaitsev.marshmallow.service.api.FilesApi;
import com.dzaitsev.marshmallow.service.api.GoodsApi;
import com.dzaitsev.marshmallow.service.api.InviteRequestsApi;
import com.dzaitsev.marshmallow.service.api.OrdersApi;
import com.dzaitsev.marshmallow.service.api.UsersApi;
import com.dzaitsev.marshmallow.utils.GsonExt;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkService {


    private static NetworkService mInstance;
    private final Retrofit mRetrofit;

    private String token;

    public static NetworkService getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkService();
        }
        return mInstance;
    }

    public void refreshToken(String token) {
        this.token = token;
    }

    private NetworkService() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request.Builder newRequestBuilder = chain.request().newBuilder();
                    if (token != null) {
                        newRequestBuilder.addHeader("Authorization", "Bearer " + token);
                    }
                    Request request = newRequestBuilder.addHeader("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        mRetrofit = new Retrofit.Builder()
//                .baseUrl("http://89.110.75.34:7001")
                .baseUrl("http://192.168.1.222:8080")
                .addConverterFactory(GsonConverterFactory.create(GsonExt.getGson()))
                .client(okHttpClient)
                .build();
    }

    public GoodsApi getGoodsApi() {
        return mRetrofit.create(GoodsApi.class);
    }

    public FilesApi getFilesApi() {
        return mRetrofit.create(FilesApi.class);
    }

    public ClientsApi getClientsApi() {
        return mRetrofit.create(ClientsApi.class);
    }

    public OrdersApi getOrdersApi() {
        return mRetrofit.create(OrdersApi.class);
    }

    public DeliveryApi getDeliveryApi() {
        return mRetrofit.create(DeliveryApi.class);
    }

    public UsersApi getUsersApi() {
        return mRetrofit.create(UsersApi.class);
    }

    public InviteRequestsApi getInviteRequestsApi() {
        return mRetrofit.create(InviteRequestsApi.class);
    }

    public AuthorizationApi getAuthorizationApi() {
        return mRetrofit.create(AuthorizationApi.class);
    }
}
