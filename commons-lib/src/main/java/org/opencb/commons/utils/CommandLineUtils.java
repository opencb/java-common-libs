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
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by imedina on 05/11/15.
 */
public class CommandLineUtils {

    public static final String DEPRECATED = "[DEPRECATED] ";

    private static final int DESCRIPTION_INDENT = 2;
    private static final int DESCRIPTION_LENGTH = 100;


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
        autoComplete.append(subCommmands);
        autoComplete.append(subCommmandsOptions);
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


}
