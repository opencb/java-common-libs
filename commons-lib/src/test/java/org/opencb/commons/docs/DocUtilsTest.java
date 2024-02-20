package org.opencb.commons.docs;

import org.junit.jupiter.api.Test;
import org.opencb.commons.docs.doc.markdown.MarkdownDoc;
import org.opencb.commons.docs.models.DataClassDoc;
import org.opencb.commons.docs.models.DataFieldDoc;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DocUtilsTest {


    @Test
    public void test() throws Exception {
        Field[] fields = DataClassDoc.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (DocUtils.isClassCollection(fields[i].getType())) {
                System.out.println(fields[i].getType() + "\tGenericType: " + DocUtils.getCollectionGenericType(fields[i]));
            } else if (DocUtils.isSimpleType(fields[i].getType())) {
                System.out.println("SimpleType: " + fields[i].getType());
            } else {
                System.out.println("Bean: " + fields[i].getType());
            }
        }
    }

    @Test
    public void getCollectionGenericTypetest() {
        Class<?> aClass = null;
        try {
            aClass = Class.forName("org.opencb.commons.utils.TestBeanClass");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field f : declaredFields) {
            Type genericParameterType = f.getGenericType();
            //  System.out.println(genericParameterType.getTypeName());
            if (genericParameterType instanceof ParameterizedType) {
                ParameterizedType aType = (ParameterizedType) genericParameterType;
                System.out.println(genericParameterType.getTypeName());
                List<Class> classes = getInnerClasses(aType);
                System.out.println("result :::: " + classes);
            }
        }
    }

    private List<Class> getInnerClasses(ParameterizedType aType) {
        List<Class> classes = new ArrayList<>();
        Type[] parameterArgTypes = aType.getActualTypeArguments();
        for (Type parameterArgType : parameterArgTypes) {
            if (parameterArgType instanceof Class) {
                classes.add((Class) parameterArgType);
            } else if (parameterArgType instanceof ParameterizedType) {
                classes.addAll(getInnerClasses((ParameterizedType) parameterArgType));
            }
            //System.out.println("parameterArgClass = " + parameterArgClass);
        }
        return classes;
    }

    @Test
    public void testMap() throws Exception {
        Field[] fields = MarkdownDoc.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (DocUtils.isClassCollection(fields[i].getType())) {
                System.out.println(fields[i].getType() + "\tGenericType: " + DocUtils.getCollectionGenericType(fields[i]));
            } else if (DocUtils.isSimpleType(fields[i].getType())) {
                System.out.println("SimpleType: " + fields[i].getType());
            } else {
                System.out.println("Bean: " + fields[i].getType());
            }
            if (DocUtils.isMap(fields[i].getType())) {
                System.out.println("Map Internal Beans: " + DocUtils.getMapGenericType(fields[i]));
            }
        }
    }

    @Test
    public void testIsSimpleClass() throws Exception {
        List<Class> classes = new ArrayList<>();
        classes.add(String.class);
        classes.add(Object.class);
        classes.add(DocUtils.class);
        classes.add(DataClassDoc.class);
        classes.add(DataFieldDoc.class);
        for (Class clazz : classes) {
            verboseSimpleClass(clazz);
        }


    }


    public void verboseSimpleClass(Class clazz) throws Exception {
        if (DocUtils.isSimpleType(clazz)) {
            System.out.println("IS SIMPLE TYPE " + clazz.getSimpleName());
        }

    }


}
