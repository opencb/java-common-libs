package org.opencb.commons.docs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownUtils {

    public static Map<String, String> getInnerClass(String name, String sourceFilePath, String className) {

        Map<String, String> res = new HashMap<>();
        String content = getFileContentAsString(sourceFilePath);
        String reg = className + "<(.*)>\\s" + name;
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(content);
        if (m.find()) {
            p = Pattern.compile("<(.*)>");
            Matcher m2 = p.matcher(m.group(0));
            if (m2.find()) {
                //ya tenemos el nombre de la clase
                String clase = m2.group(0).replaceAll("<", "").replaceAll(">", "");
                if (clase.contains(",")) {
                    String[] clases = clase.split(",");
                    for (String clas : clases) {
                        res.put(clas.trim(), getPackageClass(clas.trim(), content));
                    }
                } else {
                    res.put(clase.trim(), getPackageClass(clase.trim(), content));
                }
            }
        }

        //System.out.println("JFS RESULTADO::::: " + res);
        //System.out.println("JFS *****************************************************************\n\n\n");

        return res;
    }

    private static String getPackageClass(String clase, String content) {
        String res = clase;

        //probamos con los import
        Pattern p = Pattern.compile("import .*" + clase);
        Matcher m3 = p.matcher(content);
        if (m3.find()) {
            //System.out.println("JFS = qualifiedName " + m3.group(0));
            res = m3.group(0).replaceAll("import ", "");
        } else {
            //si no está en los import es que es del mismo paquete
            p = Pattern.compile("package .*;");
            Matcher m4 = p.matcher(content);
            if (m4.find()) {
                //System.out.println("package " + m4.group(0) + "." + clase);
                res = m4.group(0).replaceAll(";", "." + clase).replaceAll("package ", "");
            }
        }
        return res;
    }

    /**
     * Read file content and returns it as Strings.
     *
     * @param path The file path.
     * @return the content of the file as string
     */
    public static String getFileContentAsString(String path) {
        String res = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            StringBuilder out = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                out.append(line + "\n");
            }
            reader.close();
            res = out.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
