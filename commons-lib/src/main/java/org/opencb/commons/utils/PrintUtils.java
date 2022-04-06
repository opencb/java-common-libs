package org.opencb.commons.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fusesource.jansi.Ansi.Color.valueOf;
import static org.fusesource.jansi.Ansi.ansi;

public class PrintUtils {

    public static void println(String message) {
        System.out.println(message);
    }

    public static void println() {
        System.out.println();
    }

    public static void print(String message) {
        System.out.print(message);
    }

    public static void print(String message, Color color) {
        System.out.print(format(message, color));
    }

    public static void println(String message, Color color) {
        System.out.println(format(message, color));
    }

    public static void println(String message, Color color, String message2, Color color2) {
        System.out.print(format(message, color));
        System.out.println(format(message2, color2));
    }

    public static String format(String message, Color color) {
        return ansi().fg(valueOf(color.toString())).a(message).reset().toString();
    }

    public static void printInfo(String message) {
        System.out.println(format("INFO: " + message, Color.GREEN));
    }

    public static void printDebug(String message) {
        println(message, Color.YELLOW);
    }

    public static void printGreen(String message) {
        print(message, Color.GREEN);
    }

    public static void printRed(String message) {
        print(message, Color.RED);
    }

    public static void printYellow(String message) {
        print(message, Color.YELLOW);
    }

    public static void printWarn(String message) {
        System.out.println(format("WARNING: " + message, Color.YELLOW));
    }

    public static void printWarn(Exception e) {
        System.out.println(format("WARNING: " + ExceptionUtils.getRootCauseMessage(e), Color.YELLOW));
    }

    public static void printError(String message) {
        printError(message, null);
    }

    public static void printError(Exception e) {
        printError("", e);
    }

    public static void printError(String message, Exception e) {
        if (e != null) {
            System.out.println(format("ERROR: " + message + "\n" + ExceptionUtils.getStackTrace(e), Color.RED));
        } else {
            System.out.println(format("ERROR: " + message, Color.RED));
        }
    }

    public static String getKeyValueAsFormattedString(String key, String value) {
        String res = format(key, Color.GREEN);
        res += format(value, Color.YELLOW);
        return res;
    }

    public static String getHelpVersionFormattedString(String key, String value) {
        String res = format(key, Color.YELLOW);
        res += format(value, Color.GREEN);
        return res;
    }

    public static void printCommandHelpFormattedString(String command, String info) {
        String key = format(command, Color.YELLOW);
        String value = format(info, Color.GREEN);
        System.out.printf("%30s  %s\n", key, value);
    }

    public static void printCommandHelpFormattedString(int kpad, int kvalue, String command, String info) {
        String key = format(command, Color.YELLOW);
        String value = format(info, Color.GREEN);
        System.out.printf("%" + kpad + "s  %" + kvalue + "s\n", key, value);
    }

    public static void printCommandHelpFormattedString(int pad, String command, String info) {
        String key = format(command, Color.YELLOW);
        String value = format(info, Color.GREEN);
        System.out.printf("%" + pad + "s  %s\n", key, value);
    }

    public static void printCommandHelpFormattedString(int pad, String command, String typ, String info) {
        String key = format(command, Color.YELLOW);
        String type = format(typ, Color.GREEN);
        String value = format(info, Color.GREEN);
        List<String> lines = null;
        if (info.length() > 80) {
            lines = getLines(info);
            //  value = format(info.substring(0, 80), Color.GREEN);
        }
        String print = "%" + pad + "s\t%s" + (type.equals("BOOLEAN") ? "" : "\t") + "%s\n";

        if (lines != null) {
            System.out.printf(print, key, type, format(lines.remove(0).trim(), Color.GREEN));
            for (String line : lines) {
                System.out.printf("%" + pad + "s %s\n", "   ", format(line, Color.GREEN));
            }
        } else {
            System.out.printf(print, key, type, format(value, Color.GREEN));
        }
    }

    public static void printAsTable(Map<String, String> map, Color firstColumn, Color secondColumn, int margin) {

        Map<String, String> formattedMap = new HashMap<>();
        int maxLength = 0;
        for (String key : map.keySet()) {
            if (format(key, firstColumn).length() > maxLength) {
                maxLength = format(key, firstColumn).length();
            }
            formattedMap.put(format(key, firstColumn), format(map.get(key), secondColumn));
        }
        maxLength += margin;

        String leftAlignFormat = " %-" + maxLength + "s  %s %n";

        for (String key : formattedMap.keySet()) {
            System.out.format(leftAlignFormat, key, formattedMap.get(key));
        }
    }

    private static List<String> getLines(String line) {
        String[] words = line.split(" ");
        List<String> rows = new ArrayList<String>();
        String row = "";
        for (String word : words) {
            if (row.length() + word.length() < 80) {
                row += " " + word;
            } else {
                rows.add(row);
                row = word;
            }
        }
        rows.add(row);
        return rows;
    }

    public static String eraseScreen() {
        return ansi().eraseScreen().toString();
    }

    public enum Color {
        BLACK("BLACK"),
        RED("RED"),
        GREEN("GREEN"),
        YELLOW("YELLOW"),
        BLUE("BLUE"),
        MAGENTA("MAGENTA"),
        CYAN("CYAN"),
        WHITE("WHITE"),
        DEFAULT("DEFAULT");

        private final String text;

        Color(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
