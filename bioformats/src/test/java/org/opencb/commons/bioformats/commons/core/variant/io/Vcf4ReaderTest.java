package org.opencb.commons.bioformats.commons.core.variant.io;

import com.google.common.base.Predicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfRegionFilter;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfSnpFilter;
import org.opencb.commons.bioformats.commons.core.vcffilter.VcfFilters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 11:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Vcf4ReaderTest {
    private Long start, end;
    private Vcf4Reader vcf;

    @Rule
    public TestName name = new TestName();


    @Before
    public void setUp() throws Exception {
        start = System.currentTimeMillis();
        vcf = new Vcf4Reader("/home/aaleman/tmp/small.vcf");



    }

    @After
    public void tearDown() throws Exception {
        end = System.currentTimeMillis();
        vcf.close();
        System.out.println("Time "+ name.getMethodName()+": " + (end - start));

    }

    @Test
    public void testReadAllWithFilters() throws Exception {

        List<Predicate<VcfRecord>> list_filters = new ArrayList<Predicate<VcfRecord>>(2);
        list_filters.add(new VcfSnpFilter());
        list_filters.add(new VcfRegionFilter("1", 1, 100));

        vcf.setVcfFilters(list_filters);

        List<VcfRecord> list_records = vcf.readAll();

        for(VcfRecord v: list_records){
            System.out.println(v);
        }
    }

    @Test
    public void testReadSizeWithFilters() throws Exception {

        List<Predicate<VcfRecord>> list_filters = new ArrayList<Predicate<VcfRecord>>(2);
        list_filters.add(new VcfSnpFilter());
        list_filters.add(new VcfRegionFilter("1", 1,5));

        vcf.setVcfFilters(list_filters);

        List<VcfRecord> list_records = vcf.read(2);

        System.out.println("First Chunk");
        for(VcfRecord v: list_records){
            System.out.println(v);
        }

        list_records = vcf.read(2);
        System.out.println("Second Chunk");

        for(VcfRecord v: list_records){
            System.out.println(v);
        }

    }
}
