package com.dzaitsev.marshmallow.service;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkExecutor<T> implements Callback<T> {
    private final Activity activity;
    private Consumer<Response<T>> consumer;

    private CountDownLatch countDownLatch;

    private final Call<T> call;

    private boolean success;

    public NetworkExecutor(Activity activity, Call<T> call) {
        this.activity = activity;
        this.call = call;
    }

    public NetworkExecutor(Activity activity, Call<T> call, Consumer<Response<T>> consumer) {
        this.activity = activity;
        this.consumer = consumer;
        this.call = call;
    }

    public NetworkExecutor(Activity activity, Call<T> call, boolean sync) {
        this(activity, call, null, sync);
        if (sync) {
            this.countDownLatch = new CountDownLatch(1);
        }
    }

    public NetworkExecutor(Activity activity, Call<T> call, Consumer<Response<T>> consumer,
                           boolean sync) {
        this(activity, call, consumer);
        if (sync) {
            this.countDownLatch = new CountDownLatch(1);
        }
    }

    public void invoke() {
        try {
            call.enqueue(this);
        } catch (Exception e) {
            System.out.println();
        }
        if (countDownLatch != null && countDownLatch.getCount() > 0) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (countDownLatch != null && countDownLatch.getCount() > 0) {
            execute(response);
        } else {
            activity.runOnUiThread(() -> execute(response));
        }

    }

    private void execute(Response<T> response) {
        if (response.isSuccessful()) {
            success = true;
            release();
            if (consumer != null) {
                consumer.accept(response);
            }
        } else {
            release();
            showError("Ошибка сохранения клиента");

        }
    }

    private void release() {
        if (countDownLatch != null && countDownLatch.getCount() > 0) {
            countDownLatch.countDown();
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, Throwable t) {
        showError(t.getMessage());
        release();
    }

    public boolean isSuccess() {
        return success;
    }

    private void showError(String text) {
        activity.runOnUiThread(() -> new StringUtils.ErrorDialog(activity, text).show());
    }

}
