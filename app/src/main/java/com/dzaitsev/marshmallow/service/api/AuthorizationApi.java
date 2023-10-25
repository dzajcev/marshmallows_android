package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.authorization.SignInRequest;
import com.dzaitsev.marshmallow.dto.authorization.response.JwtAuthenticationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthorizationApi {
    @POST("auth/signin")
    Call<JwtAuthenticationResponse> signIn(@Body SignInRequest request);

}
