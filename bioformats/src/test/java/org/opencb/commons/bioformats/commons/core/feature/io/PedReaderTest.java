package org.opencb.commons.bioformats.commons.core.feature.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.commons.core.feature.Individual;
import org.opencb.commons.bioformats.commons.core.feature.Ped;
import org.opencb.commons.bioformats.commons.core.feature.Pedigree;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class PedReaderTest {
    private Long start, end;
    private Pedigree ped;

    @Rule
    public TestName name = new TestName();


    @Before
    public void setUp() throws Exception {
        start = System.currentTimeMillis();


    }

    @After
    public void tearDown() throws Exception {
        end = System.currentTimeMillis();
        System.out.println("Time "+ name.getMethodName()+": " + (end - start));

    }


    @Test
    public void test() throws Exception {

        ped = new Pedigree("/home/aaleman/tmp/file.ped");

//        System.out.println("Individuos");
//        for(Map.Entry<String, Individual> elem: ped.getIndividuals().entrySet()){
//            System.out.println(elem);
//
//        }
//
//        System.out.println("Familias");
//        for(Map.Entry<String, Set<Individual>> elem: ped.getFamilies().entrySet()){
//            System.out.println(elem.getKey());
//            for(Individual ind : elem.getValue()){
//                System.out.println(ind);
//            }
//
//        }

        System.out.println(ped);

    }
}
