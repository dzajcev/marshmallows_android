package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.response.ResultResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GoodsApi {
    @GET("goods")
    Call<ResultResponse<List<Good>>> getGoods(@Query("is-active") Boolean bool);

    @GET("goods/{id}")
    Call<ResultResponse<Good>> getGood(@Path("id") Integer goodId);

    @POST("goods")
    Call<ResultResponse<Good>> saveGood(@Body Good good);

    @DELETE("goods/{id}")
    Call<ResultResponse<Void>> deleteGood(@Path("id") Integer goodId);

    @PUT("goods/{id}")
    Call<ResultResponse<Void>> restoreGood(@Path("id") Integer goodId);

    @GET("goods/good-with-orders-lines/{id}")
    Call<ResultResponse<Boolean>> checkGoodOnOrdersAvailability(@Path("id") Integer goodId);
}
