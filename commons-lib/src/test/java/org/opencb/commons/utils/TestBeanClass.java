package org.opencb.commons.utils;

import org.opencb.commons.docs.models.DataFieldDoc;

import java.util.List;

public class TestBeanClass {
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
    private DataFieldDoc dataFieldDoc;
    private List<DataFieldDoc> dataFieldDocs;
    private boolean collection;
    private boolean primitive;
    private List<List<DataFieldDoc>> genericClasses;
    private boolean enumeration;
    private String[] uncommentedClasses;

}
