package org.opencb.commons.utils;

import org.opencb.commons.exec.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class DockerUtils {


    private static Logger logger = LoggerFactory.getLogger(DockerUtils.class);

    /**
     * Create the command line to execute the docker image.
     *
     * @param image Docker image name
     * @param inputBindings Array of bind mounts for docker input volumes (source-target)
     * @param outputBinding Bind mount for docker output volume (source-target)
     * @param cmdParams Image command parameters
     * @param dockerParams Docker parameters
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

    /**
     * Create and run the command line to execute the docker image.
     *
     * @param image Docker image name
     * @param inputBindings Array of bind mounts for docker input volumes (source-target)
     * @param outputBinding Bind mount for docker output volume (source-target)
     * @param cmdParams Image command parameters
     * @param dockerParams Docker parameters
     * @return The command line
     * @throws IOException IO exception
     */
    public static String run(String image, List<AbstractMap.SimpleEntry<String, String>> inputBindings,
                             AbstractMap.SimpleEntry<String, String> outputBinding, String cmdParams,
                             Map<String, String> dockerParams) throws IOException {
        String commandLine = buildCommandLine(image, inputBindings, outputBinding, cmdParams, dockerParams);

        logger.info("Run docker command line");
        logger.info("============================");
        logger.info(commandLine);
        logger.info("============================");

        // Execute command
        Command cmd = new Command(commandLine);
        cmd.run();

        return commandLine;
    }
}
