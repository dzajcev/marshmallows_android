package com.dzaitsev.marshmallow.service;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.dzaitsev.marshmallow.dto.ErrorCodes;
import com.dzaitsev.marshmallow.dto.ErrorDto;
import com.dzaitsev.marshmallow.dto.request.SignInRequest;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Response;

public class NetworkExecutorWrapper<T> {
    private final FragmentActivity activity;

    private final Call<T> call;

    private final ScreenCover screenCover;

    private OnErrorListener onErrorListener;

    private static OnErrorListener globalErrorListener;

    public NetworkExecutorWrapper(FragmentActivity activity, Call<T> call) {
        this.activity = activity;
        this.call = call;
        screenCover = new ScreenCover(activity);
    }

    public NetworkExecutorWrapper<T> setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    public static void setGlobalErrorListener(OnErrorListener globalErrorListener) {
        NetworkExecutorWrapper.globalErrorListener = globalErrorListener;
    }

    public interface OnErrorListener {
        void onError(ErrorCodes code);
    }

    public static class ScreenCover extends AlertDialog {

        protected ScreenCover(Context context) {
            super(context);
            setCancelable(false);
            setView(new ProgressBar(context));
            Optional.ofNullable(getWindow())
                    .ifPresent(window -> window.setBackgroundDrawableResource(android.R.color.transparent));

        }
    }

    public void invoke(Consumer<Response<T>> consumer) {
        screenCover.show();
        new NetworkExecutor<>(call)
                .setOnResponseListener(new NetworkExecutor.OnResponseListener<>() {
                    @Override
                    public void onResponse(@NonNull Response<T> response) {
                        if (!response.isSuccessful()) {
                            try {
                                ErrorCodes errorCode = Optional.ofNullable(response.errorBody())
                                        .map(m -> {
                                            try {
                                                return m.string();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })
                                        .map(m -> GsonExt.getGson().fromJson(m, ErrorDto.class))
                                        .map(ErrorDto::getCode)
                                        .orElse(ErrorCodes.AUTH000);
                                if (errorCode == ErrorCodes.AUTH006) {
                                    refreshToken(this);
                                    return;
                                }
                                if (onErrorListener != null) {
                                    onErrorListener.onError(errorCode);

                                }
                                if (globalErrorListener != null) {
                                    globalErrorListener.onError(errorCode);
                                }
                                return;
                            } finally {
                                screenCover.dismiss();
                            }
                        }

                        activity.runOnUiThread(() -> {
                            consumer.accept(response);
                            screenCover.dismiss();
                        });
                    }
                })
                .setOnFailListener(throwable -> {
                    screenCover.dismiss();
                    Toast.makeText(activity, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .invoke();
    }

    private void refreshToken(NetworkExecutor.OnResponseListener<T> onResponseListener) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("authorization-data", "");
        Gson gson = GsonExt.getGson();
        SignInRequest request = gson.fromJson(string, SignInRequest.class);
        new NetworkExecutor<>(NetworkService.getInstance().getAuthorizationApi().signIn(request))
                .setOnResponseListener(response -> {
                    if (response.body() != null) {
                        NetworkService.getInstance().refreshToken(response.body().getToken());
                        new NetworkExecutor<>(call.clone())
                                .setOnResponseListener(onResponseListener)
                                .invoke();

                    }
                }).invoke();
    }

}
