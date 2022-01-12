package org.opencb.commons.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import static org.fusesource.jansi.Ansi.Color.valueOf;
import static org.fusesource.jansi.Ansi.ansi;

public class PrintUtils {

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

    public static void println(String message) {
        System.out.println(message);
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

    public static String format(String message, Color color) {
        return ansi().fg(valueOf(color.toString())).a(message).reset().toString();
    }


    public static void printInfo(String message) {
        System.out.println(format("INFO: " + message, Color.GREEN));
    }

    public static void printDebug(String message) {
        println(message, Color.YELLOW);
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

    public static String eraseScreen() {
        return ansi().eraseScreen().toString();
    }
}
