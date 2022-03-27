package org.opencb.commons.utils;

import org.apache.commons.lang3.StringUtils;
import org.opencb.commons.datastore.core.QueryParam;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class DataModelsUtils {

    public static Map<String, Type> dataModelToMap(Class<?> clazz) {
        return dataModelToMap(clazz, "");
    }

    public static Map<String, Type> dataModelToMap(Class<?> clazz, String field) {
        return dataModelToMap(clazz, field, new HashMap<String, Type>());
    }

    public static Map<String, Type> dataModelToMap(Class<?> clazz, String field, Map<String, Type> map) {
        List<Field> declaredFields = getAllUnderlyingDeclaredFields(clazz);
        for (Field declaredField : declaredFields) {
            // Ignore avro data models
            if (declaredField.getType().getName().equals("org.apache.avro.Schema")) {
                continue;
            }

            String key = getMapKey(field, declaredField.getName());
            if (declaredField.getType().getName().startsWith("org.opencb") && !declaredField.getType().isEnum()
                    && !declaredField.getType().getName().endsWith("ObjectMap")) {
                dataModelToMap(declaredField.getType(), key, map);
            } else if (declaredField.getType().getName().endsWith("List")) {
                String subclassStr = declaredField.getAnnotatedType().getType().getTypeName();
                subclassStr = subclassStr.substring(subclassStr.indexOf("<") + 1, subclassStr.length() - 1);
                Class subclass;
                try {
                    subclass = Class.forName(subclassStr);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Could not obtain class for " + subclassStr);
                }
                if (subclass.getName().startsWith("org.opencb")) {
                    Map<String, Type> subMap = new HashMap<>();
                    map.put(key, new Type(true, subMap));
                    dataModelToMap(subclass, "", subMap);
                } else {
                    QueryParam.Type type = getType(declaredField.getType(), subclass);
                    map.put(key, new Type(type));

                }
            } else {
                QueryParam.Type type = getType(declaredField.getType());
                map.put(key, new Type(type));
            }
        }
        return map;
    }

    private static QueryParam.Type getType(Class<?> clazz) {
        return getType(clazz, null);
    }

    private static QueryParam.Type getType(Class<?> clazz, Class<?> subclazz) {
        boolean isList = false;
        if (clazz.getName().equals("java.util.List")) {
            clazz = subclazz;
            isList = true;
        }

        switch (clazz.getName()) {
            case "java.lang.String":
                return isList ? QueryParam.Type.TEXT_ARRAY : QueryParam.Type.TEXT;
            case "java.lang.Integer":
            case "int":
                return isList ? QueryParam.Type.INTEGER_ARRAY : QueryParam.Type.INTEGER;
            case "java.lang.Double":
            case "double":
            case "java.lang.Float":
            case "float":
            case "java.lang.Long":
            case "long":
                return isList ? QueryParam.Type.LONG_ARRAY : QueryParam.Type.LONG;
            case "java.lang.Boolean":
            case "boolean":
                return isList ? QueryParam.Type.BOOLEAN_ARRAY : QueryParam.Type.BOOLEAN;
            case "java.util.Map":
            case "org.opencb.commons.datastore.core.ObjectMap":
                return QueryParam.Type.OBJECT;
            default:
                if (clazz.isEnum()) {
                    return QueryParam.Type.TEXT;
                }
                throw new IllegalArgumentException("Unsupported type '" + clazz.getName() + "'");
        }
    }

    private static String getMapKey(String prefix, String field) {
        return StringUtils.isEmpty(prefix) ? field : prefix + "." + field;
    }

    // Scans fields in all super classes
    private static List<Field> getAllUnderlyingDeclaredFields(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<Field> fields = new LinkedList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        fields.addAll(getAllUnderlyingDeclaredFields(clazz.getSuperclass()));
        return fields;
    }

    public static String dataModelToJsonString(Class<?> clazz) {
        return getClassAsJSON(clazz, 0);
    }

    private static String getClassAsJSON(Class<?> clazz, int margin) {

        List<Field> declaredFields = getAllUnderlyingDeclaredFields(clazz);
        try {
          /*  List<Field> declaredFields = new LinkedList<>();
            declaredFields.addAll(Arrays.asList(clazz.getDeclaredFields()));*/
            String res = "{\n";
            for (Field declaredField : declaredFields) {
                res += getTypeAsJSON(declaredField, margin);
            }
            res += (addMargin(margin) + "}");
            return res;
        } catch (Exception e) {
            return "";
        }

    }

    private static String getTypeAsJSON(Field field, int margin) throws ClassNotFoundException {
        int modifiers = field.getModifiers();
        if ((Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers))
                && !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
            String value = "";
            switch (field.getType().getName()) {
                case "boolean":
                case "java.lang.Boolean":
                    value = "false";
                    break;
                case "String":
                case "java.lang.String":
                    value = "\"\"";
                    break;
                case "long":
                case "int":
                case "double":
                case "java.lang.Integer":
                case "java.lang.Double":
                case "java.lang.Long":
                    value = "0";
                    break;
                case "java.util.List":
                    value = "[]";
                    break;
                case "java.util.Date":
                    value = "\"dd/mm/yyyy\"";
                    break;
                case "java.util.Map":
                    value = "[\"key\",\"value\"]";
                    break;
                default:

                    value += getClassAsJSON(Class.forName(field.getType().getName()), margin + 4);

                    break;
            }

            return addMargin(margin + 4) + "\"" + field.getName() + "\" : " + value + ",\n";
        }
        return "";
    }

    private static String addMargin(int margin) {
        String res = "";
        for (int i = 0; i < margin; i++) {
            res += " ";
        }
        return res;
    }


    public static String dataModelToJsonSchema(Class<?> clazz) {
        return null;
    }


    private static class Type {
        private QueryParam.Type type;
        private boolean file;
        private Map<String, Type> typeMap;

        Type() {
        }

        Type(QueryParam.Type type) {
            this.type = type;
        }

        Type(boolean file, Map<String, Type> typeMap) {
            this.file = file;
            this.typeMap = typeMap;
        }

        public QueryParam.Type getType() {
            return type;
        }

        public Type setType(QueryParam.Type type) {
            this.type = type;
            return this;
        }

        public boolean isFile() {
            return file;
        }

        public Type setFile(boolean file) {
            this.file = file;
            return this;
        }

        public Map<String, Type> getTypeMap() {
            return typeMap;
        }

        public Type setTypeMap(Map<String, Type> typeMap) {
            this.typeMap = typeMap;
            return this;
        }
    }

}