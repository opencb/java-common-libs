package org.opencb.commons.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileUtilsTest {

    Path path = Paths.get("/tmp");

    @Test
    public void getUser() throws IOException {
        String user = FileUtils.getUser(path);
        Assert.assertEquals("root", user);
    }

    @Test
    public void getUserNumeric() throws IOException {
        String user = FileUtils.getUser(path, true);
        Assert.assertEquals("0", user);
    }

    @Test
    public void getGroup() throws IOException {
        String group = FileUtils.getGroup(path);
        Assert.assertEquals("root", group);
    }

    @Test
    public void getGroupNumeric() throws IOException {
        String group = FileUtils.getGroup(path, true);
        Assert.assertEquals("0", group);
    }

    @Test
    public void getUserAndGroup() throws IOException {
        String[] user = FileUtils.getUserAndGroup(path);
        Assert.assertEquals("root", user[0]);
        Assert.assertEquals("root", user[1]);
    }

    @Test
    public void getUserAndGroupNumeric() throws IOException {
        String[] user = FileUtils.getUserAndGroup(path, true);
        Assert.assertEquals("0", user[0]);
        Assert.assertEquals("0", user[1]);
    }

    @Test(expected = IOException.class)
    public void checkPathNull() throws IOException {
	FileUtils.checkPath(null, false);
    }

    @Test(expected = IOException.class)
    public void checkPathNonExist() throws IOException {
	FileUtils.checkPath(path, true);
	FileUtils.checkPath(Paths.get("/tmp/nonExistentPath"), true);
    }

    @Test(expected = IOException.class)
    public void checkFileIsDir() throws IOException {
	FileUtils.checkFile(path, true);
	FileUtils.checkFile(Paths.get("/tmp/test.txt"), true);
	FileUtils.checkFile(Paths.get("/tmp/test.txt"), false);
	FileUtils.checkFile(Paths.get("/tmp/test.txt/"), true);
    }
}
