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
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        if (path == null) {
            throw new IOException("Path is null");
        }

        if (!existsFile(path.toFile())) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' does not exist");
        }

        if (!Files.isReadable(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' cannot be read");
        }

        if (writable && !Files.isWritable(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' cannot be written");
        }

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

    public static boolean existsFile(File file) {
        logger.info("FileUtils:existsFile: file = {}; Files.exists ? {}", file, Files.exists(file.toPath()));
        URI uri = file.toURI();
        logger.info("FileUtils:existsFile: uri = {}; Files.exists ? {}", uri, Files.exists(Paths.get(uri)));

        try (InputStream is = Files.newInputStream(file.toPath())) {
            return true;
        } catch (Exception e) {
            logger.info("The file {} could not be opened (using Files.newInputStream), so it is assumed to not exist. {}", file,
                    StringUtils.join(e.getStackTrace(), "\n"));
            return false;
        }
    }

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
