package com.dzaitsev.marshmallow.service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkService {

    DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static NetworkService mInstance;
    private Retrofit mRetrofit;

    public static NetworkService getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkService();
        }
        return mInstance;
    }

    private NetworkService() {
        setNetworkProfile();
    }

    public MarshmallowApi getMarshmallowApi() {
        return mRetrofit.create(MarshmallowApi.class);
    }

    public void setNetworkProfile() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context)
                                -> new JsonPrimitive(localDateTimeFormatter.format(src)))
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, typeOfSrc, context)
                                -> new JsonPrimitive(localDateFormatter.format(src)))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type,
                                                           jsonDeserializationContext) ->
                                LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), localDateTimeFormatter))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type,
                                                       jsonDeserializationContext) ->
                                LocalDate.parse(json.getAsJsonPrimitive().getAsString(), localDateFormatter))
                .create();
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.45:8080/")
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
    }
}
