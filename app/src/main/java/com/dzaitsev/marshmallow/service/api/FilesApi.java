package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.Attachment;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FilesApi {
//    @GET("files")
//    Call<GoodsResponse> getGoods(@Query("is-active") Boolean bool);
//
//    @GET("goods/{id}")
//    Call<GoodsResponse> getGood(@Path("id") Integer goodId);

    @POST("files")
    @Multipart
    Call<Attachment> saveAttachment(@Part MultipartBody.Part file);

}
