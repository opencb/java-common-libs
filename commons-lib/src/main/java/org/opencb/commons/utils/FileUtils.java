package org.opencb.commons.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 9/25/13
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {


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
            throw new IOException("Path '" + path.toAbsolutePath() + "' must be a file");
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
     * This method is able to determine whether a file is GZipped and return a BufferedReader in any case.
     *
     * @param path to be read
     * @return BufferedReader object
     * @throws java.io.IOException IOException
     */
    public static BufferedReader newBufferedReader(Path path) throws IOException {
        FileUtils.checkFile(path);
        BufferedReader bufferedReader;
        if (path.toFile().getName().endsWith(".gz")) {
            bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path.toFile()))));
        } else {
            bufferedReader = Files.newBufferedReader(path, Charset.defaultCharset());
        }
        return bufferedReader;
    }

    /**
     * This method is able to determine whether a file is GZipped and return a BufferedReader in any case.
     *
     * @param path to be read
     * @param charset to be read
     * @return BufferedReader object
     * @throws java.io.IOException IOException
     */
    public static BufferedReader newBufferedReader(Path path, Charset charset) throws IOException {
        FileUtils.checkFile(path);
        BufferedReader bufferedReader;
        if (path.toFile().getName().endsWith(".gz")) {
            bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path.toFile()))));
        } else {
            bufferedReader = Files.newBufferedReader(path, charset);
        }
        return bufferedReader;
    }

    /**
     * This method is able to determine whether a file is GZipped and return a BufferedWriter in any case.
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
     * This method is able to determine whether a file is GZipped and return a BufferedWriter in any case.
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
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(path.toFile()))));
        } else {
            bufferedWriter = Files.newBufferedWriter(path, charset);
        }
        return bufferedWriter;
    }

}
