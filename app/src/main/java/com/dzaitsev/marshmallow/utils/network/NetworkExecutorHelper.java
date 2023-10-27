package com.dzaitsev.marshmallow.utils.network;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.dto.ErrorCodes;
import com.dzaitsev.marshmallow.dto.ErrorDto;
import com.dzaitsev.marshmallow.dto.authorization.request.SignInRequest;
import com.dzaitsev.marshmallow.dto.authorization.response.JwtAuthenticationResponse;
import com.dzaitsev.marshmallow.dto.response.UserInfoResponse;
import com.dzaitsev.marshmallow.fragments.OrdersFragment;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Response;

public class NetworkExecutorHelper<T> {
    private final FragmentActivity activity;

    private final Call<T> call;

    private final ScreenCover screenCover;

    private OnErrorListener onErrorListener;

    private static OnErrorListener globalErrorListener;

    public NetworkExecutorHelper(FragmentActivity activity, Call<T> call) {
        this.activity = activity;
        this.call = call;
        screenCover = new ScreenCover(activity);
    }
    public static void authorize(FragmentActivity activity, SignInRequest signInRequest) {
        AuthorizationHelper.getInstance().updateSignInRequest(signInRequest);
        new NetworkExecutorHelper<>(activity, NetworkService.getInstance().getAuthorizationApi().signIn(signInRequest))
                .invoke(response -> {
                    if (response.isSuccessful()) {
                        Optional.ofNullable(response.body())
                                .map(JwtAuthenticationResponse::getToken)
                                .ifPresent(s -> {
                                    NetworkService.getInstance().refreshToken(s);
                                    new NetworkExecutorHelper<>(activity, NetworkService.getInstance().getUsersApi().getMyInfo())
                                            .invoke(r -> {
                                                if (r.isSuccessful()) {
                                                    Optional.ofNullable(r.body())
                                                            .map(UserInfoResponse::getUser)
                                                            .ifPresent(user -> AuthorizationHelper.getInstance().updateUserData(user));
                                                }

                                            });
                                    Navigation.getNavigation().goForward(new OrdersFragment());
                                });
                    }
                });
    }

    public NetworkExecutorHelper<T> setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    public static void setGlobalErrorListener(OnErrorListener globalErrorListener) {
        NetworkExecutorHelper.globalErrorListener = globalErrorListener;
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
        AuthorizationHelper.getInstance().getSignInRequest().ifPresent(signInRequest
                -> new NetworkExecutor<>(NetworkService.getInstance().getAuthorizationApi().signIn(signInRequest))
                .setOnResponseListener(response -> {
                    if (response.body() != null) {
                        NetworkService.getInstance().refreshToken(response.body().getToken());
                        new NetworkExecutor<>(call.clone())
                                .setOnResponseListener(onResponseListener)
                                .invoke();

                    }
                }).invoke());

    }

}
