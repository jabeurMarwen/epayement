package com.mj.epayement.core.utils;

import java.util.List;

import org.springframework.util.LinkedMultiValueMap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Marwen JABEUR
 * date: 20/07/2021
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapUtils {
    /**
     *
     * @param properties @{@link List}
     * @param values @{@link List}
     * @return @{@link LinkedMultiValueMap}
     */
    public static LinkedMultiValueMap<String, String> generateLinkedMultiValueMap(List<String> properties, List<String> values) {
        var map = new LinkedMultiValueMap<String, String>();
        for (String parameter : properties) {
            map.add(parameter, values.get(properties.indexOf(parameter)));
        }
        return map;
    }
}
