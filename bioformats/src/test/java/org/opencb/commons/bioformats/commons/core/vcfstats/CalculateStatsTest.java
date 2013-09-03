package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.commons.core.connectors.VariantRunner;
import org.opencb.commons.bioformats.commons.core.connectors.ped.readers.PedDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.ped.readers.PedFileDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfDataWriter;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfFileDataWriter;
import org.opencb.commons.bioformats.commons.core.connectors.variant.readers.VcfDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.readers.VcfFileDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfSqliteDataWriter;
import org.opencb.commons.bioformats.commons.core.feature.Pedigree;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfFilter;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfFilterList;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfRegionFilter;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfSnpFilter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/26/13
 * Time: 1:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalculateStatsTest {

    private Long start, end;
    private Vcf4Reader vcf;
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

        vcfFileName = path + "file.vcf";
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
    public void testCalculateStatsRegionFilter() throws Exception {

        VariantRunner vr = new VariantRunner(vcfFileName, pathStats + "regionFilter.db", pedFileName);

        List<VcfFilter> filterList = new VcfFilterList(1);
        filterList.add(new VcfRegionFilter("1", 0, 100000));

        vr.filter(filterList).run();


    }

    @Test
    public void testCalculateStatsSnpFilter() throws Exception {

        VariantRunner vr = new VariantRunner(vcfFileName, pathStats + "snpFilter.db", pedFileName);

        List<VcfFilter> filterList = new VcfFilterList(1);
        filterList.add(new VcfSnpFilter());

        vr.filter(filterList).run();


    }



    @Test
    public void testCalculateStatsList() throws Exception {

        VariantRunner vr = new VariantRunner(vcfFileName, dbFilename, pedFileName);

        vr.run();


    }



}
