package org.opencb.commons.utils;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VersionUtilsTest {

    public static String getComparation(String minVersion, String version) {
        int c = new VersionUtils.Version(minVersion).compareTo(new VersionUtils.Version(version));
        String comp;
        if (c == 0) {
            comp = "=";
        } else if (c < 0) {
            comp = "<";
        } else {
            comp = ">";
        }
        System.out.println(minVersion + "\t" + comp + "\t" + version);
        return comp;
    }

    @Test
    public void testOrder() {
        assertEquals("<", getComparation("5.2.7", "5.2.8"));
        assertEquals("=", getComparation("5.2.7", "5.2.7"));
        assertEquals(">", getComparation("5.2.7.1", "5.2.7.1-alpha"));
        assertEquals(">", getComparation("5.2.7", "5.2.7-SNAPSHOT"));
        assertEquals("<", getComparation("5.2.7-alpha", "5.2.7"));
        assertEquals("<", getComparation("5.2.7-alpha", "5.2.7-beta"));
        assertEquals(">", getComparation("5.2.7", "5.2.6"));
        assertEquals("=", getComparation("5.2.7", "5.2.7.0"));
    }

    @Test
    public void testOrderShortVersions() {
        assertEquals("<", getComparation("v5.2", "v5.3"));
        assertEquals("=", getComparation("v5.2", "v5.2"));
        assertEquals(">", getComparation("v5.2", "v5.1"));
        assertEquals(">", getComparation("v5.6", "v5.2"));
    }
}