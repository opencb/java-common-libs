package org.opencb.commons.utils;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilsTest {

    Path path = Paths.get("/tmp");

    @Test
    public void getUser() throws IOException {
        String user = FileUtils.getUser(path);
        assertEquals("root", user);
    }

    @Test
    public void getUserNumeric() throws IOException {
        String user = FileUtils.getUser(path, true);
        assertEquals("0", user);
    }

    @Test
    public void getGroup() throws IOException {
        String group = FileUtils.getGroup(path);
        assertEquals("root", group);
    }

    @Test
    public void getGroupNumeric() throws IOException {
        String group = FileUtils.getGroup(path, true);
        assertEquals("0", group);
    }

    @Test
    public void getUserAndGroup() throws IOException {
        String[] user = FileUtils.getUserAndGroup(path);
        assertEquals("root", user[0]);
        assertEquals("root", user[1]);
    }

    @Test
    public void getUserAndGroupNumeric() throws IOException {
        String[] user = FileUtils.getUserAndGroup(path, true);
        assertEquals("0", user[0]);
        assertEquals("0", user[1]);
    }
}