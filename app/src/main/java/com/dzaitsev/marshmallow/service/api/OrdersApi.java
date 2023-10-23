package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.response.OrderResponse;

import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrdersApi {
    @GET("orders")
    Call<OrderResponse> getOrders(@Query("start") LocalDate start, @Query("end") LocalDate end, @Query("statuses") List<OrderStatus> statuses);

    @GET("orders/delivery")
    Call<OrderResponse> getOrdersForDelivery();

    @GET("orders/{id}")
    Call<OrderResponse> getOrder(@Path("id") Integer orderId);

    @POST("orders")
    Call<Void> saveOrder(@Body Order order);

    @DELETE("orders/{id}")
    Call<Void> deleteOrder(@Path("id") Integer orderId);

    @GET("orders/notification/{id}")
    Call<Boolean> clientIsNotificated(@Path("id") Integer orderId);

    @PUT("orders/notification/{id}")
    Call<Void> setClientIsNotificated(@Path("id") Integer orderId);
}
