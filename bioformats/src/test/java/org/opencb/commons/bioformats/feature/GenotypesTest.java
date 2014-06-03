package org.opencb.commons.bioformats.feature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.commons.test.GenericTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 11/11/13
 * Time: 9:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenotypesTest extends GenericTest {

    private List<Genotype> list;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        list = new ArrayList<>();

    }

    @After
    public void tearDown() throws Exception {
        list.clear();
        list = null;
        super.tearDown();
    }

    @Test
    public void testAddGenotypeToList() throws Exception {
        Genotype g = new Genotype("0/1");
        int size = list.size();
        list.add(g);
        assertTrue(list.size() == size + 1);
        assertEquals(g, list.get(size));


    }
}
