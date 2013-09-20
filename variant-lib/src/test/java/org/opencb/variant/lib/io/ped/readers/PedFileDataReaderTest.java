package org.opencb.variant.lib.io.ped.readers;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.variant.lib.core.formats.Pedigree;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/19/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PedFileDataReaderTest {
    private Long start, end;

    @Rule
    public TestName name = new TestName();


    @Before
    public void setUp() throws Exception {

        start = System.currentTimeMillis();
    }

    @After
    public void tearDown() throws Exception {

        end = System.currentTimeMillis();
        System.out.println("Time " + name.getMethodName() + ": " + (end - start));

    }

    //
    @Test
    public void testSmall() {

        PedDataReader ped = new PedFileDataReader("/home/aaleman/tmp/aux.ped");

        ped.open();

        Pedigree p = ped.read();

        System.out.println(p);

        ped.close();


    }

    @Test
    public void testBier() {

        PedDataReader ped = new PedFileDataReader("/home/aaleman/tmp/bier.ped");

        ped.open();

        Pedigree p = null;
        p = ped.read();

        System.out.println(p);

        ped.close();


    }

    @Test
    public void testFamiliaRara() {

        PedDataReader ped = new PedFileDataReader("/home/aaleman/tmp/familiarara.ped");

        ped.open();

        Pedigree p = null;
        p = ped.read();

        System.out.println(p);

        ped.close();


    }

}
