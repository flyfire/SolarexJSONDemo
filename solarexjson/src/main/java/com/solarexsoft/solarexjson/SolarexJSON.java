package com.solarexsoft.solarexjson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    private enum JSON_TYPE {
        JSON_TYPE_OBJECT,
        JSON_TYPE_ARRAY,
        JSON_TYPE_ERROR
    }

    public static Object fromJSON(String json, Class<?> clazz) {
        return fromJSON(json, clazz, null, null);
    }

    public static Object fromJSON(String json, Map<String, String> map) {
        return fromJSON(json, null, null, map);
    }

    public static Object fromJSON(String json, String basePackage) {
        return fromJSON(json, null, basePackage, null);
    }

    public static Object fromJSON(String json, Class<?> clazz, String basePackage) {
        return fromJSON(json, clazz, basePackage, null);
    }

    private static Object fromJSON(String json, Class<?> clazz, String basePackage,
                                   Map<String, String> map) {
        if (json == null || json.equals("")) {
            return json;
        } else {
            json = json.replace("\r\n", ENTER_REPLACE_STRING).replace("\n", ENTER_REPLACE_STRING);
        }
        Object object = null;
        try {
            if (json.charAt(0) == '[') {
                object = toList(json, clazz, basePackage, map);
            } else if (json.charAt(0) == '{') {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.has("class")) {
                    String className = jsonObject.getString("class");
                    if (className != null) {
                        if (map != null && map.get(className) != null) {
                            clazz = Class.forName(map.get(className));
                        } else if (basePackage != null) {
                            clazz = Class.forName(basePackage + className);
                        }
                    }
                }

                if (clazz == null) {
                    throw new NullPointerException("clazz,basePackage,map cant be all empty.");
                }
                object = clazz.newInstance();

                Iterator<String> iterator = jsonObject.keys();
                List<Field> fields = getAllFields(null, clazz);
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    Object fieldValue = null;
                    for (int i = 0; i < fields.size(); i++) {
                        Field field = fields.get(i);
                        if (field.getName().equalsIgnoreCase(key)) {
                            field.setAccessible(true);
                            fieldValue = getFieldValue(field, jsonObject, key, basePackage, map);
                            if (fieldValue != null) {
                                field.set(object, fieldValue);
                            }
                            field.setAccessible(false);
                        }
                    }
                }
            } else {
                return json;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            for (int i = 0; i < list.size(); i++) {
                replaceObjectBr(list.get(i));
            }
        } else {
            replaceObjectBr(object);
        }
        return object;
    }

    private static List<?> toList(String json, Class<?> clazz, String basePackage, Map<String,
            String> map) {
        List<Object> list = null;
        try {
            JSONArray jsonArray = (JSONArray) new JSONArray(json);
            list = new ArrayList<Object>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonValue = jsonArray.getJSONObject(i).toString();
                switch (getJSONType(jsonValue)) {
                    case JSON_TYPE_OBJECT:
                        list.add(fromJSON(jsonValue, clazz, basePackage, map));
                        break;
                    case JSON_TYPE_ARRAY:
                        List<?> innerList = toList(jsonValue, clazz, basePackage, map);
                        list.add(innerList);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static Object getFieldValue(Field field, JSONObject jsonObject, String key, String
            basePackage, Map<String, String> map) throws Exception {
        Object fieldValue = null;
        Class<?> fieldClass = field.getType();
        String fieldClassName = fieldClass.getSimpleName().toString();
        if (fieldClassName.equals("int") || fieldClassName.equals("Integer")) {
            fieldValue = jsonObject.getInt(key);
        } else if (fieldClassName.equals("long") || fieldClassName.equals("Long")) {
            fieldValue = jsonObject.getLong(key);
        } else if (fieldClassName.equals("double") || fieldClassName.equals("Double")) {
            fieldValue = jsonObject.getDouble(key);
        } else if (fieldClassName.equals("boolean") || fieldClassName.equals("Boolean")) {
            fieldValue = jsonObject.getBoolean(key);
        } else if (fieldClassName.equals("String")) {
            fieldValue = jsonObject.getString(key);
        } else {
            String jsonValue = jsonObject.getString(key);
            switch (getJSONType(jsonValue)) {
                case JSON_TYPE_OBJECT:
                    fieldValue = fromJSON(jsonValue, fieldClass, basePackage, map);
                    break;
                case JSON_TYPE_ARRAY:
                    Type genericFieldType = field.getGenericType();
                    if (genericFieldType instanceof ParameterizedType) {
                        ParameterizedType aType = (ParameterizedType) genericFieldType;
                        Type[] fieldArgTypes = aType.getActualTypeArguments();

                        //HashMap<Author,Reader> 会得到多个Type，不支持
                        //只支持List<User>这种类型，实际上还是取Type[0]
                        for (Type fieldArgType : fieldArgTypes) {
                            Class<?> fieldArgClass = (Class<?>) fieldArgType;
                            if (map != null && map.get(key) != null) {
                                fieldArgClass = Class.forName(map.get(key));
                            }
                            fieldValue = toList(jsonValue, fieldArgClass, basePackage, map);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return fieldValue;
    }

    private static List<Field> getAllFields(List<Field> fields, Class clazz) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        try {
            if (clazz.getSuperclass() != null) {
                Field[] fieldsSelf = clazz.getDeclaredFields();
                for (Field field : fieldsSelf) {
                    if (!Modifier.isFinal(field.getModifiers())) {
                        fields.add(field);
                    }
                }
                getAllFields(fields, clazz.getSuperclass());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fields;
    }

    private static JSON_TYPE getJSONType(String str) {
        final char firstChar = str.charAt(0);
        if (firstChar == '[') {
            return JSON_TYPE.JSON_TYPE_ARRAY;
        } else if (firstChar == '{') {
            return JSON_TYPE.JSON_TYPE_OBJECT;
        } else {
            return JSON_TYPE.JSON_TYPE_ERROR;
        }
    }

    private static void replaceObjectBr(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object fieldValue = null;
            try {
                fieldValue = field.get(obj);
                if (fieldValue instanceof Integer
                        || fieldValue instanceof Long
                        || fieldValue instanceof Double
                        || fieldValue instanceof Boolean) {
                } else if (fieldValue instanceof String) {
                    if (fieldValue != null) {
                        String str = (String) fieldValue;
                        field.set(obj, str.replaceAll(ENTER_REPLACE_STRING, "\n"));
                    }
                } else if (fieldValue instanceof List<?>) {
                    List<?> list = (List<?>) fieldValue;
                    for (Object o : list) {
                        replaceObjectBr(o);
                    }
                } else {
                    if (fieldValue != null) {
                        replaceObjectBr(fieldValue);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(false);
            }
        }
    }


    public static String toJSON(Object obj) {
        StringBuffer sb = new StringBuffer();
        if (obj instanceof List<?>) {
            sb.append('[');
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                addObjectToString(sb, list.get(i));
                if (i < list.size() - 1) {
                    sb.append(',');
                }
            }
            sb.append(']');
        } else {
            addObjectToString(sb, obj);
        }
        return sb.toString();
    }

    private static void addObjectToString(StringBuffer sb, Object o) {
        sb.append('{');
        ArrayList<Field> fields = new ArrayList<>();
        getAllFields(fields, o.getClass());
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            String fieldName = field.getName();
            Method getMethod = null;
            Object fieldValue = null;
            try {
                String methodName = "get" + ((char) (fieldName.charAt(0) - 32)) + fieldName
                        .substring(1);
                getMethod = o.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                String methodName = "is" + ((char) (fieldName.charAt(0) - 32)) + fieldName
                        .substring(1);

                try {
                    getMethod = o.getClass().getMethod(methodName);
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                }
            }
            if (getMethod != null) {
                try {
                    fieldValue = getMethod.invoke(o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            if (fieldValue != null) {
                sb.append("\"").append(fieldName).append("\":");
                //String type = field.getType().getSimpleName();
                //type.equals("int" "Integer" "long" "Long" ...
                //fieldValue instanceof Integer/Long/Double/Boolean
                if (fieldValue instanceof Integer
                        || fieldValue instanceof Long
                        || fieldValue instanceof Double
                        || fieldValue instanceof Boolean) {
                    sb.append(fieldValue.toString());
                } else if (fieldValue instanceof String) {
                    sb.append("\"").append(fieldValue.toString()).append("\"");
                } else if (fieldValue instanceof List<?>) {
                    sb.append('[');
                    List<?> list = (List<?>) fieldValue;
                    for (int j = 0; j < list.size(); j++) {
                        addObjectToString(sb, list.get(j));
                        if (j < list.size() - 1) {
                            sb.append(',');
                        }
                    }
                    sb.append(']');
                } else {
                    //normal class like User
                    addObjectToString(sb, fieldValue);
                }
                sb.append(",");
            }
            if (i == fields.size()-1 && sb.charAt(sb.length()-1) == ','){
                sb.deleteCharAt(sb.length()-1);
            }
        }
        sb.append('}');
    }
}
