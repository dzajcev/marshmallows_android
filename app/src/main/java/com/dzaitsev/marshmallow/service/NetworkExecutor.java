package com.dzaitsev.marshmallow.service;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.StringUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkExecutor<T> implements Callback<T> {
    private Activity activity;
    private Consumer<Response<T>> consumer;

    private CountDownLatch countDownLatch;

    private final Call<T> call;

    public NetworkExecutor(Call<T> call) {
        this.call = call;
    }

    public NetworkExecutor(Activity activity, Call<T> call) {
        this.activity = activity;
        this.call = call;
    }

    public void invoke(Consumer<Response<T>> consumer) {
        try {
            this.consumer = consumer;
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

    private class ResponseHolder {
        Response<T> response;

        public Response<T> getResponse() {
            return response;
        }

        public void setResponse(Response<T> response) {
            this.response = response;
        }
    }

    public Response<T> invokeSync() {
        if (this.countDownLatch == null) {
            this.countDownLatch = new CountDownLatch(1);
        }
        final ResponseHolder responseHolder = new ResponseHolder();
        if (consumer == null) {
            consumer = responseHolder::setResponse;
        }
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
        Log.e("blabla", String.format("is null?? %s", responseHolder.getResponse() == null));
        return responseHolder.getResponse();
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (countDownLatch != null && countDownLatch.getCount() > 0) {
            execute(response);
        } else {
            if (activity == null) {
                throw new RuntimeException("activity is null");
            }
            activity.runOnUiThread(() -> execute(response));
        }

    }

    private void execute(Response<T> response) {
        if (!response.isSuccessful()) {
            try (ResponseBody responseBody = response.errorBody()) {
                showError(Optional.ofNullable(responseBody)
                        .map(m -> {
                            try {
                                return m.string();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).orElse(""));
            }
        }
        if (consumer != null) {
            consumer.accept(response);
        }
        release();
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

    private void showError(String text) {
        if (activity == null) {
            throw new RuntimeException("activity is null");
        }
        activity.runOnUiThread(() -> new StringUtils.ErrorDialog(activity, text).show());
    }

}
