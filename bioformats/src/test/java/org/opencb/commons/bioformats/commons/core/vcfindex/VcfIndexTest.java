package org.opencb.commons.bioformats.commons.core.vcfindex;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.commons.core.connectors.variant.readers.VcfDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.readers.VcfFileDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfIndexDataWriter;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfSqliteIndexDataWriter;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/30/13
 * Time: 7:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfIndexTest {

    private Long start, end;
    private Vcf4Reader vcf;
    private String path = "/opt/data/";
    private String vcfFileName;
    private String pathIndex;
    private String dbFilename;


    @Rule
    public TestName name = new TestName();


    @Before
    public void setUp() throws Exception {
        vcfFileName = path + "file.vcf";
        pathIndex = path + "jstats/";
        dbFilename = path + "jstats/index.db";
        start = System.currentTimeMillis();

    }

    @After
    public void tearDown() throws Exception {
        end = System.currentTimeMillis();
        System.out.println("Time " + name.getMethodName() + ": " + (end - start));

    }

    @Test
    public void testRunner() throws Exception {

        VcfDataReader vcfReader = new VcfFileDataReader(vcfFileName);
        VcfIndexDataWriter vcfWriter = new VcfSqliteIndexDataWriter(dbFilename);

        VcfIndex.runner(vcfReader, vcfWriter);

    }
}
