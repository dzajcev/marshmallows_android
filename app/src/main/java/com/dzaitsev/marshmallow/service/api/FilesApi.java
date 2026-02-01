package com.dzaitsev.marshmallow.service.api;

import com.dzaitsev.marshmallow.dto.Attachment;
import com.dzaitsev.marshmallow.dto.response.ResultResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface FilesApi {
    @POST("files")
    @Multipart
    Call<ResultResponse<Attachment>> saveAttachment(@Part MultipartBody.Part file);

    @DELETE("files/{id}")
    Call<ResultResponse<Void>> delete(@Path("id") Integer id);

}
