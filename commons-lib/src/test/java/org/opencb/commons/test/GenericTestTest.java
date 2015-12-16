package org.opencb.commons.test;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created on 02/12/15
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class GenericTestTest extends GenericTest {

    @Test
    public void testTest() throws Exception {
        System.out.println("It works!");
        Assert.assertEquals("testTest", getTestName().getMethodName());
    }
}