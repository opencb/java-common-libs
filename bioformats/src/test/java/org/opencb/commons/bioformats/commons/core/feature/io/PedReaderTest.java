package org.opencb.commons.bioformats.commons.core.feature.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.commons.core.feature.Ped;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class PedReaderTest {
    private Long start, end;
    private PedReader ped;

    @Rule
    public TestName name = new TestName();


    @Before
    public void setUp() throws Exception {
        start = System.currentTimeMillis();
        ped = new PedReader("/home/aaleman/tmp/file.ped");


    }

    @After
    public void tearDown() throws Exception {
        end = System.currentTimeMillis();
        ped.close();
        System.out.println("Time "+ name.getMethodName()+": " + (end - start));

    }

    @Test
    public void testRead() throws Exception {

        Ped p = null;

        while((p = ped.read()) != null){
            System.out.println("p = " + p);
        }

    }

//    @Test
//    public void testReadSize() throws Exception {
//
//        List<Ped> list = ped.read(2);
//        for(Ped p : list){
//            System.out.println(p);
//        }
//
//    }
//
//    @Test
//    public void testReadAll() throws Exception {
//
//        List<Ped> list = ped.readAll();
//        for(Ped p : list){
//            System.out.println(p);
//        }
//
//    }
    @Test
    public void testTree(){

    }
}
