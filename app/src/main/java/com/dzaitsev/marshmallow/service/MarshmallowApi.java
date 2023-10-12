package com.dzaitsev.marshmallow.service;

import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.ClientResponse;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MarshmallowApi {
    @GET("goods")
    Call<GoodsResponse> getGoods();
    @POST("goods")
    Call<Void> saveGood(@Body Good good);

    @GET("orders")
    Call<GoodsResponse> getOrders();
    @POST("orders")
    Call<Void> saveOrder(@Body Order order);

    @GET("clients")
    Call<ClientResponse> getClients();
}
