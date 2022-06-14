package org.opencb.commons.api.utils;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.commons.api.models.RestEndpoint;
import org.opencb.commons.api.models.RestParameter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CommandLineUtils {

    private static final Set<String> primitiveTypes;
    private static final Map<String, String> numericTypes;

    static {
        primitiveTypes = new HashSet<>();
        numericTypes = new HashMap<>();

        primitiveTypes.add("String");
        primitiveTypes.add("string");
        primitiveTypes.add("object");
        primitiveTypes.add("Object");
        primitiveTypes.add("integer");
        primitiveTypes.add("int");
        primitiveTypes.add("boolean");
        primitiveTypes.add("Boolean");
        primitiveTypes.add("Integer");
        primitiveTypes.add("long");
        primitiveTypes.add("enum");
        primitiveTypes.add("Long");
        primitiveTypes.add("java.lang.String");
        primitiveTypes.add("java.lang.Boolean");
        primitiveTypes.add("java.lang.Integer");
        primitiveTypes.add("java.lang.Long");
        primitiveTypes.add("java.lang.Short");
        primitiveTypes.add("java.lang.Double");
        primitiveTypes.add("java.lang.Float");

        numericTypes.put("java.lang.Integer", "int");
        numericTypes.put("java.lang.Long", "long");
        numericTypes.put("java.lang.Short", "int");
        numericTypes.put("java.lang.Double", "double");
        numericTypes.put("java.lang.Float", "float");
    }

    public static boolean isPrimitiveType(String type) {
        return primitiveTypes.contains(type);
    }

    public static String getAsVariableName(String path) {
        return (Character.toLowerCase(path.charAt(0)) + path.substring(1)).replace(" ", "").replace("-", "");
    }

    public static String getSetterVariableName(String variableName) {
        variableName = variableName.replace("body_", "");
        return "set" + (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));

    }

    public static boolean isNumericType(String simpleName) {
        return numericTypes.keySet().contains(simpleName);
    }

    public static void invokeSetter(Object obj, String propertyName, Object variableValue) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(propertyName, obj.getClass());
            Method setter = pd.getWriteMethod();
            try {
                setter.invoke(obj, variableValue);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    public static void invokeGetter(Object obj, String variableName) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(variableName, obj.getClass());
            Method getter = pd.getReadMethod();
            Object f = getter.invoke(obj);
            System.out.println(f);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                 IntrospectionException e) {
            e.printStackTrace();
        }
    }


    public static List<RestParameter> getAllParameters(RestEndpoint restEndpoint) {
        List<RestParameter> res = new ArrayList<>();
        for (RestParameter restParameter : restEndpoint.getParameters()) {
            res = getListParameters(restParameter, res);
        }
        return res;
    }

    public static List<RestParameter> getListParameters(RestParameter restParameter, List<RestParameter> res) {
        if (restParameter.getData() != null) {
            for (RestParameter dataParameter : restParameter.getData()) {
                res.add(dataParameter);
                if (dataParameter.getData() != null) {
                    getListParameters(dataParameter, res);
                }
            }
        }
        return res;
    }


    public static ObjectMapper generateDefaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        // objectMapper.addMixIn(GenericRecord.class, GenericRecordAvroJsonMixin.class);
        return objectMapper;
    }
}
