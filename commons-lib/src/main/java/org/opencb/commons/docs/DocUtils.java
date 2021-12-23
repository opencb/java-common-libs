package org.opencb.commons.docs;

import org.opencb.commons.docs.doc.markdown.MarkdownDoc;
import org.opencb.commons.docs.models.DataFieldDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class DocUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownDoc.class);

    public static boolean isClassCollection(final Class<?> c) {
        return Collection.class.isAssignableFrom(c)
                || Map.class.isAssignableFrom(c);
    }

    public static boolean isMap(final Class<?> c) {
        return Map.class.isAssignableFrom(c);
    }

    public static boolean isSimpleCollectionType(final List<Class<?>> types) {
        boolean res = false;

        for (Class c : types) {
            res = res || isSimpleType(c);
        }

        return res;
    }

    public static boolean isSimpleType(final Class<?> type) {
        boolean simpleType =
                type.isAssignableFrom(Short.TYPE)
                        || type.isAssignableFrom(Integer.TYPE)
                        || type.isAssignableFrom(Float.TYPE)
                        || type.isAssignableFrom(Double.TYPE)
                        || type.isAssignableFrom(Long.TYPE)
                        || type.isAssignableFrom(Byte.TYPE)
                        || type.isAssignableFrom(Character.TYPE)
                        || type.isAssignableFrom(Boolean.TYPE)
                        || type.isAssignableFrom(String.class);
        return simpleType;
    }

    public static Class getCollectionGenericType(final Field field) {
        if (field.getType().isArray()) {
            return ((Class) field.getGenericType()).getComponentType();
        }
        return (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    public static List<Class<?>> getMapGenericType(final Field field) {
        final Type genericType = field.getGenericType();
        List<Class<?>> res = new ArrayList<>();
        if (genericType instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            res = getMapInnerClasses(types);
        }
        LOGGER.warn(field.getName() + " -> ");
        for (Class<?> c : res) {
            LOGGER.warn(c.getName() + "");
        }
        return res;
    }

    private static List<Class<?>> getMapInnerClasses(Type[] types) {
        List<Class<?>> res = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            if (!(types[i] instanceof ParameterizedType)) {
                res.add((Class<?>) types[i]);
            } else {
                res.add(Map.class);
                res.addAll(getMapInnerClasses(((ParameterizedType) types[i]).getActualTypeArguments()));
            }
        }
        return res;
    }


    public static boolean isEnum(DataFieldDoc df) {
        if (df.isPrimitive()) {
            return false;
        }
        if (df.isCollection()) {
            final List<Class<?>> genericClasses = df.getGenericClasses();
            boolean res = false;
            for (Class c : genericClasses) {
                res = res || c.isEnum();
            }
            return res;
        } else {
            return df.getClazz().isEnum();
        }
    }


    public static boolean isUncommentedClass(DataFieldDoc field, Class<?> clazz) {

        if (field.getUncommentedClasses() == null || field.getUncommentedClasses().length == 0) {
            return false;
        }
        for (String cls : field.getUncommentedClasses()) {
            if (cls.equals(clazz.getSimpleName())) {
                return true;
            }
        }
        return false;
    }
}
