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
    private static Options options = new Options();
    private static Map<String, MarkdownDoc> classes = new HashMap<>();
    private static Set<MarkdownDoc> tablemodels = new HashSet<>();
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

    private static Map<String, MarkdownDoc> createMap(ClassDoc[] classes) {
        LOGGER.info("Creating a Map with classes of the data model");
        Map<String, MarkdownDoc> res = new HashMap<>();
        for (ClassDoc doc : classes) {
            res.put(String.valueOf(doc), new MarkdownDoc(doc));
        }
        return res;
    }

    public static void printDocument() {
        LOGGER.info("Printing markdowns representing the data model");
        // //System.out.println("Printing markdowns representing the data model");
        //System.out.println("Printing markdowns " + options.getClasses2Markdown());

        for (MarkdownDoc doc : classes.values()) {

            if (options.getClasses2Markdown().contains(doc.getQualifiedTypeName())) {

                currentDocument = doc.getName();
                StringBuffer res = new StringBuffer();
                res.append("# " + currentDocument + "\n");
                res.append("## Overview\n" + doc.getDescription() + "\n");
                res.append(generateFieldsAttributesParagraph(doc.getFields(), doc.getQualifiedTypeName()));
                res.append("## Data Model\n");
                res = getTableModel(doc, doc.getFields(), currentDocument, res);
                if (options.getJsonMap().keySet().contains(currentDocument + ".json")) {
                    res.append("## Example\n");
                    res.append("This is a full JSON example:\n");
                    res.append("```javascript\n" + options.getJsonMap().get(currentDocument + ".json") + "\n```");
                }
                try {
                    write2File(options.getOutputdir() + currentDocument + ".md", res.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String generateFieldsAttributesParagraph(List<MarkdownField> fields, String className) {
        StringBuffer res = new StringBuffer();
        //System.out.println("Checking " + fields.size() + " " + className);
        if ((options.getTableTagsClasses().contains(className)) && (fields.size() > 0)) {
            res.append("### Summary \n");
            res.append("| Field | unique | required | create| updatable|\n| :--- | :---: | :---: |:---: |:---: |\n");
            if (options.getTableTagsClasses().contains(className)) {
                for (MarkdownField field : fields) {
                    res.append("| " + field.getName() + " | " + getFlag(field.isUnique()) + " | "
                            + getFlag(field.isRequired()) + " |" + getFlag(field.isCreate()) + " | "
                            + getFlag(field.isUpdatable()) + " |\n");
                }
            }
        }
        res.append("\n");
        return res.toString();
    }

    private static String generateFieldsAttributesParagraph(MarkdownDoc doc) {
        StringBuffer res = new StringBuffer();
        res.append("### Fields without tags \n");
        res.append("`");
        res.append(doc.getNotTagedFieldAsString());
        res.append("`");

        res.append("\n### Fields for Create Operations \n");
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

    private static StringBuffer getTableModel(MarkdownDoc doc, List<MarkdownField> fields, String fileName, StringBuffer res) {
        // StringBuffer res = new StringBuffer();

        LOGGER.info("Generating tables of fields in the data model markdowns for class " + doc.getName());
        Set<MarkdownDoc> relatedTableModels = new HashSet<>();
        res.append("### " + fileName + "\n");
        res.append("You can find the Java code [here](" + options.getGithubServer() + "src/main/java/"
                + getPackageAsPath(doc.getQualifiedTypeName()) + ".java).\n\n");
        if (fields.size() > 0) {
            res.append("| Field | Tags | Description |\n| :--- | :--- | :--- |\n");
            for (MarkdownField field : fields) {
                if (isModel(field.getType())) {
                    //System.out.println("LA CLASE ES UN MODELO " + field.getClassName());
                    res.append("| " + field.getNameLinkedClassAsString(currentDocument) + " <br>" + field.getSinceAsString() + " |"
                            + field.getConstraintsAsString() + " | " + field.getDescriptionAsString() + " |\n");
                } else if (field.isCollection()) {
                    String sourceFilePath = options.getSourceClassesDir()
                            + doc.getQualifiedTypeName().replaceAll("\\.", File.separator) + ".java";
                    Map<String, String> innerClasses = MarkdownUtils.getInnerClass(field.getName(), sourceFilePath, field.getClassName());
                    for (String innerClass : innerClasses.values()) {
                        if (classes.containsKey(innerClass)) {
                            relatedTableModels.add(classes.get(innerClass));
                        }
                    }
                    //System.out.println("LA CLASE ES UNA COLECCION Y ES " + field.getClassName());
                    res.append("| " + field.getCollectionClassAsString(classes, innerClasses, currentDocument)
                            + " <br>" + field.getSinceAsString() + " |"
                            + field.getConstraintsAsString() + " | " + field.getDescriptionAsString() + " |\n");
                } else {
                    //System.out.println("LA CLASE ES " + field.getClassName());
                    res.append("| " + field.getNameClassAsString() + field.getSinceAsString() + " |"
                            + field.getConstraintsAsString() + " | " + field.getDescriptionAsString() + " |\n");
                }
                if (classes.get(field.getType()) != null) {
                    if ((!tablemodels.contains(classes.get(field.getType())))
                            && (!options.getNoPrintableClasses().contains(field.getType()))) {
                        relatedTableModels.add(classes.get(String.valueOf(field.getType())));
                    }
                }
                tablemodels.add(classes.get(String.valueOf(field.getType())));
            }

            for (MarkdownDoc tableModel : relatedTableModels) {
                if (tableModel != null) {
                    res = getTableModel(tableModel, tableModel.getFields(), tableModel.getName(), res);
                }
            }
        }
        return res;
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

    public String getCurrentDocument() {
        return currentDocument;
    }
}
