package org.opencb.commons.docs;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;

import java.util.ArrayList;
import java.util.List;

public class MarkdownClassDoc {

    private ClassDoc doc;
    private String description;
    private String name;
    private List<MarkdownFieldDoc> fields = new ArrayList<>();
    private String qualifiedTypeName;
    private boolean upperClass;

    private boolean enumeration;
    private List<String> innerClasses = new ArrayList<>();

    public MarkdownClassDoc(ClassDoc doc) {
        this.doc = doc;
        initialize();
    }

    private void initialize() {

        description = doc.commentText();
        name = doc.name();
        qualifiedTypeName = doc.qualifiedTypeName();
        enumeration = MarkdownUtils.checkEnumeration(name, qualifiedTypeName);
        ClassDoc[] innerclass = doc.innerClasses();
        if (innerclass != null && innerclass.length > 0) {
            for (ClassDoc inner : innerclass) {
                innerClasses.add(inner.name());
            }
            upperClass = true;
        }
        FieldDoc[] fieldDocs = doc.fields(false);
        for (FieldDoc f : fieldDocs) {
            MarkdownFieldDoc mf = new MarkdownFieldDoc(f);
            mf.setEnumerationClass(enumeration);
            fields.add(mf);
       /*     if (f.type().typeName().toLowerCase(Locale.ROOT).contains("rga")) {
                //System.out.println("Inner classes " + innerClasses);
                //System.out.println("Type:::  " + f.type().typeName() + " upperClass " + upperClass);
            }*/
            if (upperClass && innerClasses.contains(f.type().typeName())) {
                mf.setParentType(name);
            }
        }
    }

    public ClassDoc getDoc() {
        return doc;
    }

    public MarkdownClassDoc setDoc(ClassDoc doc) {
        this.doc = doc;
        return this;
    }

    public List<MarkdownFieldDoc> getFields() {
        return fields;
    }

    public MarkdownClassDoc setFields(List<MarkdownFieldDoc> fields) {
        this.fields = fields;
        return this;
    }

    public String getNotTagedFieldAsString() {
        String res = "";
        for (MarkdownFieldDoc mf : fields) {
            if (mf.isNoTagged()) {
                res += mf.getName() + " ";
            }
        }
        return res;
    }

    public String getUniquesFieldsAsString() {
        String res = "";
        for (MarkdownFieldDoc mf : fields) {
            if (mf.isUnique()) {
                res += mf.getName() + " ";
            }
        }
        return res;
    }

    public String getCreateFieldsAsString() {
        String res = "";
        for (MarkdownFieldDoc mf : fields) {
            res += mf.getCreateFieldAsString();
        }
        return res;
    }

    public String getUpdateFieldsAsString() {
        String res = "";
        for (MarkdownFieldDoc mf : fields) {
            if (mf.isCreate()) {
                res += mf.getName() + " ";
            }
        }
        return res;
    }

    public String getDescription() {
        return description;
    }

    public MarkdownClassDoc setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public MarkdownClassDoc setName(String name) {
        this.name = name;
        return this;
    }

    public String getQualifiedTypeName() {
        return qualifiedTypeName;
    }

    public MarkdownClassDoc setQualifiedTypeName(String qualifiedTypeName) {
        this.qualifiedTypeName = qualifiedTypeName;
        return this;
    }

    public boolean isUpperClass() {
        return upperClass;
    }

    public MarkdownClassDoc setUpperClass(boolean upperClass) {
        this.upperClass = upperClass;
        return this;
    }

    public List<String> getInnerClasses() {
        return innerClasses;
    }

    public MarkdownClassDoc setInnerClasses(List<String> innerClasses) {
        this.innerClasses = innerClasses;
        return this;
    }

    public boolean isEnumeration() {
        return enumeration;
    }

    public MarkdownClassDoc setEnumeration(boolean enumeration) {
        this.enumeration = enumeration;
        return this;
    }
}

