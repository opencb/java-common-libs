package org.opencb.commons.bioformats.variant;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;
import org.opencb.commons.bioformats.variant.utils.stats.VariantStats;
import org.opencb.commons.test.GenericTest;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantTest extends GenericTest {

    private Variant v1;
    private Variant v2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        List<String> sampleNames = Arrays.asList("NA001", "NA002", "NA003");
        String[] fields1 = new String[]{"1", "100000", "rs1100000", "A", "T,G", "40", "PASS",
            "DP=5;AP=10;H2", "GT:DP", "1/1:4", "1/0:2", "0/0:3"};
        String[] fields2 = new String[]{"1", "200000", "rs1200000", "G", "T", "30", "LowQual",
            "DP=2;AP=5", "GT:DP", "1/1:3", "1/1:1", "0/0:5"};

        v1 = VariantFactory.createVariantFromVcf(sampleNames, fields1);
        v2 = VariantFactory.createVariantFromVcf(sampleNames, fields2);

        VariantStats stats1 = new VariantStats("1", 100000, "A", "T,G", 0.01, 0.30, "A", "A/T", 2, 0, 1, true, 0.02, 0.10, 0.30, 0.15);
        VariantStats stats2 = new VariantStats("1", 200000, "G", "T", 0.05, 0.20, "T", "T/T", 1, 1, 0, true, 0.05, 0.20, 0.20, 0.10);
        v1.setStats(stats1);
        v2.setStats(stats2);

        VariantEffect eff1 = new VariantEffect("1", 100000, "A", "T", "", "RP11-206L10.6",
                "intron", "processed_transcript", "1", 714473, 739298, "1", "", "", "",
                "ENSG00000237491", "ENST00000429505", "RP11-206L10.6", "SO:0001627",
                "intron_variant", "In intron", "feature", -1, "", "");
        VariantEffect eff2 = new VariantEffect("1", 100000, "A", "T", "ENST00000358533", "AL669831.1",
                "downstream", "protein_coding", "1", 722513, 727513, "1", "", "", "",
                "ENSG00000197049", "ENST00000358533", "AL669831.1", "SO:0001633",
                "5KB_downstream_variant", "Within 5 kb downstream of the 3 prime end of a transcript", "feature", -1, "", "");
        VariantEffect eff3 = new VariantEffect("1", 200000, "C", "A", "ENST00000434264", "RP11-206L10.7",
                "downstream", "lincRNA", "1", 720070, 725070, "1", "", "", "",
                "ENSG00000242937", "ENST00000434264", "RP11-206L10.7", "SO:0001633",
                "5KB_downstream_variant", "Within 5 kb downstream of the 3 prime end of a transcript", "feature", -1, "", "");
        v1.setEffect(Arrays.asList(eff1, eff2));
        v2.setEffect(Arrays.asList(eff3));

    }

    @Override
    @After
    public void tearDown() throws Exception {
        v1 = null;
        v2 = null;
        super.tearDown();
    }

    @Test
    public void testGetChromosome() throws Exception {
        assertEquals(v1.getChromosome(), "1");
    }

    @Test
    public void testSetChromosome() throws Exception {
        v1.setChromosome("2");
        assertEquals(v1.getChromosome(), "2");
    }

    @Test
    public void testGetPosition() throws Exception {
        assertEquals(v1.getPosition(), 100000);
    }

    @Test
    public void testSetPosition() throws Exception {
        v1.setPosition(1);
        assertEquals(v1.getPosition(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPositionNegative() {
        v1.setPosition(-1);
    }

    @Test
    public void testGetReference() throws Exception {
        assertEquals(v2.getReference(), "G");
    }

    @Test
    public void testSetReference() throws Exception {
        v2.setReference("C");
        assertEquals(v2.getReference(), "C");
    }

    @Test
    public void testGetAlternate() throws Exception {
        assertEquals(v2.getAlternate(), "T");
    }

    @Test
    public void testSetAlternate() throws Exception {
        v2.setAlternate("A");
        assertEquals(v2.getAlternate(), "A");
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(v1.getId(), "rs1100000");
    }

    @Test
    public void testSetId() throws Exception {
        v1.setId("rsTEST");
        assertEquals(v1.getId(), "rsTEST");
    }

    @Test
    public void testGetFormat() throws Exception {

    }

    @Test
    public void testSetFormat() throws Exception {

    }

    @Test
    public void testGetSamplesData() throws Exception {

    }

    @Test
    public void testGetSampleData() throws Exception {

    }

    @Test
    public void testGetStats() throws Exception {

    }

    @Test
    public void testSetStats() throws Exception {

    }

    @Test
    public void testGetEffect() throws Exception {

    }

    @Test
    public void testSetEffect() throws Exception {

    }

    @Test
    public void testGetAttributes() throws Exception {

    }

    @Test
    public void testSetAttributes() throws Exception {

    }

    @Test
    public void testAddEffect() throws Exception {

    }

    @Test
    public void testAddId() throws Exception {

    }

    @Test
    public void testAddAttribute() throws Exception {

    }

    @Test
    public void testGetAttribute() throws Exception {

    }

    @Test
    public void testContainsAttribute() throws Exception {

    }

    @Test
    public void testAddSampleData() throws Exception {

    }

    @Test
    public void testGetSampleData1() throws Exception {

    }

    @Test
    public void testGetSampleNames() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

    }

    @Test
    public void testGetAltAlleles() throws Exception {

    }

    @Test
    public void testIsIndel() throws Exception {

    }
}
