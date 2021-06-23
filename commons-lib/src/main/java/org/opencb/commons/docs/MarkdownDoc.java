package org.opencb.commons.docs;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;

import java.util.ArrayList;
import java.util.List;

public class MarkdownDoc {

    private ClassDoc doc;
    private String description;
    private String name;
    private List<MarkdownField> fields = new ArrayList<>();
    private String qualifiedTypeName;

    public MarkdownDoc(ClassDoc doc) {
        this.doc = doc;
        initialize();
    }

    private void initialize() {
        FieldDoc[] fieldDocs = doc.fields(false);
        for (FieldDoc f : fieldDocs) {
            fields.add(new MarkdownField(f));
        }
        description = doc.commentText();
        name = doc.name();
        qualifiedTypeName = doc.qualifiedTypeName();
        //System.out.println("initialized " + name + " with " + fields.size() + " fields");
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
}

