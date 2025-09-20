package com.dzaitsev.marshmallow.utils.authorization;

import android.content.SharedPreferences;

import com.dzaitsev.marshmallow.dto.User;
import com.dzaitsev.marshmallow.dto.UserRole;
import com.dzaitsev.marshmallow.dto.authorization.request.SignInRequest;
import com.dzaitsev.marshmallow.utils.GsonExt;

import java.util.Optional;

public class AuthorizationHelper {
    private final static String authorizationData = "authorization-data";
    private final static String userData = "user-data";

    private static AuthorizationHelper authorizationHelper;
    private SharedPreferences preferences;

    public static AuthorizationHelper getInstance() {
        if (authorizationHelper == null) {
            authorizationHelper = new AuthorizationHelper();
        }
        return authorizationHelper;
    }

    protected void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public Optional<SignInRequest> getSignInRequest() {
        return Optional.ofNullable(GsonExt.getGson()
                .fromJson(preferences.getString(authorizationData, ""), SignInRequest.class));
    }

    public void updateSignInRequest(SignInRequest request) {
        preferences.edit()
                .putString(authorizationData, GsonExt.getGson().toJson(request))
                .apply();
    }

    public Optional<User> getUserData() {
        return Optional.ofNullable(GsonExt.getGson()
                .fromJson(preferences.getString(userData, ""), User.class));
    }

    public UserRole getUserRole() {
        return getUserData().map(User::getRole).orElse(UserRole.DELIVERYMAN);
    }

    public void updateUserData(User user) {
        preferences.edit()
                .putString(userData, GsonExt.getGson().toJson(user))
                .apply();
    }
}
