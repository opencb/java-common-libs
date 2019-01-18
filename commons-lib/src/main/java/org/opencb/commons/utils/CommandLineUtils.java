/*
 * Copyright 2015-2017 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.commons.utils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by imedina on 05/11/15.
 */
public class CommandLineUtils {

    public static final String DEPRECATED = "[DEPRECATED] ";

    private static final int DESCRIPTION_INDENT = 2;
    private static final int DESCRIPTION_LENGTH = 100;

    public static void printCommandUsage(JCommander commander) {
        printCommandUsage(commander, System.err);
    }

    public static void printCommandUsage(JCommander commander, PrintStream printStream) {

        Integer paramNameMaxSize = Math.max(commander.getParameters().stream()
                .map(pd -> pd.getNames().length())
                .collect(Collectors.maxBy(Comparator.naturalOrder())).orElse(20), 20);
        Integer typeMaxSize = Math.max(commander.getParameters().stream()
                .map(pd -> getType(pd).length())
                .collect(Collectors.maxBy(Comparator.naturalOrder())).orElse(10), 10);

        int nameAndTypeLength = paramNameMaxSize + typeMaxSize + 8;
        int maxLineLength = nameAndTypeLength + DESCRIPTION_LENGTH;  //160

        Comparator<ParameterDescription> parameterDescriptionComparator =
                Comparator.comparing((ParameterDescription p) -> p.getDescription().contains("DEPRECATED"))
                        .thenComparing(ParameterDescription::getLongestName);
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
                String usage = String.format("%5s %-" + paramNameMaxSize + "s %-" + typeMaxSize + "s %s%s\n",
                        (parameterDescription.getParameterized().getParameter() != null
                                && parameterDescription.getParameterized().getParameter().required()) ? "*" : "",
                        parameterDescription.getNames(),
                        type,
                        parameterDescription.getDescription(),
                        defaultValue);

                // if lines are longer than the maximum they are trimmed and printed in several lines
                List<String> lines = new LinkedList<>();
                while (usage.length() > maxLineLength + 1) {
                    int splitPosition = Math.min(1 + usage.lastIndexOf(" ", maxLineLength), usage.length());
                    if (splitPosition <= nameAndTypeLength + DESCRIPTION_INDENT) {
                        splitPosition = Math.min(1 + usage.indexOf(" ", maxLineLength), usage.length());
                    }
                    lines.add(usage.substring(0, splitPosition) + "\n");
                    usage = String.format("%" + (nameAndTypeLength + DESCRIPTION_INDENT) + "s", "") + "" + usage.substring(splitPosition);
                }
                // this is empty for short lines and so no prints anything
                lines.forEach(printStream::print);
                // in long lines this prints the last trimmed line
                printStream.print(usage);
            }
        });
    }

    /**
     * Create the following strings from the jCommander to create AutoComplete bash script:
     * commands="users projects studies files jobs individuals families samples variables cohorts alignments variant"
     * users="create info update change-password delete projects login logout reset-password"
     * ...
     * users_create_options="--password --help --log-file --email --name --log-level --conf --organization --output-format --session-id"
     * ...
     * This accepts two levels: commands and subcommands
     *
     * @param jCommander    JComander object to extractt commands and subcommonds
     * @param fileName      Filename destination
     * @param bashFunctName Name of the bash function to autocomplete
     * @param excludeParams List of params to be excluded
     * @throws IOException If the destination file cannot be written
     */
    public static void generateBashAutoComplete(JCommander jCommander, String fileName, String bashFunctName, List<String> excludeParams)
            throws IOException {
        Map<String, JCommander> jCommands = jCommander.getCommands();
        StringBuilder mainCommands = new StringBuilder();
        StringBuilder subCommmands = new StringBuilder();
        StringBuilder subCommmandsOptions = new StringBuilder();

        // Create a HashSet to skip excluded parameters
        Set<String> excludeParamsSet;
        if (excludeParams == null) {
            excludeParamsSet = new HashSet<>();
        } else {
            excludeParamsSet = new HashSet<>(excludeParams);
        }

        for (String command : jCommands.keySet()) {
            JCommander subCommand = jCommands.get(command);
            mainCommands.append(command).append(" ");
            subCommmands.append(command + "=" + "\"");
            Map<String, JCommander> subSubCommands = subCommand.getCommands();
            for (String sc : subSubCommands.keySet()) {
                subCommmands.append(sc).append(" ");
                subCommmandsOptions.append(command + "_");
                // - is not allowed in bash main variable name, replacing it with _
                subCommmandsOptions.append(sc.replaceAll("[-.]+", "_") + "_" + "options=" + "\"");
                JCommander subCommandOptions = subSubCommands.get(sc);
                for (ParameterDescription param : subCommandOptions.getParameters()) {
                    // Add parameter if it is not excluded
                    if (!excludeParamsSet.contains(param.getLongestName())) {
                        subCommmandsOptions.append(param.getLongestName()).append(' ');
                    }
                }
                subCommmandsOptions.replace(0, subCommmandsOptions.length(), subCommmandsOptions.toString().trim()).append("\"" + "\n");
            }
            subCommmands.replace(0, subCommmands.length(), subCommmands.toString().trim()).append("\"" + "\n");
        }

        // Commands(Commands, subCommands and subCommandOptions) are populated intro three strings until this point,
        // Now we write bash script commands and blend these strings into those as appropriate
        StringBuilder autoComplete = new StringBuilder();
        autoComplete.append("_" + bashFunctName + "() \n { \n local cur prev opts \n COMPREPLY=() \n cur="
                + "$" + "{COMP_WORDS[COMP_CWORD]} \n prev=" + "$" + "{COMP_WORDS[COMP_CWORD-1]} \n");

        autoComplete.append("commands=\"").append(mainCommands.toString().trim()).append('"').append('\n');
        autoComplete.append(subCommmands.toString());
        autoComplete.append(subCommmandsOptions.toString());
        autoComplete.append("if [[ ${#COMP_WORDS[@]} > 2 && ${#COMP_WORDS[@]} < 4 ]] ; then \n local options \n case "
                + "$" + "{prev} in \n");

        for (String command : mainCommands.toString().split(" ")) {
            autoComplete.append("\t" + command + ") options=" + "\"" + "${" + command + "}" + "\"" + " ;; \n");
        }

        autoComplete.append("*) ;; \n esac \n COMPREPLY=( $( compgen -W " + "\"" + "$" + "options" + "\""
                + " -- ${cur}) ) \n return 0 \n elif [[ ${#COMP_WORDS[@]} > 3 ]] ; then \n local options \n case " + "$"
                + "{COMP_WORDS[1]} in \n");

        int subCommandIndex = 0;
        for (String command : mainCommands.toString().split(" ")) {
            String[] splittedSubCommands = subCommmands.toString().split("\n")[subCommandIndex]
                    .replace(command + "=" + "\"", "").replace("\"", "").split(" ");

            if (splittedSubCommands[0].isEmpty()) {
                ++subCommandIndex;
            } else {
                autoComplete.append('\t').append(command).append(") \n");
                autoComplete.append("\t\t case " + "$" + "{COMP_WORDS[2]} in \n");

                for (String subCommand : splittedSubCommands) {
                    autoComplete.append("\t\t").append(subCommand).append(") options=").append("\"").append("${").
                            append(command).append("_").append(subCommand.replaceAll("[-.]+", "_")).append("_options}").
                            append("\"").append(" ;; \n");
                }
                autoComplete.append("\t\t *) ;; esac ;; \n");
                ++subCommandIndex;
            }
        }
        autoComplete.append("*) ;;  esac \n COMPREPLY=( $( compgen -W " + "\"" + "$" + "options" + "\"" + " -- ${cur}) )"
                + " \n return 0 \n fi \n if [[ ${cur} == * ]] ; then \n COMPREPLY=( $(compgen -W " + "\"" + "$"
                + "{commands}" + "\"" + " -- ${cur}) ) \n return 0 \n fi \n } \n");

        autoComplete.append("\n");
        autoComplete.append("complete -F _" + bashFunctName + " " + bashFunctName + ".sh").append('\n');
//        autoComplete.append("complete -F _" + bashFunctName + " ./bin/" + bashFunctName + ".sh").append('\n');
//        autoComplete.append("complete -F _" + bashFunctName + " /opt/" + bashFunctName + "/bin/" + bashFunctName + ".sh").append('\n');

        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
            pw.write(autoComplete.toString());
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
