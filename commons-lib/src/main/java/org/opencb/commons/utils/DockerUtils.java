package org.opencb.commons.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opencb.commons.exec.Command;

import java.util.List;

public class DockerUtils {

    /**
     * Create and run the command line to execute the docker image.
     *
     * @param image Docker image name
     * @param user  Array of two strings: the first string, the user; the second, the group
     * @param bindings Array of bind mounts for docker volumes: source-target pairs
     * @param params Image command parameter
     * @return The command line
     */
    public static String run(String image, String[] user, List<Pair<String, String>> bindings, String params) {
        // Docker run
        StringBuilder commandLine = new StringBuilder("docker run ");

        // User
        if (ArrayUtils.isNotEmpty(user)) {
            String u = user[0];
            if (user.length > 1) {
                u += (":" + user[1]);
            }
            commandLine.append("-u ").append(u).append(" ");
        }

        // Mount management (bindings)
        for (Pair<String, String> binding : bindings) {
            commandLine.append("--mount type=bind,source=\"").append(binding.getLeft()).append("\",target=\"").append(binding.getRight())
                    .append("\" ");

        }

        // Docker image and version
        commandLine.append(image).append(" ");

        // Image command params
        commandLine.append(params);

        // Execute command
        Command cmd = new Command(commandLine.toString());
        cmd.run();

        return commandLine.toString();
    }
}
