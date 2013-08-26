package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/26/13
 * Time: 1:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfRecordStatsTest {

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
    public void testCalculateStats() throws Exception {

        VcfRecord v_record;
        VcfRecordStat v_stat;


        while((v_record = vcf.read()) != null){
              v_stat = VcfRecordStats.calculateStats(v_record);
            System.out.println(v_stat);
        }

    }

    @Test
    public void testCalculateStatsList() throws Exception {

        List<VcfRecord> list_vcf_records = vcf.readAll();
        List<VcfRecordStat> list_vcf_stats = VcfRecordStats.calculateStats(list_vcf_records);
        System.out.println(list_vcf_stats);

    }
}
