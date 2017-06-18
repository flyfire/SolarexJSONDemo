package com.solarexsoft.solarexjson;

import java.util.Map;

/**
 * <pre>
 *    Author: houruhou
 *    Project: https://solarex.github.io/projects
 *    CreatAt: 18/06/2017
 *    Desc:
 * </pre>
 */

public class SolarexJSON {
    private static final String ENTER_REPLACE_STRING = "\\n";

    public static Object parseObject(String json, Class<?> clazz) {
        return parseObject(json, clazz, null, null);
    }

    public static Object parseObject(String json, Map<String, String> map) {
        return parseObject(json, null, null, map);
    }

    public static Object parseObject(String json, String basePackage) {
        return parseObject(json, null, basePackage, null);
    }

    public static Object parseObject(String json, Class<?> clazz, String basePackage) {
        return parseObject(json, clazz, basePackage, null);
    }

    private static Object parseObject(String json, Class<?> clazz, String basePackage,
                                      Map<String, String> map) {
        if (json == null || json.equals("")) {
            return json;
        } else {
            json = json.replace("\r\n", ENTER_REPLACE_STRING).replace("\n", ENTER_REPLACE_STRING);
        }
        Object object = null;

        return object;
    }


}
