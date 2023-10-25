package com.dzaitsev.marshmallow.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.dzaitsev.marshmallow.dto.ErrorCodes;
import com.dzaitsev.marshmallow.dto.ErrorDto;
import com.dzaitsev.marshmallow.dto.authorization.SignInRequest;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class NetworkExecutorWrapper<T> {
    private final Activity activity;

    private final Call<T> call;

    public NetworkExecutorWrapper(Activity activity, Call<T> call) {
        this.activity = activity;
        this.call = call;
    }

    public void invoke(Consumer<Response<T>> consumer) {
        new NetworkExecutor<>(call).invoke(response -> {
            if (toRefreshToken(response)) {
                refreshToken(consumer);
                new NetworkExecutor<>(call.clone()).invoke(consumer);
                return;
            }
            activity.runOnUiThread(() -> consumer.accept(response));

        });
    }

    private void refreshToken(Consumer<Response<T>> consumer) {
        NetworkService.getInstance().refreshToken(null);
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("authorization-data", "");
        Gson gson = GsonExt.getGson();
        SignInRequest request = gson.fromJson(string, SignInRequest.class);
        new NetworkExecutor<>(NetworkService.getInstance().getAuthorizationApi().signIn(request))
                .invoke(responseToken -> {
                    if (responseToken.body() != null) {
                        NetworkService.getInstance().refreshToken(responseToken.body().getToken());
                        new NetworkExecutor<>(call.clone()).invoke(consumer);

                    }
                });
    }

    private boolean toRefreshToken(Response<T> response) {
        if (!response.isSuccessful() && response.code() == 403) {
            try (ResponseBody responseBody = response.errorBody()) {
                String message = Optional.ofNullable(responseBody)
                        .map(m -> {
                            try {
                                return m.string();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).orElse("Неизвестная ошибка");
                ErrorDto errorDto = GsonExt.getGson().fromJson(message, ErrorDto.class);
                if (errorDto.getErrorCode() == ErrorCodes.AUTH006) {
                    return true;
                }
            }
        }
        return false;
    }


}
