package org.opencb.commons.docs;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownDoc {

    private ClassDoc doc;
    private String description;
    private String name;
    private List<MarkdownField> fields = new ArrayList<>();
    private String qualifiedTypeName;
    private boolean upperClass;

    private boolean enumeration;
    private List<String> innerClasses = new ArrayList<>();

    public MarkdownDoc(ClassDoc doc) {
        this.doc = doc;
        initialize();
    }

    private void initialize() {

        description = doc.commentText();
        name = doc.name();
        qualifiedTypeName = doc.qualifiedTypeName();
        checkEnumeration();
        ClassDoc[] innerclass = doc.innerClasses();
        if (innerclass != null && innerclass.length > 0) {
            for (ClassDoc inner : innerclass) {
                innerClasses.add(inner.name());
            }
            upperClass = true;
        }
        FieldDoc[] fieldDocs = doc.fields(false);
        for (FieldDoc f : fieldDocs) {
            MarkdownField mf = new MarkdownField(f);
            mf.setEnumerationClass(enumeration);
            fields.add(mf);
            if (upperClass && innerClasses.contains(f.type().typeName())) {
                mf.setParentType(name);
            }
        }
    }

    private void checkEnumeration() {
        String sourceFilePath = Options.getInstance().getSourceClassesDir()
                + getQualifiedTypeName().replaceAll("\\.", File.separator) + ".java";
        String className = doc.name();
        if (className.contains(".")) {
            className = className.substring(className.lastIndexOf(".") + 1, className.length());
            sourceFilePath = Options.getInstance().getSourceClassesDir()
                    + getQualifiedTypeName().substring(0, getQualifiedTypeName().lastIndexOf("."))
                    .replaceAll("\\.", File.separator) + ".java";
        }
        String content = MarkdownUtils.getFileContentAsString(sourceFilePath);

        String reg = "enum " + className;
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(content);
        enumeration = m.find();

        // System.out.println(doc.name() + " ---- > " + className + " es enumeration " + enumeration + " comprobado en " + sourceFilePath);
    }

    public ClassDoc getDoc() {
        return doc;
    }

    public MarkdownDoc setDoc(ClassDoc doc) {
        this.doc = doc;
        return this;
    }

    public List<MarkdownField> getFields() {
        return fields;
    }

    public MarkdownDoc setFields(List<MarkdownField> fields) {
        this.fields = fields;
        return this;
    }

    public String getNotTagedFieldAsString() {
        String res = "";
        for (MarkdownField mf : fields) {
            if (mf.isNoTagged()) {
                res += mf.getName() + " ";
            }
        }
        return res;
    }

    public String getUniquesFieldsAsString() {
        String res = "";
        for (MarkdownField mf : fields) {
            if (mf.isUnique()) {
                res += mf.getName() + " ";
            }
        }
        return res;
    }

    public String getCreateFieldsAsString() {
        String res = "";
        for (MarkdownField mf : fields) {
            res += mf.getCreateFieldAsString();
        }
        return res;
    }

    public String getUpdateFieldsAsString() {
        String res = "";
        for (MarkdownField mf : fields) {
            if (mf.isCreate()) {
                res += mf.getName() + " ";
            }
        }
        return res;
    }

    public String getDescription() {
        return description;
    }

    public MarkdownDoc setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public MarkdownDoc setName(String name) {
        this.name = name;
        return this;
    }

    public String getQualifiedTypeName() {
        return qualifiedTypeName;
    }

    public MarkdownDoc setQualifiedTypeName(String qualifiedTypeName) {
        this.qualifiedTypeName = qualifiedTypeName;
        return this;
    }

    public boolean isUpperClass() {
        return upperClass;
    }

    public MarkdownDoc setUpperClass(boolean upperClass) {
        this.upperClass = upperClass;
        return this;
    }

    public List<String> getInnerClasses() {
        return innerClasses;
    }

    public MarkdownDoc setInnerClasses(List<String> innerClasses) {
        this.innerClasses = innerClasses;
        return this;
    }

    public boolean isEnumeration() {
        return enumeration;
    }

    public MarkdownDoc setEnumeration(boolean enumeration) {
        this.enumeration = enumeration;
        return this;
    }
}

