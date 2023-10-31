package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.InviteRequestDirection;
import com.dzaitsev.marshmallow.dto.request.AcceptInviteRequest;
import com.dzaitsev.marshmallow.dto.request.AddInviteRequest;
import com.dzaitsev.marshmallow.dto.response.DeliverymenResponse;
import com.dzaitsev.marshmallow.dto.response.InviteRequestsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface InviteRequestsApi {
    @POST("invite-request")
    Call<Void> addInviteRequest(@Body AddInviteRequest request);

    @GET("invite-request/get-deliverymen")
    Call<DeliverymenResponse> getDeliverymen();

    @GET("invite-request")
    Call<InviteRequestsResponse> getInviteRequests(@Query("direction") InviteRequestDirection direction, @Query("is-accepted") Boolean bool);

    @DELETE("invite-request/{requestId}")
    Call<Void> deleteInviteRequest(@Path("requestId") Integer requestId);

    @POST("invite-request/accept-invite-request")
    Call<Void> acceptInviteRequest(@Body AcceptInviteRequest request);
}
