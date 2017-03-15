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

import java.io.PrintStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by imedina on 05/11/15.
 */
public class CommandLineUtils {

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
        int descriptionLength = 100;
        int maxLineLength = nameAndTypeLength + descriptionLength;  //160

        Comparator<ParameterDescription> parameterDescriptionComparator = (e1, e2) -> e1.getLongestName().compareTo(e2.getLongestName());
        commander.getParameters().stream().sorted(parameterDescriptionComparator).forEach(parameterDescription -> {
            if (parameterDescription.getParameter() != null
                    && !parameterDescription.getParameter().hidden()) {
                String type = getType(parameterDescription);
                String defaultValue = "";
                if (parameterDescription.getDefault() != null && !parameterDescription.isDynamicParameter()) {
                    defaultValue = ("[" + parameterDescription.getDefault() + "]");
                }
                String usage = String.format("%5s %-" + paramNameMaxSize + "s %-" + typeMaxSize + "s %s %s\n",
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
                    lines.add(usage.substring(0, splitPosition) + "\n");
                    usage = String.format("%" + nameAndTypeLength + "s", "") + usage.substring(splitPosition);
                }
                // this is empty for short lines and so no prints anything
                lines.forEach(printStream::print);
                // in long lines this prints the last trimmed line
                printStream.print(usage);
            }
        });
    }

    private static String getType(ParameterDescription parameterDescription) {
        String type = "";
        if (parameterDescription.getParameter().arity() == 0) {
            return type;
        } else if (parameterDescription.isDynamicParameter()) {
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
