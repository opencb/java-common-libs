package org.opencb.commons.docs;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @jndi-name BasicDoclet
 * @unid 1
 */
public class MarkdownModelDoclet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownModelDoclet.class);
    private static final String CREABLE = "CREABLE";
    private static final String UPDATABLE = "UPDATABLE";
    private static final String UNIQUE = "UNIQUE";
    private static final String REQUIRED = "REQUIRED";
    private static final String NOTAGS = "NOTAGS";
    private static Options options;
    private static Map<String, MarkdownDoc> classes = new HashMap<>();
    private static Set<MarkdownDoc> tablemodels = new HashSet<>();
    private static String currentDocument;
    private static Set<MarkdownDoc> printedTableModels;

    public MarkdownModelDoclet() {
    }

    public static boolean start(RootDoc rootDoc) {
        LOGGER.info("Generating markdown for the data model");
        //System.out.println("Generating markdown for the data model");
        options = Options.getInstance();
        options.load(rootDoc.options());
        classes = createMap(rootDoc.classes());
        printDocument();
        return true;
    }

    private static Map<String, MarkdownDoc> createMap(ClassDoc[] classes) {
        //System.out.println("Creating a Map with classes of the data model");

        LOGGER.info("Creating a Map with classes of the data model");
        Map<String, MarkdownDoc> res = new HashMap<>();
        for (ClassDoc doc : classes) {
            res.put(String.valueOf(doc), new MarkdownDoc(doc));
        }

        return res;
    }

    public static void printDocument() {
        LOGGER.info("Printing markdowns representing the data model");

        for (MarkdownDoc doc : classes.values()) {
            if (options.getClasses2Markdown().contains(doc.getQualifiedTypeName())) {
                printedTableModels = new HashSet<>();
                //System.out.println("Creating " + doc.getQualifiedTypeName() + " data model");
                currentDocument = doc.getName();
                StringBuffer res = new StringBuffer();
                res.append("# " + currentDocument + "\n");
                res.append("## Overview\n" + doc.getDescription() + "\n");
                res.append(generateFieldsAttributesParagraph(doc.getFields(), doc.getQualifiedTypeName()));
                res.append("## Data Model\n");

                res = getTableModel(doc, currentDocument, res);
                if (options.getJsonMap().keySet().contains(currentDocument + ".json")) {
                    res.append("## Example\n");
                    res.append("This is a full JSON example:\n");
                    res.append("```javascript\n" + options.getJsonMap().get(currentDocument + ".json") + "\n```");
                }
                try {
                    System.out.println("******************************************************************************");
                    write2File(options.getOutputdir() + MarkdownUtils.camelToKebabCase(currentDocument) + ".md",
                            res.toString());
                    System.out.println(options.getOutputdir() + MarkdownUtils.camelToKebabCase(currentDocument) + ".md");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String generateFieldsAttributesParagraph(List<MarkdownField> fields, String className) {
        StringBuffer res = new StringBuffer();
        if ((options.getTableTagsClasses().contains(className)) && (fields.size() > 0)) {
            Map<String, List<MarkdownField>> mfFields = classifyFields(fields);
            res.append("### Summary \n");
            res.append("| Field | Create | Update | Unique | Required|\n| :--- | :---: | :---: |:---: |:---: |\n");
            res.append(getRowTicks(mfFields.get(UPDATABLE)));
            res.append(getRowTicks(mfFields.get(CREABLE)));
            res.append(getRowTicks(mfFields.get(UNIQUE)));
            res.append(getRowTicks(mfFields.get(REQUIRED)));
            res.append(getRowTicks(mfFields.get(NOTAGS)));
        }
        res.append("\n");
        return res.toString();
    }

    private static String getRowTicks(List<MarkdownField> fields) {
        String res = "";
        for (MarkdownField field : fields) {
            res += "| " + field.getName() + " | " + getFlag(field.isCreate()) + " | "
                    + getFlag(field.isUpdatable()) + " |" + getFlag(field.isUnique()) + " | "
                    + getFlag(field.isRequired()) + " |\n";
        }
        return res;
    }

    private static Map<String, List<MarkdownField>> classifyFields(List<MarkdownField> fields) {
        Map<String, List<MarkdownField>> res = new HashMap<>();
        List<MarkdownField> updatable = new ArrayList<>();
        List<MarkdownField> creable = new ArrayList<>();
        List<MarkdownField> unique = new ArrayList<>();
        List<MarkdownField> required = new ArrayList<>();
        List<MarkdownField> notags = new ArrayList<>();
        for (MarkdownField f : fields) {
            if (f.isCreate()) {
                creable.add(f);
            } else if (f.isUpdatable()) {
                updatable.add(f);
            } else if (f.isUnique()) {
                unique.add(f);
            } else if (f.isRequired()) {
                required.add(f);
            } else {
                notags.add(f);
            }
        }

        res.put(CREABLE, creable);
        res.put(UPDATABLE, updatable);
        res.put(UNIQUE, unique);
        res.put(REQUIRED, required);
        res.put(NOTAGS, notags);
        return res;
    }

    private static String generateFieldsAttributesParagraph(MarkdownDoc doc) {
        StringBuffer res = new StringBuffer();
        res.append("### Fields without tags: \n");
        res.append("`");
        res.append(doc.getNotTagedFieldAsString());
        res.append("`");

        res.append("\n### Fields for Create Operations: \n");
        res.append("`");
        res.append(doc.getCreateFieldsAsString());

        res.append("`");

        res.append("\n### Fields for Update Operations\n");
        res.append("`");
        res.append(doc.getUpdateFieldsAsString());

        res.append("`");

        res.append("\n### Fields uniques\n");

        res.append("`");
        res.append(doc.getUniquesFieldsAsString());
        res.append("`");

        return res.toString();
    }

    private static String getFlag(boolean flag) {
        return "<img src=\"https://github.com/opencb/opencga/blob/develop/docs/data-models/" + (flag ? "yes" : "no") + ".png?raw=true\">";
    }

    private static StringBuffer getTableModel(MarkdownDoc doc, String fileName, StringBuffer res) {
        // StringBuffer res = new StringBuffer();
        List<MarkdownField> fields = doc.getFields();
        LOGGER.info("Generating tables of fields in the data model markdowns for class " + doc.getName());
        Set<MarkdownDoc> relatedTableModels = new HashSet<>();
        Set<MarkdownDoc> internalTableModels = new HashSet<>();
        res.append("\n");
        if (doc.isEnumeration()) {
            res.append("### Enum " + doc.getName() + "\n");
            res.append("_Enumeration class._\n");
        } else {
            res.append("### " + doc.getName() + "\n");
        }
        //Create link for github Java code
        res.append("You can find the Java code [here](" + options.getGithubServer() + "src/main/java/"
                + getPackageAsPath(doc) + ".java).\n\n");

        //For each field we make its table row
        if (fields.size() > 0) {
            res.append("| Field | Description |\n| :---  | :--- |\n");
            for (MarkdownField field : fields) {
                if (isModel(field.getType()) && (!field.isEnumerationClass())) {
                    //In this case the class is among the models that we can document and therefore
                    // we must generate the internal link to the table
                    res.append("| " + field.getNameLinkedClassAsString(currentDocument) + " <br>" + field.getDeprecatedAsString()
                            + field.getSinceAsString() + " | " + field.getDescriptionAsString() + " |\n");
                } else if (field.isCollection()) {
                    //The field is a collection, we must extract its internal classes and generate the link if necessary
                    String sourceFilePath = options.getSourceClassesDir()
                            + doc.getQualifiedTypeName().replaceAll("\\.", File.separator) + ".java";
                    Map<String, String> innerClasses = MarkdownUtils.getInnerClass(field.getName(), sourceFilePath, field.getClassName());
                    for (String innerClass : innerClasses.values()) {
                        if (classes.containsKey(innerClass)) {
                            relatedTableModels.add(classes.get(innerClass));
                        }
                    }
                    res.append("| " + field.getCollectionClassAsString(classes, innerClasses, currentDocument)
                            + " <br>" + field.getDeprecatedAsString()
                            + field.getSinceAsString() + " | " + field.getDescriptionAsString() + " |\n");
                } else {
                    //The field is a primitive class and we must print only the name of the class
                    res.append("| " + field.getNameClassAsString() + " <br>" + field.getDeprecatedAsString()
                            + field.getSinceAsString() + " | " + field.getDescriptionAsString() + " |\n");
                }

                //If the type of the class is among those that we want to document, we add it to the list of related table models to print
                // it later.
                if (classes.get(field.getType()) != null) {
                    if ((!printedTableModels.contains(classes.get(field.getType())))
                            && (!options.getNoPrintableClasses().contains(field.getType()))) {
                        if (String.valueOf(field.getType()).endsWith("Internal")) {
                            internalTableModels.add(classes.get(String.valueOf(field.getType())));
                        } else {
                            relatedTableModels.add(classes.get(String.valueOf(field.getType())));
                        }
                    }
                    tablemodels.add(classes.get(String.valueOf(field.getType())));
                }
            }

            res = printRelatedTableModels(res, relatedTableModels, internalTableModels);
        }
        return res;
    }

    private static StringBuffer printRelatedTableModels(StringBuffer res, Set<MarkdownDoc> relatedTableModels,
                                                        Set<MarkdownDoc> internalTableModels) {

        for (MarkdownDoc tableModel : relatedTableModels) {
            if (tableModel != null && !printedTableModels.contains(tableModel)) {
                printedTableModels.add(tableModel);
                res = getTableModel(tableModel, tableModel.getName(), res);
            }
        }
        for (MarkdownDoc internal : internalTableModels) {
            res = getTableModel(internal, currentDocument, res);
        }
        return res;
    }

    private static String getPackageAsPath(MarkdownDoc doc) {
        String spackage = doc.getQualifiedTypeName();
        if (spackage.contains("$")) {
            spackage = spackage.substring(0, spackage.lastIndexOf("$"));
        }
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

    public String getCurrentDocument() {
        return currentDocument;
    }
}
