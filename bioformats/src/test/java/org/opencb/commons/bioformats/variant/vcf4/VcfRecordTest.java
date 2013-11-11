package org.opencb.commons.bioformats.variant.vcf4;

import com.google.common.collect.Lists;
import org.junit.*;
import org.opencb.commons.bioformats.feature.Genotype;
import org.opencb.commons.test.GenericTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 11/5/13
 * Time: 10:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class VcfRecordTest extends GenericTest {

    private static List<String> sampleName;
    private static String[] fields;
    private static String line = "1\t69735\t.\tA\tG\t1014.27\tPASS\t.\tGT:AD:DP:GQ:PL\t0/0:.:1874:.:.\t0/0:.:1265:.:.\t0/1:44,35:76:99:1036,0,1162\t0/1:37,34:69:99:1049,0,939";
    private VcfRecord record;

    @BeforeClass
    public static void createSampleNames() {
        sampleName = new ArrayList<>();
        sampleName.add("sample_A");
        sampleName.add("sample_B");
        sampleName.add("sample_C");
        sampleName.add("sample_D");

        fields = line.split("\t");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        record = new VcfRecord(fields, sampleName);
    }

    @After
    public void tearDown() throws Exception {
        record = null;
        super.tearDown();
    }

    @Test
    public void testToString() throws Exception {
        System.out.println(record.toString());

    }

    @Test
    public void testGetChromosome() throws Exception {

        assertEquals(record.getChromosome(), "1");

    }

    @Test
    public void testSetChromosome() throws Exception {
        record.setChromosome("2");
        assertEquals(record.getChromosome(), "2");

    }

    @Test
    public void testGetPosition() throws Exception {

        assertEquals(record.getPosition(), 69735);

    }

    @Test
    public void testSetPosition() throws Exception {
        record.setPosition(70000);
        assertEquals(record.getPosition(), 70000);

    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(record.getId(), ".");

    }

    @Test
    public void testSetId() throws Exception {

        record.setId("rsID");
        assertEquals(record.getId(), "rsID");

    }

    @Test
    public void testGetReference() throws Exception {
        assertEquals(record.getReference(), "A");

    }

    @Test
    public void testSetReference() throws Exception {
        record.setReference("C");
        assertEquals(record.getReference(), "C");

    }

    @Test
    public void testGetAlternate() throws Exception {
        assertEquals(record.getAlternate(), "G");

    }

    @Test
    public void testSetAlternate() throws Exception {
        record.setAlternate("A");
        assertEquals(record.getAlternate(), "A");

    }

    @Test
    public void testGetQuality() throws Exception {
        assertEquals(record.getQuality(), "1014.27");

    }

    @Test
    public void testSetQuality() throws Exception {
        record.setQuality("10.6");
        assertEquals(record.getQuality(), "10.6");

    }

    @Test
    public void testGetFilter() throws Exception {
        assertEquals(record.getFilter(), "PASS");

    }

    @Test
    public void testSetFilter() throws Exception {
        record.setFilter("STD_FILTER");
        assertEquals(record.getFilter(), "STD_FILTER");

    }

    @Test
    public void testGetInfo() throws Exception {
        assertEquals(record.getInfo(), ".");

    }

    @Test
    public void testSetInfo() throws Exception {
        record.setInfo("TEST=ACTG");
        assertEquals(record.getInfo(), "TEST=ACTG");

    }

    @Test
    public void testGetFormat() throws Exception {
        assertEquals(record.getFormat(), "GT:AD:DP:GQ:PL");

    }

    @Test
    public void testSetFormat() throws Exception {
        record.setFormat("AS");
        assertEquals(record.getFormat(), "AS");

    }

    @Test
    public void testGetAltAlleles() throws Exception {

        String[] alleles = record.getAlternate().split(",");
        assertArrayEquals(alleles, record.getAltAlleles());

    }

    @Test
    public void testGetValueFormatSample() throws Exception {

        assertEquals(record.getValueFormatSample("sample_A", "DP"), "1874");

    }

    @Test
    public void testGetSampleGenotype() throws Exception {

        Genotype g = new Genotype("0/1");

        assertEquals(g, record.getSampleGenotype("sample_C"));

    }

    @Test
    public void testGetSampleNames() throws Exception {

        List<String> list_aux_1 = Lists.newArrayList(record.getSampleNames());
        List<String> list_aux_2 = Lists.newArrayList(sampleName);
        Collections.sort(list_aux_1);
        Collections.sort(list_aux_2);


        assertEquals(list_aux_1, list_aux_2);

    }

    @Test
    public void testGetSampleData() throws Exception {

        Map<String, String> m = record.getSampleData("sample_A");
        assertEquals(m.get("DP"), "1874");
    }

    @Test
    public void testGetSampleData2() throws Exception {

        assertNotNull(record.getSampleData());
    }

    @Test
    public void testGetSampleRawData() throws Exception {
        assertEquals(record.getSampleRawData("sample_C"), "0/1:44,35:76:99:1036,0,1162");

    }

    @Test
    public void testGetSampleRawData2() throws Exception {
        assertNotNull(record.getSampleRawData());

    }

    @Test
    public void testAddInfoField() throws Exception {

        record.addInfoField("TEST=TEST");
        assertEquals(record.getInfo(), "TEST=TEST");
        record.addInfoField("TEST2=TEST2");
        assertEquals(record.getInfo(), "TEST=TEST;TEST2=TEST2");

    }

    @Test
    public void testGetStats() throws Exception {
        assert (true);

    }

    @Ignore
    public void testSetStats() throws Exception {

    }

    @Ignore
    public void testGetEffects() throws Exception {

    }

    @Ignore
    public void testSetEffects() throws Exception {

    }

    @Ignore
    public void testAddEffect() throws Exception {

    }
}
