package org.opencb.commons.docs.models;

import java.util.List;

public class DataClassDoc {

    private String name;
    private boolean managed;
    private boolean deprecated;
    private boolean enumeration;
    private String since;
    private String description;
    private List<DataFieldDoc> dataFieldDocs;
    private Class clazz;


    public DataClassDoc() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DataClassDoc{");
        sb.append("name='").append(name).append('\'');
        sb.append(", managed=").append(managed);
        sb.append(", deprecated=").append(deprecated);
        sb.append(", since='").append(since).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", dataFieldDocs=").append(dataFieldDocs);
        sb.append(", clazz=").append(clazz);
        sb.append('}');
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(boolean managed) {
        this.managed = managed;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DataFieldDoc> getDataFieldDocs() {
        return dataFieldDocs;
    }

    public void setDataFieldDocs(List<DataFieldDoc> dataFieldDocs) {
        this.dataFieldDocs = dataFieldDocs;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public boolean isEnumeration() {
        return enumeration;
    }

    public DataClassDoc setEnumeration(boolean enumeration) {
        this.enumeration = enumeration;
        return this;
    }
}
