package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.response.ClientResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ClientsApi {
    @GET("clients")
    Call<ClientResponse> getClients(@Query("is-active") Boolean isActive);

    @GET("clients/{id}")
    Call<ClientResponse> getClient(@Path("id") Integer clientId);

    @POST("clients")
    Call<Void> saveClient(@Body Client good);

    @DELETE("clients/{id}")
    Call<Void> deleteClient(@Path("id") Integer clientId);

    @PUT("clients/{id}")
    Call<Void> restoreClient(@Path("id") Integer clientId);

    @GET("clients/client-with-orders/{id}")
    Call<Boolean> checkClientOnOrdersAvailability(@Path("id") Integer clientId);

}
