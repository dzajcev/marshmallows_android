package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.authorization.response.JwtAuthenticationResponse;
import com.dzaitsev.marshmallow.dto.authorization.response.JwtSignUpResponse;
import com.dzaitsev.marshmallow.dto.request.SignInRequest;
import com.dzaitsev.marshmallow.dto.request.SignUpRequest;
import com.dzaitsev.marshmallow.dto.request.VerificationCodeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthorizationApi {
    @POST("auth/signin")
    Call<JwtAuthenticationResponse> signIn(@Body SignInRequest request);

    @POST("auth/signup")
    Call<JwtSignUpResponse> signUp(@Body SignUpRequest request);

    @POST("auth/verify-code")
    Call<JwtAuthenticationResponse> verify(@Body VerificationCodeRequest request);

    @POST("auth/send-code")
    Call<JwtAuthenticationResponse> sendCode();
}
