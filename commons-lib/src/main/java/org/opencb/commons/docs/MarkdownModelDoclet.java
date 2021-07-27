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
    private static Set<MarkdownDoc> internalTableModels = new HashSet<>();
    private static Set<MarkdownDoc> relatedTableModels;

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
                Set<MarkdownDoc> printedTableModels = new HashSet<>();
                //System.out.println("Creating " + doc.getQualifiedTypeName() + " data model");
                currentDocument = doc.getName();
                StringBuffer res = new StringBuffer();
                res.append("# " + currentDocument + "\n");
                res.append("## Overview\n" + doc.getDescription() + "\n");
                res.append(generateFieldsAttributesParagraph(doc.getFields(), doc.getQualifiedTypeName()));
                res.append("## Data Model\n");
                res = getTableModel(doc, currentDocument, res);
                printedTableModels.add(doc);
                if (relatedTableModels != null) {
                    for (MarkdownDoc tableModel : relatedTableModels) {
                        if (tableModel != null && !printedTableModels.contains(tableModel)) {
                            printedTableModels.add(tableModel);
                            res = getTableModel(tableModel, tableModel.getName(), res);
                        }
                    }
                }
                for (MarkdownDoc internal : internalTableModels) {
                    res = getTableModel(internal, currentDocument, res);
                }
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
        if ((options.getTableTagsClasses().contains(className)) && (fields.size() > 0)) {
            Map<String, List<MarkdownField>> mfFields = classifyFields(fields);
            res.append("### Summary \n");
            res.append("| Field | create | update | unique | required|\n| :--- | :---: | :---: |:---: |:---: |\n");
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

    private static String getFlag(boolean flag) {
        String res = "";
        if (flag) {
            res += "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAABmJLR0QA/wD/AP"
                    + "+gvaeTAAAL5ElEQVR4nO2dWZBcZRXHf"
                    + "+f29CQhIRuLkkowPISQRQhJoJAgJJgCYbqnu2foCVjAg5bsVgFVAgqUAUURlVIImy8iWAWZgenu2aBSYAYESjQbEmMCVRIIhRgLE0yGZKa77"
                    + "/FhpjEVIn3v7bv0cn8v8/J955yp/7"
                    + "+/u30LhISEhISEhISEhISEhISENAoSdAFekO5MR4bHDZ9kqpyMaZyicLKIzkZ0iqhMVJgITGP0L8AQsEdgSEWHUPlYVXYKvIVhbi+q7JiUj"
                    + "+7s6ugqBvdfeUNdGOCigYsmR0aaz1PTWCGiy4EFQLPLaUaArcCgqKwvjD/48nMXP/cfl3P4Ts0aoDXXeqppGpcKnK+wFIj4XEJBYCMqLxYN8+mBZO5Nn"
                    + "/O7Qk0Z4MLO9PSmaP4SRK8UlWVB13MoCtsQfUJHmn870NH1YdD1WKUmDNCSTawUlZuAC4CmoOspQwFYB9zfl8q"
                    + "+GHQx5aheAygSyyViwPdROSvochyyWVV+0p/KPIOgQRdzJKrPAKPCd4jK7QpfDrocl3hDVe6pRiNUlQFac61zTdNYA6wMuhaP"
                    + "+IOqXNffltkadCElqsIA8d74UVqI3AJ8D/cf36qNAqIPG6J39CR69gVdTOAGiHenYir6CDAz6Fp8ZpeaxrX97d39QRYRmAGWr1"
                    + "/eNGnv1DuAOwEjqDoCRhF9cMJI83e7OrpGgiggEAPEe+MnaiHyNPCVIPJXGwobMCOr+tuf/bvfuX3/5cUzyaQWIm8Qiv8pAkvFKG5oybW2"
                    + "+p3bVwPEulPXKzwLTPUzb40wTUwjG+tO3exnUt8M0JJN3IroGj9z1iCC6C/i3al7/UvoMenOdOSTpsJDInq117nqCYXHh6bu"
                    + "/fbgisGCl3k8NUC6Mx052Jx/SpW0l3nqFYXOo/LRb3g5D8G74ViRA80jj4TiO0eg42A0/7CXOTz7hh4/LXUPcKNX8RuIJXMum2u8/fSOQS"
                    + "+Ce3IJiGcT16nKQ17EbmBu7Etlf"
                    + "+V2UNcNEM8kk2OPeuHdvruYiCb7krleN4O6aoCxN3ybgeluxg35lD1NsDibyu50K6Brv9Ilj10VHXu9G4rvHdMKsDbdmXbti6lrBphx/O77CF/v"
                    + "+sGZB5pHfuRWMFcuAbFsIo5Kzq14IWVRRGN9ydxApYEqFmxsMsc24EuVxgqxxa4J+ej8ro6u/ZUEqfwSUIjcTSh"
                    + "+EMw60Dxye6VBKhoB4j3xBRQjmxWilRYS4ogRVVnU35b5m9MAzkcARbQYWROKHyjNBjyKOv8hOzZALJfoAJY77R/iDip6bksmlXLa35kBFBGViq8"
                    + "/jYh48KAkonc6HQUcGSCeSbXW0aIN35g/41i+de7pTIi6ftVcFMslLnLS0ZEBVPQWJ/0amfkzjqV96QJmTp"
                    + "/MFctO9cIEdzrpZNsALdnESuBsJ8kalZL4EWN0lJ4x9Wj3TaByVkuudYXdbrYNMLZKN8Qih4tfwgsTiGnYnlBq68ahNdf6BdM03qf6l2hXBf9P"
                    + "/EP5YO8+nnz1LxzI591IWTAMc2ZPouefVjvYGgGKpnE5ofiWsCI+uD4SNJmmcamdDrYMIHCFvXoaE6vil3DZBLY0smyAlu7UQuA02"
                    + "+U0GHbFLzFj6tEsPekEN0pYcnE2YfkR3bIBDLjMWT2Ng1PxAba89yGvvP2eK3VEVCzPxLZsABWt100bXKFS8XObt6Mu7R2i8DWrbS1Vu7IzPWV8NP8R"
                    + "/m/FVhNUk/hjFIrjho+xso+hpRFgfPPIuYTiH5EqFB+gKTLSfI6VhpYMoKZh"
                    + "+w1TI1Cl4gPWNbNkgLHtV0MOoZrFBxBRSwYoW326Mx05EM0PAeMqrqpOqHbxxxiekI9OLLewtOwIMDxu+CRC8T+lRsQHGHdw/METyzUqa4BioWmuO"
                    + "/XUPjUkPmBNu7IGENHQANSe+ACGBe1CA1igFsUHEKjcANrgc/5rVXwAhdnl2pR/DBSd4kYxtUgtiw"
                    + "+gopPLtSlvAJVJrlTjkFnTJzPvhON8z1vr4gOIaRxdro2VF0Flg3jFrOmTufzsU+k4cz4LZx7vW956EB8A0bLaWZndE4gBSuKPaxotsX3JPAC2vr"
                    + "/b07x1I/4orowAvl8CDhcfQERoXzLP05GgzsQHlwzgK0cSv4SXJqhD8S1hxQAVrT+3w+eJX8ILE9Sx"
                    + "+GUPpLBiAF9OtbAifgk3TVDH4kOtGOCYSRMsi19CREgtnsf8Gcc6zlvn4oNKbRjg3/sPOrq7jxhC+owFjkaCuhcfUMN0xQCen4"
                    + "+rKH1b3mbjzg9s93VyOWgE8QFEpfI5gQLvulPO5+OXCRpFfACBneXalP8YpLLDlWos4LUJGkl8AIWy2lWVAcA7EzSa"
                    + "+ACmBe3KGsA0zO3ulGMdt03QiOIDNIuW1a7sc9ekfHTngWh+GJ/nBZZMALBk9gxbfUsmADBNsyHFR/RgdKR5V9lmVmLFMslNwOkVF"
                    + "+UAEUicfgqLTvyi7b5Fc1S5hhN/lI19qezSco2sfgtYX2ExjlGF3KYdji4HEUMaVXwQ"
                    + "/b2VZtYWhqgEZgCo7J7ALnUhPtY1s2aASPElwNPjy8rhhwnqRXygIIb5ipWGlgzQk+jZJ7Cxspoqx0sT1JH4qOjrVo"
                    + "+mtzwfwBR9wXlJ7uGFCepJfABDxdL1H+zsEGKYTzkrx33cNEG9iQ9gqnRabWvZAL2tvX8FtjiqyAPcMEE9iq"
                    + "+wob8ts9Vqe3tTwlSetF2Rh1RignoUH8CwqZEtA0ThdwT8NHA4TkxQr+IDBYkU19rpYMsAmbbMbmCdrZJ8wI4J6lh8gOfs7BIKzmYF3++gj"
                    + "+dYMUGdiw8OtHF0yEAsk3yVKt0xXBBii+Z85gNS3Ysv+se+ZM72uY3O1gWI3uuonw8caSSoe/EBUbnbUT9H2RSJZZMbgMWO"
                    + "+vtAaSSIGEbdiw9s6UtmFyPY/i8djgCoqlTtKABjI8EbbzWC+Ajc5UT8sb4OUSSeSb2oFrcjC/GMl"
                    + "/qS2RVODeB8baCghmHeIODKSQchjhhRlWudig8VLg7NJXPbVPSXlcQIcY6o/LySU0PBhdXBEjFXq4X55yGu897wxKEfVxqkYgP0xns"
                    + "/wTRuAOfDUIht1FS5et2F64YqDeTK/gD97d39UqVvCOsRUblvoC3zvBuxXNsgYt/UvbcBr7kVL+TICLz+wb+Oc3RI5JFwzQCDKwYLEdFLgY"
                    + "/cihnyGfYUTWPVxqt/7dqTl6tbxOSSuV1qmN8ETDfjhgBQRPTygfZuVxfrur5HUH+ipweV692O2+go3NSXzA24HdeTY2DeWrt9w5zL5hqCnOdF"
                    + "/EZDVH7Q15b9mSexvQhaIp5JPqDwHS9z1Duq8lh/W+Yar+J7uk3c+Hz0JgXLM1RDPsPaowpNnl5OPR0BYOzImeaRNah45uK6ROU3"
                    + "+6ftuWpwxaCnczA9N0CJlmziVqnyT8jVgqj8tLctc5svufxIUiKeTVynKg9ShTuUVglFVG7oa8s86ldCXw0AEMsm4qg8Dkz3O3eV8xGiV3rxqPd5"
                    + "+G4AgEQ2MasAT4nKsiDyVyF/lkhxVW9r7zt+Jw5kKM4lc7uGpny8HLiLxn5rqIg+8I/dxy8LQnwIaAQ4lFg2cTEqjwBlz7irM941Va5x66ueUwK"
                    + "/GetL5gakqTiP0dFgOOh6vEYgj+gDE/LRhUGLP1ZP9RB7pn0OkeIa4IKga"
                    + "/GIlyRSvH5spXVVUFUGKNHSnWoT0TsIaGcyD9gk8MPeVDYbdCGHU5UGKBHvTp2joncB5wddi0NeQ"
                    + "/TevkSur5KZu15S1QYo0ZJrXSGmcTPwdawddBUkBeB5Ncz7+xM9ge6uZoWaMECJCzvT05ui"
                    + "+UsQvbLa3iEobEP0iYjo43aXaAdJTRngUOI98QUUI6sUVgJn4P/IUAD"
                    + "+pKIvNMHaXDK3zef8rlCzBjiU1lzr0abKV0XlfIXlwELc39t4WOFNVAbFMNdPGGl+uaujy7cDtbyiLgxwOOnOdOSTiPklFfNkET1FROcKzFaVycBERs"
                    + "/Tm8r/zkTcD+wF9qnofgP2ofKOwg5TZUekqbBjyaYl765evbqR31qGhISEhISEhISEhISEhITUOP8FRj8ku0SbqC0AAAAASUVORK5CYII=\" "
                    + "width=\"16px\" heigth=\"16px\"/>";
        } else {
            res += "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAABmJLR0QA/wD/AP"
                    + "+gvaeTAAAO8klEQVR4nO2de4xcVR3Hv79zp9uy2"
                    + "+7so7UEEbda9lmq2AiGFl0EoaW07GtES9RGDSaS+IjxESRYaKIoDw2JRkENIj6w3dnSVrYFLVVaFUtNtGw7W7a0CFLB7mP2Rdude37"
                    + "+sTPb3dm5O3Nn7j3nzHY+CQlz595zvt3znd"
                    + "/vnnPPPYcwy2CAoq3VS4QU9UxYAkYVEy4BsAhAJQOVRJgHRhGAkvhlIyCcZcZpAnoB9ILoTZL8KggnAHqZyT4SbD96nADW9W"
                    + "/zA9ItIFdOharfHpC0kkErifF+AMsALPCpuiEGDgmmAwy5f8zG/kXbu1"
                    + "/3qS4l5J0BeGPVvMHo3EYpaA0xVgOo1isI3UTcCaJdpaOxvdTZc0arHpfkhQE41FA0FLOvt4k"
                    + "/Skw3g1CqW5MDUQKeBOh3pQuLn6aHD47pFpQOow0Qba27lCE/A6aNABbr1uOSN5jwhIR4uLL9cJduMU4YaYC"
                    + "+5upVJMTXibEWhmp0AwP7mfDd8vbITtNuIo354zJAAy11TUTyLjC9V7cen"
                    + "/gHM99T1tG93RQjGGGAaFP1GrbEZjBW6NaiiBeIcGewPbJbtxCtBhgM1dTYNj1AwFqdOjTyB8n05YqOIy/qEqDFAK+vW1FcEhjZzIQvAAjo0GAQYwB"
                    + "/f8ga2fSOLa+9pbpy5QYYaK27Dsw/AfAu1XUbTg+YP1fW0b1HZaXKDMAbq+ZFh+ZuAtNXAQhV9eYZDOCR0bGSL1+04"
                    + "+CoigqVGKCvtWa5xfRrBhpU1Jf3EA7ZEB9XMX7g+y8x2lr7ccH0l0Lju4BxmcXyQH9r7af8rsq3CMAhWIN2zf0M"
                    + "+pJfdZwnPBBcHvkabYL0o3BfDMChhqJoTP4SxB/1o/zzkI5g6ekN9OiJ014X7LkB3gw1zC+K2e0gXO912ec5z9pvxZoqO3sGvSzUUwMMr1"
                    + "+2OBaIPQXgfV6WWyAO4aAlx25c0HHsTe+K9Ij"
                    + "+ptoqEngGwFKvyiyQkpdY4vrybZETXhTmiQGGQksX2bHAcyDUeFFegXTQsYAlVs3f0vXfXEvKuRvYu2ZpqW0HdhUaXyX87pht7"
                    + "+5vem9ZriXlZAAONRRZFwS2opDzdbCcxOkwr1k6N5dCsjYAb4KISvk4gI/kIqBATlwTvcD6LYdgZVtA1gYY/FfNA2AOZXt9Aa+gpqhd8"
                    + "+2sr87momhL7QYGfpVtpQU8h5m5ubyj+0m3F7o2QF9rzXLB9FcAxW6vLeArvbYduLzyyRdfdXORqxTAG6vmCdDjKDS"
                    + "+iVRaVmwL37ZijpuLXBlgYHDefWBc5k5XAYVcGe0d3uzmgoxTQHwmz9NurvGJYYwb17QoNApAApivWQcz45ryjsifMjk5owjw"
                    + "+roVxWD8GPobf5TB6yXLGzBuBFMYBfM6AV4NYEizFgLwo0xTQUYGKAmMbAb43TnJyp1hKcTq8nD3sxUdR/exwDoAI5o1AcAIC6wt6+jeUxru3i"
                    + "+FWAvN5iRCfbR3OKN5GGl/0UPNl9bZZP0TgKubC48ZZfBN5eHuZycf7GuuXiVIdEJf2B0F87rkiZyDLTUrJagT"
                    + "/r2lnAmjDKovDx95ZaaT0kYAm6wHYWDjA0BFx9F9kuUa6PnFpWx8ACgNd+8X4DXQmw6KCXx/upNmNEC0qXoNgNWeSXLPRNh3OkFTOpgI"
                    + "+04nGJIO2gba6q+d6QRHAzBALMTd3mvKmFEGr6/Yevi5dCeWb43slSxXQ80fexTM68u3RvamO7Fi6"
                    + "+HndN8YspT3zPS9owEGWuqaALzfc0WZ4Rj2nVCUDhzDvhO60wEBV/W11V/t9L2jAYjkXf5ISkvasO+Ez+kgbdh3Qnc6ELa80+m7lL2A+KDPM"
                    + "/5JcsT1Lz8VPvQOXP/yU6Gzd0CQVwTDRw8kH08dAZi/4rui6XjS+IDn6cCTxgf0pgMG3ZHq+LQIEG2tu5SZu1N95yPDUogbM7nhc0N"
                    + "/W20jSezEueXg3DLCAjdlcsPnhr62+quFlE9B7fgFQ8YuLdvWc2zywWkRgFl+FmobP+O7fbfk2DvI+G7fLZp6BwQR+HTywSkGGB8/pk+q0"
                    + "+Rd2Hciy3TgWdh3QlM6+HTyM4IpBujvHVkN4EJFYnxv/AQuTeB74yfQYIILB/83PGVgaIoBSJKqd"
                    + "/mUNX6CDE2grPETKDcB0S2TP04YgDdWzSPwegUSsu7n50qacYKs+/m5onKcgMFNHGooSnyeMMBgdG6johU4hbBtbQ"
                    + "+XHG4MfbvhMw8qi0r5wcSnCQNIQWsUKSgG0Y6B5poPK6pvGknpQHnYT0Z5t5B54gHfhAHiCy+rohhE2"
                    + "/vbahsV1jmFRDrQFfYT9LfVNgoplc5pYMbEj52A+JLrtnhNlYBJaP/16UTTgBAAYA7oopLwkZMCAAK2tUq1gDja04EudDY"
                    + "+AIwRXwXEUwDHP2jivDOB7sYHAAJWAnEDxHfa0In2ewJV6Mj5qZCMKwBA8CYIwIiXPUpI4vezORL0tdVfTRI7kP3DKc8gYBkDRNFQw1K27Zd0C5"
                    + "rErLwxNCHsJ8OgKmFLu063kCRmXTowJewnI6RdLyzJS3QLScGsSQd9zdWrTAn7ybBFVQKgd+oW4kDe9w4MeHFlZqRYIuKbKppK3qaD/rbaRkFiF0xt"
                    + "/HHeKQC8TbeKNORdOjA57E+BeJEgoFK3jgzIm3RgfNifSqWQQIVuFRlifDrIk7A"
                    + "/mUpB5i20MBPGpoO8CfuTIRQLAEVpTzQL49JBnoX9czDm5qMBAINMkLeNP87cwuZN5zkCwFndIrLAmOcFmhepyJUz+WgAYxo"
                    + "/Qd6agHBGkBkLLWWKtqnb6TBs4arMYIwIBvp068gQ46duK16pxAt6BRindKvIAOPCvhN5lQ6YTwmQ8QYwNuw7kT"
                    + "/pQPQKYvxbt4wZMD7sO5EX6YDkCQHCCd06HMibsO+E8emAcEIQ47huHSnIu7DvhMnpgCSOCynkYd1CksjbsO"
                    + "+EqekgJud0EQMUba4dUPRmcDryPuzPhGHPDaLBcKRcEMBMeFG3GsyisO+ESemAQIcIYAEAgmna+nGKmXVh3wlT0gGDnwcS7wZC7teoZVaH"
                    + "/VSY0Dtg0H4gboAxG7oMcN41fgLdJphjib8CcQMs2t79OhjdijVoz/n9bbWNJixSAcX3BAR0JTaePrdCCGiXQg3ac35iDp/uOYY67gmY0Jn4"
                    + "/3MzggR3pjzbe7SH/aTumPbpZcrTwfjMZQCTDFA6GtsLIKqifiawinpS4dAX126CABEDKv4uPBAkmliW91wK6Ow5Q4DrvWezoJhAO"
                    + "/tbaq5RUNcU0gzEaDOB0mXkWYRpS9fELLCkSaH0O98FjKPcBBmOwik3gfI9BCx+YvLHKQYo7Vu8m4GTSoQoNIHLIVhlJtCwgcR"
                    + "/gxUlU1ZonbpW8N69MSJ+TJEYQIEJshx/990EWnYPYfoZPXxwbPKhae8FkAj8FEpuRiYoJtD2mTY2ypYc39Xz7V3Evrb6q+V4t1vl1jEMYf88"
                    + "+eA0AwS3dPUAyrqECeYLKXd5GQk8elfP83cRB1tqVgopfw"
                    + "/FTwQJ2FnWfvTl5OMOewbhAd8VTcezdODxY1fP0oHOTaNsIe5Lddxxa5iBltqDAN7nmyJncto"
                    + "/yIN9gpzIaf8gzauEPV8Wjnwg1RfOO4cyz7jjpI9knQ58fkU763SgK+wnICLHtpxxc6iBlrrnAb7Ce0kZ4WpXEYWzbVwNZRuwk/iBYDhyJTnc2M"
                    + "/4djAR69o9FHDRO1C8MkfGvQNNd/tTYf6GU+MDaQwQbI/sBvgp71VlTNp0oGlljrTpQHfYH4efSBep0q4PQFbgi9D7BrFj70DzJEvH3oEBYR8AhmJW"
                    + "+h1g0xogPi7wA280Zc20dGDIgkzT0oERYR8AEzYt3HL0P+nOy2iH0FdDF1+wwJ7"
                    + "/LwBLc1aWG6MMvomZxwyaXg3EbwwF4YwBv3wQ0FW6sOTy5GFfh3MzY6C55sMg"
                    + "+oOba3xiOK7BtNW4RgFI6DellEI0ZjqOkvEaQWUd3XuI6aHsdXnGfJjX+MD4cnu6Gx8A7nUziOZqkajS02NfB"
                    + "/BP15IKqOJvwYUlm9xc4MoA1NlzxiZxKwx4s6XANE7ZiIUyyfuTcb1MXGX74S4GfQJqHxkXmBlm8Gcqwz2ut/7Lap3A8vCRDgAPZnNtAV"
                    + "+4tzzcvT2bC7NeKDK4PPI1YOr8sgJa+G1weeTObC/OqUvHt62YEz01sgPADbmUUyBr9gTfit1InT1nsi0gp6Vi6eGDY2MxDoFwMJdyCmTFC2ct6+ZcGh"
                    + "/waFBncF31QhkQ+0Co8aK8AumgY4GYtXL"
                    + "+9hffyLUkTxaLLt1x9BSP7z5u0v6Ds5WXGLjWi8YHPB7WHWyurZSEnQBSTj8qkDMvWDy2dkHHsTe9KtDT5eJLOyK9Zy3rIwB2e1luAQDAHvut"
                    + "2LVeNj7g04MdDjUURW37FwA+5kf55x2EcHDB6Vvp0ROnvS7alw0jaEvX2eDyyK0Afw+FEcNcYADfCV4WCfnR"
                    + "+ICCR7sDrXXXgflxAIv9rmuWcYqIPhVsP+LrlDwlz"
                    + "/Z7W5ZeHMCc3zB4lYr68h/6O0u+pXxb5ITfNSnZM6gy3PNaad/iaxi4G+OTJgqkhpnwUHBh8SoVjQ9omN3T31z7IUH4IQMNqus2GsIhSeL2bN"
                    + "+Iyr5aDXBjY2Cg8uTtxLQZmufPGcAoA/eVWda3J6/coQqt8/v+t77mokAA99L4/ILzDibsZGHdXrGlS9ueDboneAIABtrqryXb"
                    + "/hYTeb5GgKH8DczfNGGBTCMMkKC/pe6DBL4Ds/bxMv2dCHf73bVzg1EGSNDfXP0eEH2FQBsAWLr15AgD+KMkPFTRHtmhW0wyRhogQe"
                    + "/Ny95hCXsDiD8P4BLdetzAwEkifgy2/UjZtp5juvU4YbQBEnBjY2Cw/OR1ILqFwU0AlenWlBJCPxjbiPBEae"
                    + "+Ff6S9e2O6JaUjLwwwGQ41FEVj8kMEXi2B1USo16mHgC4GdoFoV1CIP+voyuVC3hkgmeFQw4Uxaa8kYKVkXEHAMgBBn6qLEugQiA9IiX1z7MB"
                    + "+ryZm6CLvDZCK/qbaKgFZB2EtYUYVSF4C0NsAVAKoBKEYjADODUINgRADYxRAb/y/N8D0KoQ8ThLHJYkj5eEjr2j6J/nG"
                    + "/wGDZAh8IGekZQAAAABJRU5ErkJggg==\" width=\"16px\" heigth=\"16px\">";
        }

        return res;
    }

    private static StringBuffer getTableModel(MarkdownDoc doc, String fileName, StringBuffer res) {
        // StringBuffer res = new StringBuffer();
        List<MarkdownField> fields = doc.getFields();
        LOGGER.info("Generating tables of fields in the data model markdowns for class " + doc.getName());
        relatedTableModels = new HashSet<>();
        if (doc.isEnumeration()) {
            res.append("### Enum " + doc.getName() + "\n");
            res.append("_Enumeration class._\n");
        } else {
            res.append("### " + doc.getName() + "\n");
        }
        //Create link for github Java code
        res.append("You can find the Java code [here](" + options.getGithubServer() + "src/main/java/"
                + getPackageAsPath(doc.getQualifiedTypeName()) + ".java).\n\n");

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
                    if ((!tablemodels.contains(classes.get(field.getType())))
                            && (!options.getNoPrintableClasses().contains(field.getType()))) {
                        if (String.valueOf(field.getType()).endsWith("Internal")) {
                            internalTableModels.add(classes.get(String.valueOf(field.getType())));
                        } else {
                            relatedTableModels.add(classes.get(String.valueOf(field.getType())));
                        }
                    }
                }
                tablemodels.add(classes.get(String.valueOf(field.getType())));
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
