package com.dzaitsev.marshmallow.service;


import com.dzaitsev.marshmallow.service.api.ClientsApi;
import com.dzaitsev.marshmallow.service.api.DeliveryApi;
import com.dzaitsev.marshmallow.service.api.GoodsApi;
import com.dzaitsev.marshmallow.service.api.OrdersApi;
import com.dzaitsev.marshmallow.utils.GsonExt;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkService {


    private static NetworkService mInstance;
    private Retrofit mRetrofit;

    public static NetworkService getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkService();
        }
        return mInstance;
    }

    private NetworkService() {
        setNetworkProfile();
    }

    public GoodsApi getGoodsApi() {
        return mRetrofit.create(GoodsApi.class);
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

    public void setNetworkProfile() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();


        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://5.59.136.54:8080/")
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .addConverterFactory(GsonConverterFactory.create(GsonExt.getGson()))
                .client(okHttpClient)
                .build();
    }
}
