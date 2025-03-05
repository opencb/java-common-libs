package org.opencb.commons.utils;

import org.apache.commons.lang3.StringUtils;
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
        return buildCommandLine(image, inputBindings, Collections.singletonList(outputBinding), cmdParams, dockerParams);
    }

    /**
     * Create the command line to execute the docker image.
     *
     * @param image         Docker image name
     * @param inputBindings  Array of bind mounts for docker input volumes (source-target)
     * @param outputBindings Array of bind mount for docker output volume (source-target)
     * @param cmdParams     Image command parameters
     * @param dockerParams  Docker parameters
     * @return The command line
     * @throws IOException IO exception
     */
    public static String buildCommandLine(String image, List<AbstractMap.SimpleEntry<String, String>> inputBindings,
                                          List<AbstractMap.SimpleEntry<String, String>> outputBindings, String cmdParams,
                                          Map<String, String> dockerParams) throws IOException {
        // Sanity check
        if (outputBindings == null || outputBindings.isEmpty()) {
            throw new IllegalArgumentException("Missing output binding(s)");
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
                if (key.equals("user") && StringUtils.isEmpty(dockerParams.get("user"))) {
                    // User wants to disable user setting
                    continue;
                }
                if (!key.startsWith("-")) {
                    commandLine.append("--");
                }
                commandLine.append(key).append(" ");
                if (StringUtils.isNotEmpty(dockerParams.get(key))) {
                    commandLine.append(dockerParams.get(key)).append(" ");
                }
            }
        }

        if (setUser) {
            // User: array of two strings, the first string, the user; the second, the group
            String[] user = FileUtils.getUserAndGroup(Paths.get(outputBindings.get(0).getKey()), true);
            commandLine.append("--user ").append(user[0]).append(":").append(user[1]).append(" ");
        }

        if (inputBindings != null) {
            // Mount management (bindings)
            Set<String> inputBindingSet = new HashSet<>();
            for (AbstractMap.SimpleEntry<String, String> binding : inputBindings) {
                if (!inputBindingSet.contains(binding.getKey())) {
                    commandLine.append("--mount type=bind,source=\"").append(binding.getKey()).append("\",target=\"")
                            .append(binding.getValue()).append("\",readonly ");
                    inputBindingSet.add(binding.getKey());
                }
            }
        }
        for (AbstractMap.SimpleEntry<String, String> outputBinding : outputBindings) {
            commandLine.append("--mount type=bind,source=\"").append(outputBinding.getKey()).append("\",target=\"")
                    .append(outputBinding.getValue()).append("\" ");
        }

        // Docker image and version
        commandLine.append(image).append(" ");

        // Image command params
        commandLine.append(cmdParams);
        return commandLine.toString();
    }

    public static String buildMountPathsCommandLine(String image, String entryPoint) {
        return buildMountPathsCommandLine(image, entryPoint, Collections.emptyList());
    }

    public static String buildMountPathsCommandLine(String image, String entryPoint, List<String> dockerOpts) {
        String res = "docker run ";
        if (dockerOpts != null && !dockerOpts.isEmpty()) {
            res += StringUtils.join(dockerOpts, " ") + " ";
        }

        String prefix = entryPoint;
        String suffix = "";
        if (prefix.contains(">")) {
            prefix = entryPoint.substring(0, entryPoint.indexOf(">"));
            suffix = entryPoint.substring(entryPoint.indexOf(">"));
        }

        List<String> paths = getPaths(prefix);
        String mappedPaths = "";
        for (int i = 0; i < paths.size(); i++) {
            mappedPaths += MAPPING_PATH.replace(PATH_KEY, paths.get(i)) + MAPPED_PATH + i + "/ ";
            prefix = prefix.replace(paths.get(i), MAPPED_PATH + i + "/");
        }
        res += mappedPaths + image + " " + prefix + suffix;
        return res;
    }

    private static List<String> getPaths(String entryPoint) {
        // clean '//' because it's not valid path and the regex extracts to '/'
        entryPoint = entryPoint.replace("//", "/");
        Set<String> res = new HashSet<>();
        String regex = "((\\/([A-z0-9-_+\\.]+\\/)+)|(\\/))";
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
        return run(image, inputBindings, outputBinding != null ? Collections.singletonList(outputBinding) : null, cmdParams, dockerParams);
    }

    /**
     * Create and run the command line to execute the docker image.
     *
     * @param image         Docker image name
     * @param inputBindings Array of bind mounts for docker input volumes (source-target)
     * @param outputBindings Array of bind mount for docker output volume (source-target)
     * @param cmdParams     Image command parameters
     * @param dockerParams  Docker parameters
     * @return The command line
     * @throws IOException IO exception
     */
    public static String run(String image, List<AbstractMap.SimpleEntry<String, String>> inputBindings,
                             List<AbstractMap.SimpleEntry<String, String>> outputBindings, String cmdParams,
                             Map<String, String> dockerParams) throws IOException {
        checkDockerDaemonAlive();

        String commandLine = buildCommandLine(image, inputBindings, outputBindings, cmdParams, dockerParams);

        LOGGER.info("Run docker command line");
        LOGGER.info("============================");
        LOGGER.info(commandLine);
        LOGGER.info("============================");

        // Execute command
        Command cmd = new Command(commandLine);
        cmd.run();

        return commandLine;
    }


    public static void checkDockerDaemonAlive() throws IOException {
        int maxAttempts = 12;
        for (int i = 0; i < maxAttempts; i++) {
            Command command = new Command("docker stats --no-stream");
            command.run();
            if (command.getExitValue() == 0) {
                // Docker is alive
                if (i != 0) {
                    LOGGER.info("Docker daemon up and running!");
                }
                return;
            }
            LOGGER.info("Waiting for docker to start... (sleep 5s) [" + i + "/" + maxAttempts + "]");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
        throw new IOException("Docker daemon is not available on this node!");
    }
}
