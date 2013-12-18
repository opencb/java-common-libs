package org.opencb.commons.bioformats.feature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.commons.test.GenericTest;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 11/11/13
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class RegionTest extends GenericTest {
    private Region r;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        r = new Region("1", 10, 20);
    }

    @After
    public void tearDown() throws Exception {
        r = null;
        super.tearDown();
    }

    @Test
    public void testGetChromosome() throws Exception {
        assertEquals(r.getChromosome(), "1");

    }

    @Test
    public void testSetChromosome() throws Exception {
        r.setChromosome("2");
        assertEquals(r.getChromosome(), "2");

    }

    @Test
    public void testGetStart() throws Exception {
        assertEquals(new Long(r.getStart()), new Long(10));
    }

    @Test
    public void testSetStart() throws Exception {
        r.setStart(12);
        assertEquals(new Long(r.getStart()), new Long(12));

    }

    @Test
    public void testGetEnd() throws Exception {
        assertEquals(new Long(r.getEnd()), new Long(20));
    }

    @Test
    public void testSetEnd() throws Exception {
        r.setEnd(21);
        assertEquals(new Long(r.getEnd()), new Long(21));
    }

    @Test
    public void testEquals() throws Exception {
        Region r1 = new Region("1", 10, 20);

        assertEquals(r, r1);
        r1.setEnd(21);
        assertNotEquals(r, r1);

    }

    @Test
    public void testHashCode() throws Exception {

        Region r1 = new Region("1", 11, 20);
        assertEquals(r.hashCode(), r.hashCode());
        assertNotEquals(r.hashCode(), r1.hashCode());

    }

    @Test
    public void testContains() throws Exception {
        assertTrue(r.contains("1", 11));
        assertFalse(r.contains("1", 100));

    }

    @Test
    public void testConstructorString1() {
        Region aux = new Region("1");
        assertEquals(aux.getChromosome(), "1");
    }

    @Test
    public void testConstructorString2() {
        Region aux = new Region("1:1000");
        assertEquals(aux.getChromosome(), "1");
        assertEquals(aux.getStart(), 1000);
        assertEquals(aux.getEnd(), Long.MAX_VALUE);
    }

    @Test
    public void testConstructorString3() {
        Region aux = new Region("1:1-2");
        assertEquals(aux.getChromosome(), "1");
        assertEquals(aux.getStart(), 1);
        assertEquals(aux.getEnd(), 2);
    }

}
