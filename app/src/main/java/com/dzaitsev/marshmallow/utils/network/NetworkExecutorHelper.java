package com.dzaitsev.marshmallow.utils.network;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.dzaitsev.marshmallow.dto.ErrorCodes;
import com.dzaitsev.marshmallow.dto.ErrorDto;
import com.dzaitsev.marshmallow.dto.User;
import com.dzaitsev.marshmallow.dto.UserRole;
import com.dzaitsev.marshmallow.dto.authorization.request.SignInRequest;
import com.dzaitsev.marshmallow.dto.authorization.response.JwtAuthenticationResponse;
import com.dzaitsev.marshmallow.dto.response.UserInfoResponse;
import com.dzaitsev.marshmallow.fragments.DeliveriesFragment;
import com.dzaitsev.marshmallow.fragments.OrdersFragment;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.dzaitsev.marshmallow.utils.StringUtils;
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
                                                            .map(UserInfoResponse::getUser)
                                                            .ifPresent(user -> {
                                                                AuthorizationHelper.getInstance().updateUserData(user);
                                                                if (onAuthorizeListener != null) {
                                                                    onAuthorizeListener.onAuthorize(user);
                                                                }
                                                                if (user.getRole() == UserRole.DEVELOPER) {
                                                                    Navigation.getNavigation().goForward(new OrdersFragment());
                                                                } else {
                                                                    Navigation.getNavigation().goForward(new DeliveriesFragment());
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
        default void onError(ErrorCodes code, String text) {
            onError(new ErrorDto(code, text));
        }

        default void onError(ErrorDto errorDto) {
            onError(errorDto.getCode(), errorDto.getMessage());
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
                .setOnResponseListener(new NetworkExecutor.OnResponseListener<>() {
                    @Override
                    public void onResponse(@NonNull Response<T> response) {
                        if (!response.isSuccessful()) {
                            try {
                                ErrorDto errorDto = Optional.ofNullable(response.errorBody())
                                        .map(m -> {
                                            try {
                                                String string = m.string();
                                                Log.e("network error", string);
                                                return string;
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })
                                        .map(m -> GsonExt.getGson().fromJson(m, ErrorDto.class))
                                        .orElse(new ErrorDto(ErrorCodes.AUTH000));
                                if (errorDto.getCode() == ErrorCodes.AUTH006) {
                                    refreshToken(this);
                                    return;
                                }
                                if (onErrorListener != null) {
                                    onErrorListener.onError(errorDto);
                                }
                                if (response.code() == 403 && globalErrorListener != null) {
                                    globalErrorListener.onError(errorDto);
                                }
                                return;
                            } finally {
                                screenCover.dismiss();
                            }
                        } else {
                            if (!StringUtils.isEmpty(response.message())) {
                                Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        activity.runOnUiThread(() -> {
                            if (consumer != null) {
                                consumer.accept(response);
                            }
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
