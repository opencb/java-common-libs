package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.commons.core.connectors.ped.readers.PedDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.ped.readers.PedFileDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfFileStatsDataWriter;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfSqliteStatsDataWriter;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfStatsDataWriter;
import org.opencb.commons.bioformats.commons.core.connectors.variant.readers.VcfDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.readers.VcfFileDataReader;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;

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


    @Rule
    public TestName name = new TestName();


    @Before
    public void setUp() throws Exception {

        vcfFileName = path + "small.vcf";
        pedFileName= path + "file.ped";
        pathStats = path + "jstats/";
        dbFilename = path + "jstats/stats.db";
        start = System.currentTimeMillis();


    }

    @After
    public void tearDown() throws Exception {

        end = System.currentTimeMillis();
        System.out.println("Time " + name.getMethodName() + ": " + (end - start));

    }

    @Test
    public void testCalculateStatsList() throws Exception {

        VcfDataReader vcfReader = new VcfFileDataReader(vcfFileName);
        VcfStatsDataWriter vcfWriter = new VcfFileStatsDataWriter(pathStats);
        //VcfStatsDataWriter vcfWriter = new VcfSqliteStatsDataWriter(dbFilename);
        PedDataReader pedReader = new PedFileDataReader(pedFileName);

        CalculateStats.runner(vcfReader, vcfWriter, pedReader);

    }



}
