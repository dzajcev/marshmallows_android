package com.dzaitsev.marshmallow.utils;

public class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }


    public static String normalize(String str) {
        if (isEmpty(str)) return null;
        return str.trim();

    }


}
