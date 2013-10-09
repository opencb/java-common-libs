package org.opencb.variant.lib.filters.customfilters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.variant.lib.io.VariantFilterRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 10/9/13
 * Time: 5:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfGeneFilterTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test(){

        String fileIn = "/home/aleman/tmp/file.gz";
        String fileOut = "/home/aleman/tmp/file-out.vcf";
        String geneList = "/home/aleman/tmp/gene_list.txt";

        VariantFilterRunner vr = new VariantFilterRunner(fileIn, fileOut);
        List<VcfFilter> list = new ArrayList<>();


        list.add(new VcfGeneFilter(geneList));


        vr.filters(list);

        vr.run();

    }
}
