package org.opencb.commons.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created on 02/12/15
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class GenericTestTest extends GenericTest {


    private final String methodName = "testTest";

    @Test
    public void testTest() {
        System.out.println("It works!");
        assertEquals("testTest", methodName);
    }
}