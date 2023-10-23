package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.DeliveryStatus;
import com.dzaitsev.marshmallow.dto.response.DeliveryResponse;

import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DeliveryApi {
    @GET("delivery/{id}")
    Call<DeliveryResponse> getDelivery(@Path("id") Integer deliveryId);

    @GET("delivery")
    Call<DeliveryResponse> getDeliveries(@Query("start") LocalDate start, @Query("end")LocalDate end,@Query("statuses") List<DeliveryStatus> statuses);

    @POST("delivery")
    Call<Void> saveDelivery(@Body Delivery good);

    @DELETE("delivery/{id}")
    Call<Void> deleteDelivery(@Path("id") Integer delivery);

}
