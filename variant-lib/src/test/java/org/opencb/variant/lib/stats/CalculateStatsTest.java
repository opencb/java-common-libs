package org.opencb.variant.lib.stats;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.variant.lib.io.VariantStatsRunner;
import org.opencb.variant.lib.io.variant.writers.VcfFileDataWriter;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/3/13
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalculateStatsTest {
    private Long start, end;
    private String path = "/opt/data/";
    private String vcfFileName;
    private String pedFileName;
    private String pathStats;
    private String dbFilename;
    private String dbFilters;


    @Rule
    public TestName name = new TestName();


    @Before
    public void setUp() throws Exception {

        vcfFileName = path + "small.vcf";
        pedFileName = path + "file.ped";
        pathStats = path + "jstats/";
        dbFilename = path + "jstats/variant.db";
        dbFilters = path + "jstats/filters.db";
        start = System.currentTimeMillis();


    }

    @After
    public void tearDown() throws Exception {

        end = System.currentTimeMillis();
        System.out.println("Time " + name.getMethodName() + ": " + (end - start));

    }

    @Test
    public void testCalculateStatsList() throws Exception {


        VariantStatsRunner vr = new VariantStatsRunner(vcfFileName, dbFilename, pedFileName);

        vr.writer(new VcfFileDataWriter(pathStats));
        vr.setEffect(true);

        vr.run();


    }
}
