package com.dzaitsev.marshmallow.service;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkExecutor<T> implements Callback<T> {
    private OnResponseListener<T> onResponseListener;
    private OnFailListener onFailListener;

    private final Call<T> call;

    public NetworkExecutor(Call<T> call) {
        this.call = call;
    }

    public NetworkExecutor<T> setOnResponseListener(OnResponseListener<T> onResponseListener) {
        this.onResponseListener = onResponseListener;
        return this;
    }

    public NetworkExecutor<T> setOnFailListener(OnFailListener onFailListener) {
        this.onFailListener = onFailListener;
        return this;
    }

    public void invoke() {
        call.enqueue(this);
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        onResponseListener.onResponse(response);
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (onFailListener != null) {
            onFailListener.onFail(t);
        }
    }

    public interface OnResponseListener<T> {
        void onResponse(@NonNull Response<T> response);
    }

    public interface OnFailListener {
        void onFail(@NonNull Throwable throwable);
    }
}
