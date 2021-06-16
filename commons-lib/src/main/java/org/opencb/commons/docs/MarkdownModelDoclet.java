package org.opencb.commons.docs;

import com.sun.javadoc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @jndi-name BasicDoclet
 * @unid 1
 */
public class MarkdownModelDoclet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownModelDoclet.class);
    private static final Pattern SIMPLE_LINK = Pattern.compile("(?<label>[^<]*)<(?<url>[^>]+)>");
    private static final Pattern FULL_LINK = Pattern.compile("\\[(?<label>[^)]+)\\]\\s+?\\((?<url>[^]\\s]+)\\)|\\((?<url2>[^]\\s]+)\\)");
    private static Options options = new Options();
    private static Map<String, ClassDoc> classes = new HashMap<>();
    private static Set<ClassDoc> tablemodels = new HashSet<>();
    private static String currentDocument;

    public MarkdownModelDoclet() {
    }

    public static boolean start(RootDoc rootDoc) {
        LOGGER.info("Generating markdown for the data model");
        options.load(rootDoc.options());
        classes = createMap(rootDoc.classes());
        printDocument();
        return true;
    }

    private static Map<String, ClassDoc> createMap(ClassDoc[] classes) {
        LOGGER.info("Creating a Map with classes of the data model");
        Map<String, ClassDoc> res = new HashMap<>();
        for (ClassDoc doc : classes) {
            res.put(String.valueOf(doc), doc);
        }
        return res;
    }

    public static void printDocument() {
        LOGGER.info("Printing markdowns representing the data model");
        for (ClassDoc doc : classes.values()) {
            if (options.getClasses2Markdown().contains(String.valueOf(doc))) {
                int index = String.valueOf(doc).lastIndexOf('.') + 1;
                if (index > 0) {
                    String fileName = String.valueOf(doc).substring(index);
                    currentDocument = fileName;
                    StringBuffer res = new StringBuffer();
                    res.append("# " + fileName + "\n");
                    res.append("## Overview\n" + doc.commentText() + "\n");
                    res.append(generateFieldsAttributesParagraph(doc.fields(false), String.valueOf(doc)));
                    res.append("## Data Model\n");
                    res = getTableModel(doc, doc.fields(false), fileName, res);
                    //System.out.println(String.valueOf(options.getJsonMap().keySet()));
                    if (options.getJsonMap().keySet().contains(fileName + ".json")) {
                        res.append("## Example\n");
                        res.append("This is a full JSON example:\n");
                        res.append("```javascript\n" + options.getJsonMap().get(fileName + ".json") + "\n```");
                    }
                    try {
                        write2File(options.getOutputdir() + fileName + ".md", res.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String generateFieldsAttributesParagraph(FieldDoc[] fields, String className) {
        StringBuffer res = new StringBuffer();
        List<String> unique = new ArrayList<>();
        List<String> updatable = new ArrayList<>();
        List<String> create = new ArrayList<>();
        List<String> required = new ArrayList<>();
        List<String> noTagsFields = new ArrayList<>();
        if (options.getTableTagsClasses().contains(className)) {
            res.append("### Fields tags \n");
            res.append("| Field | unique | required | immutable| internal|\n| :--- | :---: | :---: |:---: |:---: |\n");
        }

        if (fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                Tag[] tags = fields[i].tags();
                String stags = new String();
                boolean enc = false;
                for (int j = 0; j < tags.length; j++) {
                    if (tags[j].name().equals("@apiNote")) {
                        enc = true;
                        stags = tags[j].text().toLowerCase();
                        if (stags.contains("unique")) {
                            unique.add(fields[i].name());
                        }
                        if (stags.contains("required")) {
                            required.add(fields[i].name());
                        }
                        if (!stags.contains("immutable") && !stags.contains("internal")) {
                            updatable.add(fields[i].name());
                        }
                        if (!stags.contains("internal")) {
                            create.add(fields[i].name());
                        }
                    }
                }
                if (!enc) {
                    create.add(fields[i].name());
                    updatable.add(fields[i].name());
                }
                if (options.getTableTagsClasses().contains(className)) {
                    if (stags.trim().length() > 0) {
                        res.append("| " + fields[i].name() + " | " + getFlag(stags.contains("unique")) + " | "
                                + getFlag(stags.contains("required")) + " |" + getFlag(stags.contains("required")) + " | "
                                + getFlag(stags.contains("internal")) + " |\n");
                    } else {
                        noTagsFields.add(fields[i].name());
                    }
                }
            }
            res.append("### Fields without tags \n");
            res.append("`");
            for (String s : noTagsFields) {
                res.append(s + " ");
            }
            res.append("`");

            res.append("\n### Fields for Create Operations \n");
            res.append("`");
            for (String s : create) {
                res.append(required.contains(s) ? s + "* " : s + " ");
            }
            res.append("`");

            res.append("\n### Fields for Update Operations\n");
            res.append("`");
            for (String s : updatable) {
                res.append(s + " ");
            }
            res.append("`");

            if (unique.size() > 0) {
                res.append("\n### Fields uniques\n");

                res.append("`");
                for (String s : unique) {
                    res.append(s + " ");
                }
                res.append("`");
            }
            res.append("\n");
        }

        return res.toString();
    }

    private static String getFlag(boolean unique) {
        String res = "";
        if (unique) {
            res += "<img src=\"http://docs.opencb.org/s/en_GB/7101/4f8ce896bdf903a209ab02696e335acf844f5c2c/_/images/icons/emoticons/check"
                    + ".png\" width=\"16px\" heigth=\"16px\">";
        } else {
            res += "<img src=\"http://docs.opencb.org/s/en_GB/7101/4f8ce896bdf903a209ab02696e335acf844f5c2c/_/images/icons/emoticons/error"
                    + ".png\" width=\"16px\" heigth=\"16px\">";
        }

        return res;
    }

    private static StringBuffer getTableModel(ClassDoc doc, FieldDoc[] fields, String fileName, StringBuffer res) {
        // StringBuffer res = new StringBuffer();

        LOGGER.info("Generating tables of fields in the data model markdowns for class " + String.valueOf(doc));
        Set<ClassDoc> relatedTableModels = new HashSet<>();
        res.append("### " + fileName + "\n");
        res.append("You can find the Java code [here](" + options.getGithubServer() + "src/main/java/"
                + getPackageAsPath(String.valueOf(doc)) + ".java).\n\n");
        if (fields.length > 0) {
            res.append("| Field | Tags | Description |\n| :--- | :--- | :--- |\n");
            for (int i = 0; i < fields.length; i++) {
                boolean deprecated = false;
                Tag[] tags = fields[i].tags();
                String constraints = "";
                String implNote = "";
                String see = "";
                String since = "";
                for (int j = 0; j < tags.length; j++) {
                    if ("@apiNote".equals(tags[j].name()) && tags[j].text().trim() != null && tags[j].text().trim().length() > 0) {
                        constraints = "**`" + tags[j].text().trim() + "`**";
                    }
                    if ("@deprecated".equals(tags[j].name())) {
                        deprecated = true;
                    }
                    if ("@implNote".equals(tags[j].name())) {
                        implNote = "**`" + tags[j].text().trim() + "`**";
                    }
                    if ("@see".equals(tags[j].name())) {
                        see = tags[j].text().trim();
                    }
                    if ("@since".equals(tags[j].name())) {
                        since = tags[j].text().trim();
                    }
                }
                int index = String.valueOf(fields[i].type()).lastIndexOf('.') + 1;
                String className = String.valueOf(fields[i].type()).substring(index);
                if (!isModel(String.valueOf(fields[i].type()))) {
                    res.append("| **" + (deprecated ? "~~" + fields[i].name() + "~~ <br> Deprecated" : fields[i].name())
                            + "**<br>*" + className + "* <br>" + (since != null && since.length() > 0 ? "since: " + since : "") + " |"
                            + constraints + " | "
                            + constructDescription(fields[i].commentText(), implNote, see) + " |\n");
                } else {
                    res.append("| **" + (deprecated ? "~~" + fields[i].name() + "~~ <br> Deprecated" : fields[i].name())
                            + "**<br>*<a href=\"" + currentDocument + ".md#" + className + "\"><em>" + className
                            + "</em></a>*| " + constraints + " | "
                            + constructDescription(fields[i].commentText(), implNote, see) + " |\n");
                    if (!tablemodels.contains(classes.get(String.valueOf(fields[i].type())))) {
                        relatedTableModels.add(classes.get(String.valueOf(fields[i].type())));
                    }
                    tablemodels.add(classes.get(String.valueOf(fields[i].type())));
                }
            }
            if (!res.toString().contains("## Related data models\n") && tablemodels.size() > 0) {
                res.append("## Related data models\n");
            }
            for (ClassDoc tableModel : relatedTableModels) {
                int index = String.valueOf(tableModel).lastIndexOf('.') + 1;
                String className = String.valueOf(tableModel).substring(index);
                res = getTableModel(tableModel, tableModel.fields(false), className, res);
            }
        }
        return res;
    }

    private static String constructDescription(String commentText, String implNote, String see) {
        String res = "<p>" + commentText + "</p>" + implNote + "</br>" + renderSeeTag(see);

        return res.replaceAll("\\n", "<br>");
    }

    private static String getPackageAsPath(String spackage) {
        return spackage.replaceAll("\\.", File.separator);
    }

    private static boolean isModel(String className) {

        return classes.keySet().contains(className);
    }

    public static void write2File(String fileName, String toWrite)
            throws IOException {
        File f = new File(fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(toWrite);
        writer.close();
    }

    public static boolean validOptions(String[][] options, DocErrorReporter reporter) {
        LOGGER.info("Validating input options");
        boolean res = Options.validOptions(options, reporter);
        Options.validOptions(options, reporter);
        LOGGER.info(res ? "Valid input options" : "Invalid input options");
        return res;
    }

    public static int optionLength(String option) {
        LOGGER.info("Validating input options " + option);
        return Options.optionLength(option);
    }

    public static String renderSeeTag(String tag) {
        StringBuilder target = new StringBuilder();
        if (tag.length() > 1) {
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
            } else {
                return tag;
            }
        }
        return target.toString();
    }

    public String getCurrentDocument() {
        return currentDocument;
    }
}
