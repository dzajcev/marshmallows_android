package com.dzaitsev.marshmallow.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonExt {
    private static final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static Gson instance;
    

    public static Gson getGson() {
        if (instance==null){
            instance=new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class,
                            (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context)
                                    -> new JsonPrimitive(localDateTimeFormatter.format(src)))
                    .registerTypeAdapter(LocalDate.class,
                            (JsonSerializer<LocalDate>) (src, typeOfSrc, context)
                                    -> new JsonPrimitive(localDateFormatter.format(src)))
                    .registerTypeAdapter(LocalDateTime.class,
                            (JsonDeserializer<LocalDateTime>) (json, type,
                                                               jsonDeserializationContext) ->
                                    LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), localDateTimeFormatter))
                    .registerTypeAdapter(LocalDate.class,
                            (JsonDeserializer<LocalDate>) (json, type,
                                                           jsonDeserializationContext) ->
                                    LocalDate.parse(json.getAsJsonPrimitive().getAsString(), localDateFormatter))
                    .create();
        }
        return instance;
    }
}
