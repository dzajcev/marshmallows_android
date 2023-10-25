package com.dzaitsev.marshmallow.service;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkExecutor<T> implements Callback<T> {
    private Consumer<Response<T>> consumer;

    private final Call<T> call;

    public NetworkExecutor(Call<T> call) {
        this.call = call;
    }

    public void invoke(Consumer<Response<T>> consumer) {
        this.consumer = consumer;
        call.enqueue(this);
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (consumer != null) {
            consumer.accept(response);
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        //todo:
    }


}
