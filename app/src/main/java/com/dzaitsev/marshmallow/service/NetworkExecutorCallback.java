package com.dzaitsev.marshmallow.service;

import android.app.Activity;

import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkExecutorCallback<T> implements Callback<T> {
    private final Activity activity;
    private final Consumer<Response<T>> consumer;

    private CountDownLatch countDownLatch;

    private boolean success;


    public NetworkExecutorCallback(Activity activity, Consumer<Response<T>> consumer) {
        this.activity = activity;
        this.consumer = consumer;
    }

    public NetworkExecutorCallback(Activity activity, Consumer<Response<T>> consumer,
                                   CountDownLatch countDownLatch) {
        this.activity = activity;
        this.consumer = consumer;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (countDownLatch != null) {
            execute(response);
        } else {
            activity.runOnUiThread(() -> execute(response));
        }

    }

    private void execute(Response<T> response) {
        if (response.isSuccessful()) {
            consumer.accept(response);
            success = true;
        } else {
            showError("Ошибка получения списка клиентов");
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        showError(t.getMessage());
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public boolean isSuccess() {
        return success;
    }

    private void showError(String text) {
        activity.runOnUiThread(() -> new StringUtils.ErrorDialog(activity, text).show());
    }

}
