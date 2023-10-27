package com.dzaitsev.marshmallow.utils;

public class GsonHelper {

    public static <T> String serialize(T object) {
        return GsonExt.getGson().toJson(object);
    }

    public static <T> T deserialize(String json, Class<T> targetClass) {
        return GsonExt.getGson().fromJson(json, targetClass);
    }
}
