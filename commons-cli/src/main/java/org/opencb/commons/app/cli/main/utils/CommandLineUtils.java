package org.opencb.commons.app.cli.main.utils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;
import org.apache.commons.lang3.ArrayUtils;
import org.opencb.commons.utils.GitRepositoryState;
import org.opencb.commons.utils.PrintUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import static org.opencb.commons.utils.PrintUtils.printError;


public class CommandLineUtils {

    protected static final Logger logger = LoggerFactory.getLogger(CommandLineUtils.class);


    public static String getVersionString() {
        String res = PrintUtils.getKeyValueAsFormattedString("\tOpenCGA CLI version: ", "\t" + GitRepositoryState.get().getBuildVersion() + "\n");
        res += PrintUtils.getKeyValueAsFormattedString("\tGit version:", "\t\t" + GitRepositoryState.get().getBranch() + " " + GitRepositoryState.get().getCommitId() + "\n");
        res += PrintUtils.getKeyValueAsFormattedString("\tProgram:", "\t\tOpenCGA (OpenCB)" + "\n");
        res += PrintUtils.getKeyValueAsFormattedString("\tDescription: ", "\t\tBig Data platform for processing and analysing NGS data" + "\n");
        return res;
    }

    public static String getHelpVersionString() {
        String res = PrintUtils.getHelpVersionFormattedString("OpenCGA CLI version: ", "\t" + GitRepositoryState.get().getBuildVersion() + "\n");
        res += PrintUtils.getHelpVersionFormattedString("Git version:", "\t\t" + GitRepositoryState.get().getBranch() + " " + GitRepositoryState.get().getCommitId() + "\n");
        res += PrintUtils.getHelpVersionFormattedString("Program:", "\t\tOpenCGA (OpenCB)" + "\n");
        res += PrintUtils.getHelpVersionFormattedString("Description: ", "\t\tBig Data platform for processing and analysing NGS data" + "\n");
        return res;
    }

    public static boolean isNotHelpCommand(String[] args) {
        return !isHelpCommand(args);
    }

    public static boolean isHelpCommand(String[] args) {
        return ArrayUtils.contains(args, "--help") || ArrayUtils.contains(args, "-h");
    }

    public static boolean isValidUser(String user) {
        return user.matches("^[A-Za-z][A-Za-z0-9_\\-ñÑ]{2,29}$");
    }

    public static void error(String message) {
        printError(message);
    }

    public static void error(Exception e) {
        printError(e.getMessage());
    }

    public static void error(String message, Exception e) {
        if (e == null) {
            printError(message);
        } else {
            printError(message + " : " + e.getMessage());
        }
    }


    public static String getShortcut(String[] args) {
        if (ArrayUtils.contains(args, "--help")
                || ArrayUtils.contains(args, "-h")
                || ArrayUtils.contains(args, "?")
                || ArrayUtils.contains(args, "help")) {
            return "--help";
        }
        return args[0];
    }

    public static void printArgs(String[] args) {
        PrintUtils.println(argsToString(args));
    }

    public static String argsToString(String[] args) {

        String[] res = Arrays.copyOf(args, args.length);
        if (ArrayUtils.contains(res, "--password") && (ArrayUtils.indexOf(res, "--password") + 1) < res.length) {
            res[(ArrayUtils.indexOf(res, "--password") + 1)] = "********";
        }
        return String.join(" ", res);
    }

    public static void printCommandUsage(JCommander commander) {
        printCommandUsage(commander, System.err);
    }

    public static void printCommandUsage(JCommander commander, PrintStream printStream) {


        Comparator<ParameterDescription> parameterDescriptionComparator =
                Comparator.comparing((ParameterDescription p) -> p.getDescription().contains("DEPRECATED"))
                        .thenComparing(ParameterDescription::getLongestName);
        int maxLength = 0;
        for (ParameterDescription parameterDescription : commander.getParameters()) {
            String desc = (parameterDescription.getParameterized().getParameter() != null
                    && parameterDescription.getParameterized().getParameter().required()) ? "*" : "";
            desc += parameterDescription.getNames();
            if (desc.length() > maxLength) {
                maxLength = desc.length();
            }
        }
        final int pad = maxLength + 12;
        commander.getParameters().stream().sorted(parameterDescriptionComparator).forEach(parameterDescription -> {
            if (parameterDescription.getParameter() != null && !parameterDescription.getParameter().hidden()) {
                String type = getType(parameterDescription);
                String defaultValue = "";
                if (parameterDescription.getDefault() != null) {
                    if (parameterDescription.isDynamicParameter()) {
                        Object def = parameterDescription.getDefault();
                        if (def instanceof Map && !((Map) def).isEmpty()) {
                            defaultValue = " [" + def + "]";
                        }
                    } else {
                        defaultValue = " [" + parameterDescription.getDefault() + "]";
                    }
                }
                PrintUtils.printCommandHelpFormattedString(pad, ((parameterDescription.getParameterized().getParameter() != null
                        && parameterDescription.getParameterized().getParameter().required()) ? "*" : "")
                        + parameterDescription.getNames(), type, parameterDescription.getDescription() + " "
                        + defaultValue);
            }
        });
    }

    private static String getType(ParameterDescription parameterDescription) {
        String type = "";
        if (parameterDescription.getParameter().arity() == 0) {
            return type;
        } else {
            if (parameterDescription.isDynamicParameter()) {
                Type genericType = parameterDescription.getParameterized().getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericType;
                    Type rawType = parameterizedType.getRawType();
                    if (rawType instanceof Class && Map.class.isAssignableFrom((Class) rawType)) {
                        String key = getType(parameterizedType.getActualTypeArguments()[0]);
                        String assignment = parameterDescription.getParameter().getAssignment();
                        String value = getType(parameterizedType.getActualTypeArguments()[1]);
                        type = key + assignment + value;
                    }
                } else {
                    type = getType(genericType);
                }
            } else {
                Type genericType = parameterDescription.getParameterized().getGenericType();
                type = getType(genericType);
                if (type.equals("BOOLEAN") && parameterDescription.getParameterized().getParameter().arity() == -1) {
                    type = "";
                }
            }
        }
        return type;
    }

    private static String getType(Type genericType) {
        String type;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class && Collection.class.isAssignableFrom((Class) rawType)) {
                return getType(parameterizedType.getActualTypeArguments()[0]) + "*";
            }
        }
        type = genericType.getTypeName();
        type = type.substring(1 + Math.max(type.lastIndexOf("."), type.lastIndexOf("$")));
        type = Arrays.asList(org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase(type)).stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining("_"));

        if (type.equals("INTEGER")) {
            type = "INT";
        }
        return type;
    }
}
