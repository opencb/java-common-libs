package org.opencb.commons.docs.models;

import java.util.Arrays;
import java.util.List;

public class DataFieldDoc {
    /**
     * @return id of the field.
     */
    private String id;
    private String name;
    private String[] alias;
    private boolean required;
    private boolean indexed;
    private boolean managed;
    private boolean immutable;
    private boolean unique;
    private boolean deprecated;
    private String defaultValue;
    private String since;
    private String[] dependsOn;
    private String description;
    private Class clazz;
    private boolean collection;
    private boolean primitive;
    private List<Class<?>> genericClasses;
    private boolean enumeration;
    private String[] uncommentedClasses;

    public DataFieldDoc() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DataFieldDoc{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", alias=").append(alias == null ? "null" : Arrays.asList(alias).toString());
        sb.append(", required=").append(required);
        sb.append(", indexed=").append(indexed);
        sb.append(", managed=").append(managed);
        sb.append(", immutable=").append(immutable);
        sb.append(", unique=").append(unique);
        sb.append(", deprecated=").append(deprecated);
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", since='").append(since).append('\'');
        sb.append(", dependsOn=").append(dependsOn == null ? "null" : Arrays.asList(dependsOn).toString());
        sb.append(", description='").append(description).append('\'');
        sb.append(", clazz=").append(clazz);
        sb.append(", collection=").append(collection);
        sb.append(", primitive=").append(primitive);
        sb.append(", genericClasses=").append(genericClasses);
        sb.append(", enumeration=").append(enumeration);
        sb.append(", uncommentedClasses=").append(uncommentedClasses == null ? "null" : Arrays.asList(uncommentedClasses).toString());
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public DataFieldDoc setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DataFieldDoc setName(String name) {
        this.name = name;
        return this;
    }

    public String[] getAlias() {
        return alias;
    }

    public DataFieldDoc setAlias(String[] alias) {
        this.alias = alias;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public DataFieldDoc setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public DataFieldDoc setIndexed(boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    public boolean isManaged() {
        return managed;
    }

    public DataFieldDoc setManaged(boolean managed) {
        this.managed = managed;
        return this;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public DataFieldDoc setImmutable(boolean immutable) {
        this.immutable = immutable;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public DataFieldDoc setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public DataFieldDoc setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public DataFieldDoc setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getSince() {
        return since;
    }

    public DataFieldDoc setSince(String since) {
        this.since = since;
        return this;
    }

    public String[] getDependsOn() {
        return dependsOn;
    }

    public DataFieldDoc setDependsOn(String[] dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DataFieldDoc setDescription(String description) {
        this.description = description;
        return this;
    }

    public Class getClazz() {
        return clazz;
    }

    public DataFieldDoc setClazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public boolean isCollection() {
        return collection;
    }

    public DataFieldDoc setCollection(boolean collection) {
        this.collection = collection;
        return this;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public DataFieldDoc setPrimitive(boolean primitive) {
        this.primitive = primitive;
        return this;
    }

    public List<Class<?>> getGenericClasses() {
        return genericClasses;
    }

    public DataFieldDoc setGenericClasses(List<Class<?>> genericClasses) {
        this.genericClasses = genericClasses;
        return this;
    }

    public boolean isEnumeration() {
        return enumeration;
    }

    public DataFieldDoc setEnumeration(boolean enumeration) {
        this.enumeration = enumeration;
        return this;
    }

    public String[] getUncommentedClasses() {
        return uncommentedClasses;
    }

    public DataFieldDoc setUncommentedClasses(String[] uncommentedClasses) {
        this.uncommentedClasses = uncommentedClasses;
        return this;
    }
}
