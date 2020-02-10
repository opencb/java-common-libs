package org.opencb.commons.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.opencb.commons.exec.Command;

import java.util.List;

public class DockerUtils {

    public static void run(String image, List<Pair<String, String>> bindings, String params) {
        // Docker run
        StringBuilder commandLine = new StringBuilder("docker run ");

        // Mount management (bindings)
        for (Pair<String, String> binding : bindings) {
            commandLine.append("--mount type=bind,source=\"").append(binding.getLeft()).append("\",target=\"").append(binding.getRight())
                    .append("\" ");

        }

        // Docker image and version
        commandLine.append(image).append(" ");

        // Params
        commandLine.append(params);

        // Execute command
        Command cmd = new Command(commandLine.toString());
        cmd.run();
    }
}
