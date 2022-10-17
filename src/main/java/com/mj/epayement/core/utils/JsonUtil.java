package com.mj.epayement.core.utils;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    /**
     * String to {@link JsonObject}
     *
     * @param data @{@link String}
     * @return {@link JsonObject}
     */
    public static JsonObject stringToJsonObject(String data) {
        if (StringUtils.isNotEmpty(data))
            return JsonParser.parseString(data).getAsJsonObject();

        return null;
    }

    /**
     *
     * @param jsonObject @{@link JsonObject}
     * @param member @{@link String}
     * @return @{@link String}
     */
    public static String getAsString(JsonObject jsonObject, String member) {
        if (jsonObject.has(member))
            return jsonObject.get(member).getAsString();
        return null;
    }

    /**
     * Json to Object
     *
     * @param data
     * @param classOfT
     * @param <T>
     * @return <T>
     */
    public static <T> T jsonObjectToObject(String data, Class<T> classOfT) {
        if (StringUtils.isNoneEmpty(data))
            return new Gson().fromJson(data, classOfT);

        return null;
    }
}
