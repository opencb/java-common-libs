package org.opencb.commons.docs.doc;

import org.opencb.commons.annotations.DataClass;
import org.opencb.commons.annotations.DataField;
import org.opencb.commons.docs.DocUtils;
import org.opencb.commons.docs.config.DocConfiguration;
import org.opencb.commons.docs.models.DataClassDoc;
import org.opencb.commons.docs.models.DataFieldDoc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Doc {


    protected final DocConfiguration config;
    protected String currentDocument;

    public Doc(DocConfiguration config) {
        this.config = config;
    }

    protected DataClassDoc buildClassDoc(Class<?> clazz) {
        DataClassDoc docClass = new DataClassDoc();

        docClass.setClazz(clazz);
        if (clazz.getAnnotation(DataClass.class) != null) {
            docClass.setName((clazz.getAnnotation(DataClass.class)).id());
            docClass.setDeprecated((clazz.getAnnotation(DataClass.class)).deprecated());
            docClass.setManaged((clazz.getAnnotation(DataClass.class)).managed());
            docClass.setDescription((clazz.getAnnotation(DataClass.class)).description());
            docClass.setSince((clazz.getAnnotation(DataClass.class)).since());
            docClass.setEnumeration(clazz.isEnum());
        } else {
            docClass.setName(clazz.getSimpleName());
            docClass.setDescription("Data model details for class: " + clazz.getName());
            docClass.setEnumeration(clazz.isEnum());
        }
        docClass.setDataFieldDocs(buildFieldDoc(clazz));

        return docClass;
    }

    private List buildFieldDoc(Class<?> clazz) {
        List<DataFieldDoc> res = new ArrayList<>();
        List<Field> fields = getAllFields(clazz);
        for (Field field : fields) {
            if (field.getAnnotation(DataField.class) != null) {
                DataFieldDoc docField = new DataFieldDoc();
                docField.setId((field.getAnnotation(DataField.class)).id());
                docField.setName((field.getAnnotation(DataField.class)).id());
                docField.setAlias((field.getAnnotation(DataField.class)).alias());
                docField.setRequired((field.getAnnotation(DataField.class)).required());
                docField.setIndexed((field.getAnnotation(DataField.class)).indexed());
                docField.setManaged((field.getAnnotation(DataField.class)).managed());
                docField.setImmutable((field.getAnnotation(DataField.class)).immutable());
                docField.setUnique((field.getAnnotation(DataField.class)).unique());
                docField.setDeprecated((field.getAnnotation(DataField.class)).deprecated());
                docField.setDefaultValue((field.getAnnotation(DataField.class)).defaultValue());
                docField.setSince((field.getAnnotation(DataField.class)).since());
                docField.setDependsOn((field.getAnnotation(DataField.class)).dependsOn());
                docField.setDescription((field.getAnnotation(DataField.class)).description());
                docField.setClazz(field.getType());
                docField.setCollection(DocUtils.isClassCollection(field.getType()));
                docField.setUncommentedClasses((field.getAnnotation(DataField.class)).uncommentedClasses());
                if (docField.isCollection()) {
                    if (DocUtils.isMap(field.getType())) {
                        docField.setGenericClasses(DocUtils.getMapGenericType(field));
                    } else {
                        List<Class<?>> classes = new ArrayList<>();
                        classes.add(DocUtils.getCollectionGenericType(field));
                        docField.setGenericClasses(classes);
                    }
                    docField.setPrimitive(false);
                    docField.setEnumeration(false);
                } else {
                    docField.setPrimitive(DocUtils.isSimpleType(field.getType()));
                    docField.setEnumeration(field.getType().isEnum());
                }
                res.add(docField);
            }

        }
        return res;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        final Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            fields.add(field);
        }
        if (clazz.getSuperclass() != null && clazz.getSuperclass().getCanonicalName().contains("opencb")) {
            fields.addAll(getAllFields(clazz.getSuperclass()));
        }
        return fields;
    }

    public abstract String getOverview(DataClassDoc doc);

    public abstract String getDataModel(DataClassDoc doc);

    public abstract String getSummary(DataClassDoc doc);

    public abstract String getRelatedTables(DataClassDoc doc);

    public abstract String getExample(DataClassDoc doc);

    public String getDoc(DataClassDoc doc) {
        currentDocument = doc.getName();
        String res = getOverview(doc);
        res += getSummary(doc);
        res += getDataModel(doc);
        res += getRelatedTables(doc);
        res += getExample(doc);
        return res;
    }

    public abstract void writeDoc(DocConfiguration config) throws IOException;
}
