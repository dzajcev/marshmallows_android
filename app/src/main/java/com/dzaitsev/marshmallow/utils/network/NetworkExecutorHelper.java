package com.dzaitsev.marshmallow.utils.network;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.dzaitsev.marshmallow.dto.User;
import com.dzaitsev.marshmallow.dto.UserRole;
import com.dzaitsev.marshmallow.dto.authorization.request.SignInRequest;
import com.dzaitsev.marshmallow.dto.authorization.response.JwtAuthenticationResponse;
import com.dzaitsev.marshmallow.dto.response.ResultResponse;
import com.dzaitsev.marshmallow.fragments.DeliveriesFragment;
import com.dzaitsev.marshmallow.fragments.OrdersFragment;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;

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
    private static OnAuthorizeListener onAuthorizeListener;
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
                                                            .map(ResultResponse::getData)
                                                            .ifPresent(user -> {
                                                                AuthorizationHelper.getInstance().updateUserData(user);
                                                                if (onAuthorizeListener != null) {
                                                                    onAuthorizeListener.onAuthorize(user);
                                                                }
                                                                if (user.getRole() == UserRole.DEVELOPER) {
                                                                    Navigation.getNavigation().forward(OrdersFragment.IDENTITY);
                                                                } else {
                                                                    Navigation.getNavigation().forward(DeliveriesFragment.IDENTITY);
                                                                }
                                                            });
                                                }
                                            });

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

    public static void setAuthorizeListener(OnAuthorizeListener onAuthorizeListener) {
        NetworkExecutorHelper.onAuthorizeListener = onAuthorizeListener;
    }

    public interface OnErrorListener {
        default void onError(String code, String message) {
        }
    }

    public interface OnAuthorizeListener {
        void onAuthorize(User code);
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

    public void invoke() {
        invoke(null);
    }

    public void invoke(Consumer<Response<T>> consumer) {
        activity.runOnUiThread(screenCover::show);
        new NetworkExecutor<>(call)
                .setOnResponseListener(new NetworkExecutor.OnResponseListener<T>() {
                    @Override
                    public void onResponse(@NonNull Response<T> response) {
                        if (!response.isSuccessful()) {
                            handleHttpError(response);
                            return;
                        }

                        T body = response.body();
                        if (body instanceof ResultResponse<?> result) {
                            if (!result.isSuccess()) {
                                activity.runOnUiThread(() -> {
                                    Toast.makeText(activity, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                                    screenCover.dismiss();
                                });
                                return;
                            }
                        }

                        activity.runOnUiThread(() -> {
                            if (consumer != null) {
                                consumer.accept(response);
                            }
                            screenCover.dismiss();
                        });
                    }

                    private void handleHttpError(Response<T> response) {
                        try {
                            ResultResponse<?> resultResponse = Optional.ofNullable(response.errorBody())
                                    .map(m -> {
                                        try {
                                            return m.string();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .map(m -> GsonExt.getGson().fromJson(m, ResultResponse.class))
                                    .orElse(new ResultResponse<>(null, false, "Неожиданная ошибка", "AUTH000"));

                            if ("AUTH006".equals(resultResponse.getErrorCode())) {
                                refreshToken(this);
                                return;
                            }
                            if (onErrorListener != null) {
                                onErrorListener.onError(resultResponse.getErrorCode(), resultResponse.getErrorMessage());
                            }
                            if ((response.code() == 403 || response.code() == 401) && globalErrorListener != null) {
                                globalErrorListener.onError(resultResponse.getErrorCode(), resultResponse.getErrorMessage());
                            }
                            activity.runOnUiThread(() -> {
                                String msg = resultResponse.getErrorMessage();
                                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                                if (onErrorListener != null)
                                    onErrorListener.onError(resultResponse.getErrorCode(), resultResponse.getErrorMessage());
                                screenCover.dismiss();
                            });
                        } catch (Exception e) {
                            activity.runOnUiThread(screenCover::dismiss);
                        }
                    }
                })
                .setOnFailListener(throwable -> activity.runOnUiThread(() -> {
                    screenCover.dismiss();
                    Toast.makeText(activity, "Ошибка сети: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }))
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
