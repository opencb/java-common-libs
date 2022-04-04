package org.opencb.commons.utils;

import org.opencb.commons.exec.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DockerUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(DockerUtils.class);
    private static final String MAPPING_PATH = "--mount type=bind,source=${PATH},target=";
    private static final String PATH_KEY = "${PATH}";
    private static final String MAPPED_PATH = "/data/input";

    /**
     * Create the command line to execute the docker image.
     *
     * @param image         Docker image name
     * @param inputBindings Array of bind mounts for docker input volumes (source-target)
     * @param outputBinding Bind mount for docker output volume (source-target)
     * @param cmdParams     Image command parameters
     * @param dockerParams  Docker parameters
     * @return The command line
     * @throws IOException IO exception
     */
    public static String buildCommandLine(String image, List<AbstractMap.SimpleEntry<String, String>> inputBindings,
                                          AbstractMap.SimpleEntry<String, String> outputBinding, String cmdParams,
                                          Map<String, String> dockerParams) throws IOException {
        // Sanity check
        if (outputBinding == null) {
            throw new IllegalArgumentException("Missing output binding");
        }

        // Docker run
        StringBuilder commandLine = new StringBuilder("docker run --rm ");

        // Docker params
        boolean setUser = true;
        if (dockerParams != null) {
            if (dockerParams.containsKey("user")) {
                setUser = false;
            }
            for (String key : dockerParams.keySet()) {
                commandLine.append("--").append(key).append(" ").append(dockerParams.get(key)).append(" ");
            }
        }

        if (setUser) {
            // User: array of two strings, the first string, the user; the second, the group
            String[] user = FileUtils.getUserAndGroup(Paths.get(outputBinding.getKey()), true);
            commandLine.append("--user ").append(user[0]).append(":").append(user[1]).append(" ");
        }

        if (inputBindings != null) {
            // Mount management (bindings)
            for (AbstractMap.SimpleEntry<String, String> binding : inputBindings) {
                commandLine.append("--mount type=bind,source=\"").append(binding.getKey()).append("\",target=\"").append(binding.getValue())
                        .append("\" ");
            }
        }
        commandLine.append("--mount type=bind,source=\"").append(outputBinding.getKey()).append("\",target=\"")
                .append(outputBinding.getValue()).append("\" ");

        // Docker image and version
        commandLine.append(image).append(" ");

        // Image command params
        commandLine.append(cmdParams);
        return commandLine.toString();
    }


    public static String buildMountPathsCommandLine(String image, String entryPoint) {
        String res = "docker run ";
        List<String> paths = getPaths(entryPoint);
        String mappedPaths = "";
        for (int i = 0; i < paths.size(); i++) {
            mappedPaths += MAPPING_PATH.replace(PATH_KEY, paths.get(i)) + MAPPED_PATH + i + "/ ";
            entryPoint = entryPoint.replace(paths.get(i), MAPPED_PATH + i + "/");
        }
        res += mappedPaths + image + " " + entryPoint;
        return res;
    }

    private static List<String> getPaths(String entryPoint) {
        // clean '//' because it's not valid path and the regex extracts to '/'
        entryPoint = entryPoint.replace("//", "/");
        Set<String> res = new HashSet<>();
        String regex = "((\\/([A-z0-9-_+]+\\/)+)|(\\/))";
        Pattern regexPattern = Pattern.compile(regex);
        Matcher match = regexPattern.matcher(entryPoint);
        Set<String> aux = new HashSet<>();
        while (match.find()) {
            aux.add(match.group(1));
            res.add(match.group(1));
        }
        //Search for paths contained in other paths, to return only the root path
        for (String path : aux) {
            for (String path2 : aux) {
                if (path.contains(path2) && !path2.equals(path)) {
                    res.remove(path);
                }
            }
        }
        return new ArrayList<>(res);
    }


    /**
     * Create and run the command line to execute the docker image.
     *
     * @param image         Docker image name
     * @param inputBindings Array of bind mounts for docker input volumes (source-target)
     * @param outputBinding Bind mount for docker output volume (source-target)
     * @param cmdParams     Image command parameters
     * @param dockerParams  Docker parameters
     * @return The command line
     * @throws IOException IO exception
     */
    public static String run(String image, List<AbstractMap.SimpleEntry<String, String>> inputBindings,
                             AbstractMap.SimpleEntry<String, String> outputBinding, String cmdParams,
                             Map<String, String> dockerParams) throws IOException {
        String commandLine = buildCommandLine(image, inputBindings, outputBinding, cmdParams, dockerParams);

        LOGGER.info("Run docker command line");
        LOGGER.info("============================");
        LOGGER.info(commandLine);
        LOGGER.info("============================");

        // Execute command
        Command cmd = new Command(commandLine);
        cmd.run();

        return commandLine;
    }
}
