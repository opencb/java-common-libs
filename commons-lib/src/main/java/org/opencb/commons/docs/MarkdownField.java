package org.opencb.commons.docs;

import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownField {

    private static final Pattern SIMPLE_LINK = Pattern.compile("(?<label>[^<]*)<(?<url>[^>]+)>");
    private static final Pattern FULL_LINK = Pattern.compile("\\[(?<label>[^)]+)\\]\\s+?\\((?<url>[^]\\s]+)\\)|\\((?<url2>[^]\\s]+)\\)");

    private FieldDoc field;
    private List<MarkdownTag> tags = new ArrayList<>();
    private boolean updatable = true;
    private boolean create = true;
    private boolean unique = false;
    private boolean required = false;
    private boolean noTagged = true;
    private boolean deprecated = false;
    private String name;
    private String see;
    private String since;
    private String className;
    private String implNote;
    private String qualifiedTypeName;
    private String constraints;
    private String description;
    private String type;
    private boolean collection;
    private String parentType;
    private boolean enumerationClass;

    public MarkdownField(FieldDoc field) {
        this.field = field;
        name = field.name();
        Tag[] tagdocs = field.tags();
        for (Tag t : tagdocs) {
            tags.add(new MarkdownTag(t));
        }
        initialize();
    }

    private void initialize() {
        for (MarkdownTag mt : tags) {
            if (mt.getName().equals("@apiNote")) {
                noTagged = false;
                constraints = mt.getDescription().toLowerCase();
                if (constraints.contains("unique")) {
                    unique = true;
                }
                if (constraints.contains("required")) {
                    required = true;
                }
                if (constraints.contains("internal")) {
                    updatable = false;
                    create = false;
                }
                if (constraints.contains("immutable")) {
                    updatable = false;
                }
            }
            if ("@deprecated".equals(mt.getName())) {
                deprecated = true;
            }
            if ("@implNote".equals(mt.getName())) {
                implNote = mt.getDescription().trim();
            }
            if ("@see".equals(mt.getName())) {
                see = mt.getDescription().trim();
            }
            if ("@since".equals(mt.getName())) {
                since = mt.getDescription().trim();
            }
        }
        enumerationClass = false;
        type = String.valueOf(field.type());
        int index = type.lastIndexOf('.') + 1;
        className = type.substring(index).trim();
        description = field.commentText();
        qualifiedTypeName = field.type().qualifiedTypeName();
        collection = qualifiedTypeName.contains("java.util.");
    }

    public List<String> getInnerClassesOfCollection(String className) {
        List<String> res = new ArrayList<>();
        Pattern p = Pattern.compile("<(.*)>");
        Matcher m2 = p.matcher(className);
        if (m2.find()) {
            //ya tenemos el nombre de la clase
            String classes = m2.group().replaceAll("<", "").replaceAll(">", "");
            if (classes.contains(",")) {
                res = Arrays.asList(classes.split(","));
            } else {
                res.add(classes);
            }
        }
        return res;
    }

    public String getCreateFieldAsString() {
        String res = "";
        if (create) {
            res = getName() + " ";
        }
        if (create && required) {
            res = getName() + "* ";
        }
        return res;
    }

    public String getNameClassAsString() {
        String res = "";
        if (isDeprecated()) {
            res += "**~~" + getName() + "~~**";
        } else {
            res += "**" + getName() + "**";
        }
        if (!isEnumerationClass()) {
            res += "<br> *" + getClassName() + "*";
        }

        return res;
    }

    public String getNameLinkedClassAsString(String currentDocument) {
        String res = "";
        if (isDeprecated()) {
            res += "**~~" + getName() + "~~**<br>*" + generateLink(currentDocument, getClassName()) + "*";
        } else {
            res += "**" + getName() + "**<br>*" + generateLink(currentDocument, getClassName()) + "*";
        }

        return res;
    }

    public String getCollectionClassAsString(Map<String, MarkdownDoc> classes, Map<String, String> innerClasses,
                                             String currentDocument) {

        String res = "";

        res += "**" + getName() + "**<br> " + getClassName() + "<";

        for (String s : innerClasses.keySet()) {
            if (classes.keySet().contains(innerClasses.get(s))) {
                res += "*" + generateLink(currentDocument, s) + "*,";
            } else {
                res += "*" + s.trim() + "*,";
            }
        }
        res = res.trim().substring(0, res.trim().length() - 1);
        res += ">";
        return res;
    }

    private String generateLink(String currentDocument, String clase) {
        String res = "";
        String link = "";

        if (getParentType() != null) {
            link = getParentType().toLowerCase()
                    + clase.toLowerCase();
            clase = getParentType() + "." + clase;
        } else {
            link = clase;
        }

        if (MarkdownUtils.checkEnumeration(clase, field.qualifiedName())) {
            link = "enum-" + link;
        }
        res = "<a href=\"" + currentDocument + ".md#" + link + "\"><em>" + clase + "</em></a>";
        return res;
    }

    public String getSinceAsString() {

        String res = "";
        if (since != null && since.length() > 0) {
            res += "<br>_since_: " + since;
        }

        return res;
    }

    public String getDeprecatedAsString() {

        String res = "";
        if (isDeprecated()) {
            res += "<br>_Deprecated_";
        }

        return res;
    }

    public String getDescriptionAsString() {
        String res = "<p>" + getDescription() + "</p>" + renderImplNote();
        res = renderSeeTag(res);
        res = getConstraintsAsString(res);
        return res.replaceAll("\\n", "<br>");
    }

    private String renderImplNote() {
        if (implNote != null) {
            return "_Note_: _" + implNote + "_";
        }
        return "";
    }

    private String renderSeeTag(String res) {
        String tag = getSee();
        if (tag != null) {
            StringBuilder target = new StringBuilder();
            if (tag.length() > 0) {
                String text = tag;
                if (tag.startsWith("\"") && tag.endsWith("\"")) {
                    text = tag.substring(1, tag.length() - 1).trim();
                }
                Matcher matcher = SIMPLE_LINK.matcher(text);
                if (!matcher.matches()) {
                    matcher = FULL_LINK.matcher(text);
                } else {
                    //its a correct link
                    return tag;
                }
                if (matcher.matches()) {
                    String label = matcher.group("label");
                    String url = matcher.group("url");
                    if ((label == null || label.isEmpty()) && (url == null || url.isEmpty())) {
                        url = matcher.group("url2");
                    }
                    if (label != null) {
                        label = label.trim();
                    }
                    if (label == null || label.isEmpty()) {
                        label = matcher.group("url");
                    }

                    target.append("<a href=\"");
                    target.append(url);
                    target.append("\">");
                    target.append(label);
                    target.append("</a>");
                    if (res.endsWith("</p>")) {
                        res += "_More info at_: " + target.toString();
                    } else {
                        res += "</br>_More info at_: " + target.toString();
                    }
                } else {
                    if (res.endsWith("</p>")) {
                        res += "" + tag;
                    } else {
                        res += "</br>" + tag;
                    }
                    return res;
                }
            }
        }
        return res;
    }

    public String getConstraintsAsString(String res) {
        if (!(getConstraints() == null) && !"".equals(getConstraints().trim())) {
            if (res.endsWith("</p>")) {
                res += "_Tags_: _" + getConstraints() + "_";
            } else {
                res += "<br>_Tags_: _" + getConstraints() + "_";
            }
        }
        return res;
    }

    public FieldDoc getField() {
        return field;
    }

    public MarkdownField setField(FieldDoc field) {
        this.field = field;
        return this;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public MarkdownField setUpdatable(boolean updatable) {
        this.updatable = updatable;
        return this;
    }

    public boolean isCreate() {
        return create;
    }

    public MarkdownField setCreate(boolean create) {
        this.create = create;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public MarkdownField setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public MarkdownField setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public List<MarkdownTag> getTags() {
        return tags;
    }

    public MarkdownField setTags(List<MarkdownTag> tags) {
        this.tags = tags;
        return this;
    }

    public String getName() {
        return name;
    }

    public MarkdownField setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isNoTagged() {
        return noTagged;
    }

    public MarkdownField setNoTagged(boolean noTagged) {
        this.noTagged = noTagged;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public MarkdownField setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    public String getSee() {
        return see;
    }

    public MarkdownField setSee(String see) {
        this.see = see;
        return this;
    }

    public String getSince() {
        return since;
    }

    public MarkdownField setSince(String since) {
        this.since = since;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public MarkdownField setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getImplNote() {
        return implNote;
    }

    public MarkdownField setImplNote(String implNote) {
        this.implNote = implNote;
        return this;
    }

    public String getQualifiedTypeName() {
        return qualifiedTypeName;
    }

    public MarkdownField setQualifiedTypeName(String qualifiedTypeName) {
        this.qualifiedTypeName = qualifiedTypeName;
        return this;
    }

    public String getConstraints() {
        return constraints;
    }

    public MarkdownField setConstraints(String constraints) {
        this.constraints = constraints;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MarkdownField setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public MarkdownField setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isCollection() {
        return collection;
    }

    public MarkdownField setCollection(boolean collection) {
        this.collection = collection;
        return this;
    }

    public String getParentType() {
        return parentType;
    }

    public MarkdownField setParentType(String parentType) {
        this.parentType = parentType;
        return this;
    }

    public boolean isEnumerationClass() {
        return enumerationClass;
    }

    public MarkdownField setEnumerationClass(boolean enumerationClass) {
        this.enumerationClass = enumerationClass;
        return this;
    }
}
