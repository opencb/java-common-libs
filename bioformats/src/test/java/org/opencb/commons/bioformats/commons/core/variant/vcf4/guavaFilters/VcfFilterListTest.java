package org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfFilterListTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test(){

        VcfFilter f1 = new VcfRegionFilter("1",1,1);
        VcfFilter f2 = new VcfSnpFilter();

        VcfFilterList filter_list = new VcfFilterList();

        filter_list.add(f2);
        filter_list.add(f1);

        for (VcfFilter f: filter_list){
            System.out.println(f.getClass());
        }

    }
}
