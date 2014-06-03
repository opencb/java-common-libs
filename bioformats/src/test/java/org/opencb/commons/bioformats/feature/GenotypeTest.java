package org.opencb.commons.bioformats.feature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.commons.test.GenericTest;

import static org.junit.Assert.*;

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

    @Test
    public void testGetAllele1() throws Exception {
        assertEquals(g.getAllele1(), new Integer(0));

    }

    @Test
    public void testSetAllele1() throws Exception {
        g.setAllele1(2);
        assertEquals(g.getAllele1(), new Integer(2));

    }

    @Test
    public void testGetAllele2() throws Exception {
        assertEquals(g.getAllele2(), new Integer(1));

    }

    @Test
    public void testSetAllele2() throws Exception {
        g.setAllele2(2);
        assertEquals(g.getAllele2(), new Integer(2));
    }

    @Test
    public void testGetCode() throws Exception {
        assertEquals(g.getCode(), AllelesCode.ALLELES_OK);

    }

    @Test
    public void testSetCode() throws Exception {
        g.setAllele1(null);
        assertEquals(g.getCode(), AllelesCode.FIRST_ALLELE_MISSING);

        g.setAllele2(null);
        assertEquals(g.getCode(), AllelesCode.ALL_ALLELES_MISSING);


    }

    @Test
    public void testGetCount() throws Exception {
        assertEquals(g.getCount(), new Integer(0));

    }

    @Test
    public void testSetCount() throws Exception {
        g.setCount(123);
        assertEquals(g.getCount(), new Integer(123));

    }

    @Test
    public void testToString() throws Exception {
        System.out.println(g.toString());

    }

    @Test
    public void testEquals() throws Exception {
        Genotype g2 = new Genotype("0/1");

        assertEquals(g, g2);


    }

    @Test
    public void testIsAllele1Ref() throws Exception {

        assertTrue(g.isAllele1Ref());
    }

    @Test
    public void testIsAllele2Ref() throws Exception {
        assertFalse(g.isAllele2Ref());

    }
}
