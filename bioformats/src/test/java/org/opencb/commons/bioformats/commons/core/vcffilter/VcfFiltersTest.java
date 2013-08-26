package org.opencb.commons.bioformats.commons.core.vcffilter;

import com.google.common.base.Predicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfRegionFilter;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfSnpFilter;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 10:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfFiltersTest {
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
    public void testFilterList() throws IOException, FileFormatException {

        List<VcfRecord> list_records = vcf.readAll();
        List<Predicate<VcfRecord>> list_filters = new ArrayList<Predicate<VcfRecord>>(2);
        list_filters.add(new VcfSnpFilter());
        list_filters.add(new VcfRegionFilter("1", 1,3));

        List<VcfRecord> filter_list = VcfFilters.filter(list_records, list_filters);

        for(VcfRecord v: filter_list){
            System.out.println(v);
        }

    }

    @Test
    public void testFilter() {

    }
}
