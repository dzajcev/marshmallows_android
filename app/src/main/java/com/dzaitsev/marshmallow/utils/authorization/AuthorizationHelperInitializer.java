package com.dzaitsev.marshmallow.utils.authorization;

import android.content.SharedPreferences;

public class AuthorizationHelperInitializer {

    public static void init(SharedPreferences preferences) {
        AuthorizationHelper instance = AuthorizationHelper.getInstance();
        instance.setPreferences(preferences);
    }
}
