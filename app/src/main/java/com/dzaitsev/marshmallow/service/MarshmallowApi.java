package com.dzaitsev.marshmallow.service;

import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.response.ClientResponse;
import com.dzaitsev.marshmallow.dto.response.DeliveryResponse;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;
import com.dzaitsev.marshmallow.dto.response.OrderResponse;

import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MarshmallowApi {
    @GET("goods")
    Call<GoodsResponse> getGoods();

    @GET("goods/{id}")
    Call<GoodsResponse> getGood(@Path("id") Integer clientId);
    @POST("goods")
    Call<Void> saveGood(@Body Good good);

    @GET("orders")
    Call<OrderResponse> getOrders(@Query("start") LocalDate start, @Query("end")LocalDate end,@Query("statuses") List<OrderStatus> statuses);

    @GET("orders/{id}")
    Call<OrderResponse> getOrder(@Path("id") Integer orderId);
    @POST("orders")
    Call<Void> saveOrder(@Body Order order);

    @DELETE("orders/{id}")
    Call<Void> deleteOrder(@Path("id") Integer orderId);

    @GET("clients")
    Call<ClientResponse> getClients();
    @GET("clients/{id}")
    Call<ClientResponse> getClient(@Path("id") Integer clientId);
    @POST("clients")
    Call<Void> saveClient(@Body Client good);

    @GET("delivery/{id}")
    Call<DeliveryResponse> getDelivery(@Path("id") Integer deliveryId);
    @GET("delivery")
    Call<DeliveryResponse> getDeliveries();

    @POST("delivery")
    Call<Void> saveDelivery(@Body Delivery good);

    @DELETE("delivery/{id}")
    Call<Void> deleteDelivery(@Path("id") Integer delivery);

}
