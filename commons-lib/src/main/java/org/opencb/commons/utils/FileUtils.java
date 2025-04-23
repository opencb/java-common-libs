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

import org.apache.commons.lang3.StringUtils;
import org.opencb.commons.exec.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 9/25/13
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public final class FileUtils {

    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void checkPath(Path path) throws IOException {
        checkPath(path, false);
    }

    public static void checkPath(Path path, boolean writable) throws IOException {
        if (path == null) {
            throw new IOException("Path is null");
        }

        if (!Files.exists(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' does not exist");
        }

        if (!Files.isReadable(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' cannot be read");
        }

        if (writable && !Files.isWritable(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' cannot be written");
        }
    }

    public static void checkFile(Path path) throws IOException {
        checkFile(path, false);
    }

    public static void checkFile(Path path, boolean writable) throws IOException {
        checkPath(path, writable);

        if (Files.isDirectory(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' must be a file and not a directory");
        }
    }

    public static void checkDirectory(Path path) throws IOException {
        checkDirectory(path, false);
    }

    public static void checkDirectory(Path path, boolean writable) throws IOException {
        checkPath(path, writable);

        if (!Files.isDirectory(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' must be a directory");
        }
    }

    /**
     * This method is able to determine whether a file is GZipped and return a {@link BufferedReader} in any case.
     *
     * @param path to be read
     * @return BufferedReader object
     * @throws java.io.IOException IOException
     */
    public static BufferedReader newBufferedReader(Path path) throws IOException {
        return newBufferedReader(path, Charset.defaultCharset());
    }

    /**
     * This method is able to determine whether a file is GZipped and return a {@link BufferedReader} in any case.
     *
     * @param path to be read
     * @param charset to be read
     * @return BufferedReader object
     * @throws java.io.IOException IOException
     */
    public static BufferedReader newBufferedReader(Path path, Charset charset) throws IOException {
        return new BufferedReader(new InputStreamReader(newInputStream(path), charset));
    }

    /**
     * This method is able to determine whether a file is GZipped and return an {@link InputStream} in any case.
     *
     * @param path     the path to the file to open
     * @param options   options specifying how the file is opened
     * @return          a new input stream
     * @throws IOException  if an I/O error occurs
     */
    public static InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        FileUtils.checkFile(path);
        InputStream inputStream;
        if (path.toFile().getName().endsWith(".gz")) {
            inputStream = new GZIPInputStream(Files.newInputStream(path, options));
        } else {
            inputStream = Files.newInputStream(path, options);
        }
        return inputStream;
    }

    /**
     * This method is able to determine whether a file is GZipped and return a {@link BufferedWriter} in any case.
     *
     * @param path to be write
     * @return BufferedWriter object
     * @throws java.io.IOException IOException
     */
    public static BufferedWriter newBufferedWriter(Path path) throws IOException {
        FileUtils.checkDirectory(path.getParent());
        BufferedWriter bufferedWriter;
        if (path.toFile().getName().endsWith(".gz")) {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(path.toFile()))));
        } else {
            bufferedWriter = Files.newBufferedWriter(path, Charset.defaultCharset());
        }
        return bufferedWriter;
    }

    /**
     * This method is able to determine whether a file is GZipped and return a {@link BufferedWriter} in any case.
     *
     * @param path to be write
     * @param charset to be write
     * @return BufferedWriter object
     * @throws java.io.IOException IOException
     */
    public static BufferedWriter newBufferedWriter(Path path, Charset charset) throws IOException {
        FileUtils.checkDirectory(path.getParent());
        BufferedWriter bufferedWriter;
        if (path.toFile().getName().endsWith(".gz")) {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(path.toFile())), charset));
        } else {
            bufferedWriter = Files.newBufferedWriter(path, charset);
        }
        return bufferedWriter;
    }

    public static String getUser(Path path) throws IOException {
        return getUser(path, false);
    }

    public static String getUser(Path path, boolean numericId) throws IOException {
        return getLsOutput(path, numericId).split(" ")[2];
    }

    public static String getGroup(Path path) throws IOException {
        return getGroup(path, false);
    }

    public static String getGroup(Path path, boolean numericId) throws IOException {
        return getLsOutput(path, numericId).split(" ")[3];
    }

    public static String[] getUserAndGroup(Path path) throws IOException {
        return getUserAndGroup(path, false);
    }

    public static String[] getUserAndGroup(Path path, boolean numericId) throws IOException {
        String[] split = getLsOutput(path, numericId).split(" ");

        return new String[]{split[2], split[3]};
    }

    public static void copyFile(Path src, Path dest) throws IOException {
        copyFile(src.toFile(), dest.toFile());
    }

    public static void copyFile(File src, File dest) throws IOException {
        try {
            org.apache.commons.io.FileUtils.copyFile(src, dest);
        } catch (IOException e) {
            try {
                if (src.length() == dest.length()) {
                    LoggerFactory.getLogger(FileUtils.class).warn(e.getMessage());
                    return;
                }
                throw e;
            } catch (Exception e1) {
                throw e;
            }
        }
    }

    public static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("Source directory does not exist or is not a directory: " + sourceDir);
        }

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
            logger.debug("Created target directory: {}", targetDir);
        } else if (!Files.isDirectory(targetDir)) {
            String msg = "Target path exists but is not a directory: " + targetDir;
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        try (Stream<Path> stream = Files.walk(sourceDir)) {
            stream.forEach(source -> {
                Path target = targetDir.resolve(sourceDir.relativize(source));
                try {
                    if (!Files.exists(target)) {
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(target);
                            logger.debug("Created directory: {}", target);
                        } else {
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                            logger.debug("Copied file: {} to {}", source, target);
                        }
                    }
                } catch (IOException e) {
                    String msg = "Error copying: " + source + " to " + target + " - " + e.getMessage();
                    logger.error(msg);
                    throw new RuntimeException(msg, e);
                }
            });
        }
    }

    public static void deleteDirectory(Path directory) throws IOException {
        deleteDirectory(directory.toFile());
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        // If it's a directory, delete contents recursively
        if (directory.isDirectory()) {
            java.io.File[] files = directory.listFiles();
            // Not null if directory is not empty
            if (files != null) {
                for (java.io.File file : files) {
                    // Recursively delete subdirectories and files
                    deleteDirectory(file);
                }
            }
        }

        // Finally, delete the directory or file itself
        Files.delete(directory.toPath());
    }

    //-------------------------------------------------------------------------
    // P R I V A T E     M E T H O D S
    //-------------------------------------------------------------------------

    private static String getLsOutput(Path path, boolean numericId) throws IOException {
        FileUtils.checkPath(path);

        String dirOption = path.toFile().isDirectory() ? "d" : "";
        String numericOption = numericId ? "n" : "";
        String cmdline = "ls -l" + dirOption + numericOption + " " + path.toAbsolutePath().toString();

        // Execute command line
        Command cmd = new Command(cmdline);
        cmd.run();

        // Get output and error
        String output = cmd.getOutput();
        String error = cmd.getError();

        if (StringUtils.isNotEmpty(error)) {
            throw new IOException(error);
        }

        if (StringUtils.isEmpty(output)) {
            throw new IOException("Error accessing to path: " + path);
        }

        return output;
    }
}
