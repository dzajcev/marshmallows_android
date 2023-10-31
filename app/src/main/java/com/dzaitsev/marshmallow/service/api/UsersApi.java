package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.authorization.request.ChangePasswordRequest;
import com.dzaitsev.marshmallow.dto.authorization.request.SaveMyInfoRequest;
import com.dzaitsev.marshmallow.dto.response.UserInfoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UsersApi {
    @GET("users/my")
    Call<UserInfoResponse> getMyInfo();

    @POST("users/save")
    Call<Void> saveMyInfo(@Body SaveMyInfoRequest request);

    @POST("users/change-password")
    Call<Void> changePassword(@Body ChangePasswordRequest request);
}
