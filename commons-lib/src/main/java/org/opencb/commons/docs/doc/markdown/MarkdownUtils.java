package org.opencb.commons.docs.doc.markdown;

import org.apache.commons.lang3.ObjectUtils;
import org.opencb.commons.docs.DocUtils;
import org.opencb.commons.docs.config.DocConfiguration;
import org.opencb.commons.docs.models.DataClassDoc;
import org.opencb.commons.docs.models.DataFieldDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MarkdownUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownUtils.class);

    public static String getFlag(boolean flag) {
        return "<img src=\"https://github.com/opencb/opencga/blob/develop/docs/data-models/"
                + (flag ? "yes" : "no") + ".png?raw=true\">";
    }

    public static List<DataFieldDoc> sortByFlags(final List<DataFieldDoc> dataFieldDocList) {

        Collections.sort(dataFieldDocList, new Comparator<DataFieldDoc>() {

            @Override
            public int compare(DataFieldDoc fieldDoc, DataFieldDoc fieldDoc1) {
                String sfield = getAsString(fieldDoc);
                String sfield1 = getAsString(fieldDoc1);
                if (fieldDoc.getName().equals("id")) {
                    sfield = "aaaa";
                } else if (fieldDoc1.getName().equals("id")) {
                    sfield1 = "aaaa";
                }
                return sfield.compareTo(sfield1);
            }

            private String getAsString(DataFieldDoc fieldDoc1) {
                String res = "";
                res += fieldDoc1.isUnique() ? "a" : "b";
                res += !fieldDoc1.isManaged() ? "a" : "b";
                res += !fieldDoc1.isImmutable() ? "a" : "b";
                res += fieldDoc1.isRequired() ? "a" : "b";

                return res;
            }

        });
        return dataFieldDocList;
    }


    public static String getPackageAsPath(final DataClassDoc doc) {
        String spackage = doc.getClazz().getCanonicalName();
        if (spackage.contains("$")) {
            spackage = spackage.substring(0, spackage.lastIndexOf("$"));
        }
        return spackage.replaceAll("\\.", File.separator);
    }

    public static String getProcessedName(DataFieldDoc fieldDoc, String currentDocument, DocConfiguration config) {
        String res = "";
        String link = generateLink(fieldDoc, currentDocument, fieldDoc.getClazz().getSimpleName(), config);


        if (fieldDoc.isDeprecated()) {
            res += "**~~" + fieldDoc.getName() + "~~**<br>*" + link + "*";
        } else {
            res += "**" + fieldDoc.getName() + "**<br>*" + link + "*";
        }

        return res;
    }

    public static String camelToKebabCase(String str) {
        return str.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }

    public static String getSinceAsString(DataFieldDoc fieldDoc) {

        String res = "";
        if (fieldDoc.getSince() != null && fieldDoc.getSince().length() > 0) {
            res += "<br>_since_: " + fieldDoc.getSince();
        }

        return res;
    }

    public static String getDeprecatedAsString(DataFieldDoc fieldDoc) {

        String res = "";
        if (fieldDoc.isDeprecated()) {
            res += "<br>_Deprecated_";
        }

        return res;
    }

    public static String getDescriptionAsString(DataFieldDoc fieldDoc) {
        String res = "<p>" + fieldDoc.getDescription() + "</p>";
        return res.replaceAll("\\n", "<br>");
    }

    private static String generateLink(DataFieldDoc fieldDoc, String currentDocument, String clazz, DocConfiguration config) {


        if (!config.getGitbookServerURL().endsWith("/")) {
            config.setGitbookServerURL(config.getGitbookServerURL() + "/");
        }
        if (config.getDocClasses().contains(fieldDoc.getClazz())) {
            String link = fieldDoc.getClazz().getSimpleName();

            return "[" + link + "](" + config.getGitbookServerURL() + link.toLowerCase() + ")";
        }

        if (fieldDoc.isPrimitive() || fieldDoc.isEnumeration()
                || (DocUtils.isUncommentedClass(fieldDoc, fieldDoc.getClazz()))
                || (fieldDoc.isCollection() && ObjectUtils.isEmpty(fieldDoc.getGenericClasses()))) {

            return fieldDoc.getClazz().getSimpleName();
        }

        if (fieldDoc.isCollection()) {
            if (fieldDoc.getGenericClasses().size() > 0) {
                if (DocUtils.isMap(fieldDoc.getClazz())) {
                    String res = fieldDoc.getClazz().getSimpleName() + "<";
                    res = getInnerClassesOfMapAsString(fieldDoc, currentDocument, config, res, 0);
                    return res;
                } else {
                    String res = fieldDoc.getClazz().getSimpleName() + "<";
                    String link = fieldDoc.getGenericClasses().get(0).getSimpleName();
                    if (config.getDocClasses().contains(fieldDoc.getGenericClasses().get(0))) {
                        res += "[" + link + "](" + config.getGitbookServerURL() + link.toLowerCase() + ")";
                    } else {
                        res += "[" + link + "](" + config.getGitbookServerURL() + currentDocument + "#"
                                + link.toLowerCase() + ")";
                    }
                    res += " >";
                    return res;
                }
            }
        }

        return "[" + clazz + "](" + config.getGitbookServerURL() + currentDocument + "#"
                + clazz.toLowerCase() + ")";


    }

    private static String getInnerClassesOfMapAsString(DataFieldDoc fieldDoc, String currentDocument,
                                                       DocConfiguration config, String res, int index) {

        List<Class<?>> cls = fieldDoc.getGenericClasses();
        for (int i = index; i < cls.size(); i++) {
            if (DocUtils.isMap(cls.get(i))) {
                if (i + 1 < cls.size()) {
                    return res + getInnerClassesOfMapAsString(fieldDoc, currentDocument,
                            config, res, i + 2);
                } else {
                    return res + "Map";
                }
            } else {
                String link = cls.get(i).getSimpleName();
                if (DocUtils.isSimpleType(cls.get(i)) || DocUtils.isUncommentedClass(fieldDoc, cls.get(i))) {
                    res += link + ",";
                } else {


                    if (config.getDocClasses().contains(cls.get(i))) {
                        res += "<a href=\"" + config.getGitbookServerURL() + link.toLowerCase() + "\"><em>"
                                + link + "</em></a>,";

                    } else {
                        res += "<a href=\"" + config.getGitbookServerURL() + currentDocument + "#"
                                + link.toLowerCase() + "\"><em>" + link + "</em></a>,";

                    }
                }
            }
        }
        if (res.endsWith(",")) {
            res = res.substring(0, res.length() - 1);
        }
        res += ">";
        return res;
    }

    /**
     * Read file content and returns it as Strings.
     *
     * @param path The file path.
     * @return the content of the file as string
     */
    public static String getFileContentAsString(String path) {
        String res = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(path));
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line + "\n");
                }
                reader.close();
                res = out.toString();
            }
        } catch (Exception e) {
            res = null;
        }
        return res;
    }

}
