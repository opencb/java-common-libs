package org.opencb.commons.docs.doc.markdown;

import org.opencb.commons.docs.DocUtils;
import org.opencb.commons.docs.config.DocConfiguration;
import org.opencb.commons.docs.doc.Doc;
import org.opencb.commons.docs.models.DataClassDoc;
import org.opencb.commons.docs.models.DataFieldDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MarkdownDoc extends Doc {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownDoc.class);
    private final Map<String, DataClassDoc> relatedClasses = new HashMap<>();
    private final Set<String> printedTableModels = new HashSet<>();
    // private final Set<DataClassDoc> relatedTableModels = new HashSet<>();

    public MarkdownDoc(DocConfiguration config) {
        super(config);
    }

    private StringBuffer getTableModel(DataClassDoc doc, String fileName) {
        StringBuffer res = new StringBuffer();
        List<DataFieldDoc> fields = doc.getDataFieldDocs();
        printedTableModels.add(doc.getName());
        res.append("\n");
        if (doc.getClazz().isEnum()) {
            res.append("### Enum " + doc.getName() + "\n");
            res.append("_Enumeration class._\n");
        } else {
            res.append("### " + doc.getName() + "\n");
        }
        //Create link for github Java code
        res.append("You can find the Java code [here](" + getUrlPrefix(config.getGithubServerURL(), doc) + "src/main/java/"
                + MarkdownUtils.getPackageAsPath(doc) + ".java).\n\n");

        //For each field we make its table row
        if (fields.size() > 0) {
            res.append("| Field | Description |\n| :---  | :--- |\n");
            for (DataFieldDoc field : MarkdownUtils.sortByFlags(fields)) {
                //  if (!field.isPrimitive() && (!field.getClazz().isEnum()) && (field.isNavigate())) {
                res.append("| " + MarkdownUtils.getProcessedName(field, currentDocument.toLowerCase(), config)
                        + " <br>" + MarkdownUtils.getDeprecatedAsString(field)
                        + MarkdownUtils.getSinceAsString(field) + " | "
                        + MarkdownUtils.getDescriptionAsString(field) + " |\n");
                if (!field.isPrimitive() && (!field.getClazz().isEnum())) {
                    if (!field.isCollection()) {
                        printRelatedTable(field.getClazz(), field);
                    } else {
                        final List<Class<?>> genericClasses = field.getGenericClasses();
                        for (Class<?> genericClass : genericClasses) {
                            printRelatedTable(genericClass, field);
                        }
                    }
                }
            }

        }
        return res;
    }

    private String getUrlPrefix(String githubServer, DataClassDoc doc) {
        String res = githubServer;
        if (doc.getClazz().getCanonicalName().contains("biodata")) {
            res = "https://github.com/opencb/biodata/tree/develop/biodata-models/";
        }
        if (!res.endsWith("/")) {
            res = res + "/";
        }

        return res;
    }

    private boolean checkDocumentedClass(String className) {
        boolean res = false;
        for (Class<?> clazz : config.getDocClasses()) {
            if (clazz.getCanonicalName().equals(className)) {
                return true;
            }
        }
        return res;
    }

    private void printRelatedTable(Class<?> clazz, DataFieldDoc field) {
        if (!config.getDocClasses().contains(clazz) && DocUtils.isUncommentedClass(field, clazz)) {
            final DataClassDoc classDoc = buildClassDoc(clazz);
            classDoc.setName(clazz.getSimpleName());
            relatedClasses.put(clazz.getCanonicalName(), classDoc);
        }
    }

    private StringBuffer printRelatedTableModels() {
        StringBuffer res = new StringBuffer();
        Set<DataClassDoc> auxRelatedTableModels = new HashSet<>(relatedClasses.values());
        for (DataClassDoc tableModel : auxRelatedTableModels) {
            if (tableModel != null && !printedTableModels.contains(tableModel.getName())) {
                printedTableModels.add(tableModel.getName());
                res.append(getTableModel(tableModel, tableModel.getName()));
            }
        }
        if (existsRelatedTablesToPrint()) {
            res.append(printRelatedTableModels());
        }

        return res;
    }

    private boolean existsRelatedTablesToPrint() {
        for (DataClassDoc tableModel : relatedClasses.values()) {
            if (tableModel != null && !printedTableModels.contains(tableModel.getName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String getOverview(DataClassDoc doc) {
        StringBuffer res = new StringBuffer();
        res.append("# " + doc.getName() + "\n");
        res.append("## Overview\n" + doc.getDescription() + "\n");
        return res.toString();
    }

    @Override
    public String getDataModel(DataClassDoc doc) {
        StringBuffer res = new StringBuffer();
        res.append("## Data Model\n");
        printedTableModels.clear();
        //relatedTableModels.clear();
        relatedClasses.clear();
        res.append(getTableModel(doc, currentDocument));

        return res.toString();
    }

    @Override
    public String getSummary(DataClassDoc doc) {
        StringBuffer res = new StringBuffer();
        res.append("### Summary \n");
        res.append("| Field | Create | Update | Unique | Required|\n| :--- | :---: | :---: |:---: |:---: |\n");
        res.append(getRowTicks(doc.getDataFieldDocs()));
        res.append("\n");
        return res.toString();
    }

    public String getRowTicks(List<DataFieldDoc> fields) {
        String res = "";
        for (DataFieldDoc field : MarkdownUtils.sortByFlags(fields)) {
            res += "| " + field.getName() + " | " + MarkdownUtils.getFlag((!field.isManaged() && !field.isImmutable())
                    || (field.isRequired())) + " | "
                    + MarkdownUtils.getFlag(!field.isManaged()) + " |"
                    + MarkdownUtils.getFlag(field.isUnique()) + " | "
                    + MarkdownUtils.getFlag(field.isRequired()) + " |\n";
        }
        return res;
    }

    @Override
    public String getRelatedTables(DataClassDoc doc) {
        return printRelatedTableModels().toString();
    }

    @Override
    public String getExample(DataClassDoc doc) {
        StringBuffer res = new StringBuffer();
        if (config.getJsondir() != null && !config.getJsondir().endsWith(File.separator)) {
            config.setJsondir(config.getJsondir() + File.separator);
        }
        LOGGER.info("GETTING JSON ::: " + config.getJsondir() + currentDocument + ".json");
        String json = MarkdownUtils.getFileContentAsString(config.getJsondir() + currentDocument + ".json");
        if (json != null) {
            res.append("## Example\n");
            res.append("This is a full JSON example:\n");
            res.append("```javascript\n" + json + "\n```");
        } else {
            LOGGER.warn("Not available JSON ::: " + config.getJsondir() + currentDocument + ".json");
        }
        return res.toString();
    }


    @Override
    public void writeDoc(DocConfiguration config) throws IOException {
        for (Class clazz : config.getDocClasses()) {
            DataClassDoc classDoc = buildClassDoc(clazz);
            write(getDoc(classDoc), config.getOutputDir() + classDoc.getName().toLowerCase() + ".md");
        }
    }

    public void write(String doc, String fileName) throws IOException {
        File f = new File(fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(doc);
        writer.close();

    }
}
