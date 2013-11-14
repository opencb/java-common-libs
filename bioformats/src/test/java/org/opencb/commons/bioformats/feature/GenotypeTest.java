package org.opencb.commons.bioformats.feature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.commons.test.GenericTest;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 11/5/13
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenotypeTest extends GenericTest {

    private Genotype g;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        g = new Genotype("0/1");

    }

    @After
    public void tearDown() throws Exception {
        g = null;
        super.tearDown();
    }
//
//    @Test
//    public void testGetAllele1() throws Exception {
//        assertEquals(g.getAllele1(), new Integer(0));
//
//    }
//
//    @Test
//    public void testSetAllele1() throws Exception {
//
//    }
//
//    @Test
//    public void testGetAllele2() throws Exception {
//
//    }
//
//    @Test
//    public void testSetAllele2() throws Exception {
//
//    }
//
//    @Test
//    public void testGetCode() throws Exception {
//
//    }
//
//    @Test
//    public void testSetCode() throws Exception {
//
//    }
//
//    @Test
//    public void testGetCount() throws Exception {
//
//    }
//
//    @Test
//    public void testSetCount() throws Exception {
//
//    }
//
//    @Test
//    public void testToString() throws Exception {
//
//    }
//
//    @Test
//    public void testEquals() throws Exception {
//
//    }
//
//    @Test
//    public void testIsAllele1Ref() throws Exception {
//
//    }
//
//    @Test
//    public void testIsAllele2Ref() throws Exception {
//
//    }

    @Test
    public void testGenotypes() {
        System.out.println(new Genotype("C/T", "C", "T,A"));
        System.out.println(new Genotype("C/C", "C", "T,A"));
        System.out.println(new Genotype("T/T", "C", "T,A"));
        System.out.println(new Genotype("T/C", "C", "T,A"));
        System.out.println(new Genotype("A/T", "C", "T,A"));
        System.out.println(new Genotype("./T", "C", "T,A"));
        System.out.println(new Genotype("./.", "C", "T,A"));
        System.out.println(new Genotype("G/.", "C", "T,A"));
    }
}
