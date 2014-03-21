package org.opencb.commons.bioformats.alignment;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import net.sf.samtools.*;
import org.junit.*;
import org.opencb.commons.bioformats.feature.Region;
import org.opencb.commons.containers.map.QueryOptions;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cgonzalez@cipf.es>
 */
public class AlignmentHelperTest {
    
    public AlignmentHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getDifferencesFromCigar method, of class AlignmentHelper.
     */
    @Test
    public void testGetDifferencesFromCigarInsertions() {
        SAMRecord record = new SAMRecord(new SAMFileHeader());
        List<CigarElement> elements = null;
        List expResult = null, result = null;
        String referenceSequence = "AAAACCCCGGGGTTTTAAAACCCCGGGGTTTTAAAACCCCGGGGTTTTAAAACCCCGGGGTTTTAAAACCCCGGGGTTTT"; // 80 nt
        
        // 20M1I20M - middle
        System.out.println("20M1I20M");
        elements = new LinkedList<>();
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(1, CigarOperator.I));
        elements.add(new CigarElement(20, CigarOperator.M));
        record.setCigar(new Cigar(elements));
        record.setReadString("AAAACCCCGGGGTTTTAAAANCCCCGGGGTTTTAAAACCCC");
        
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(20, Alignment.AlignmentDifference.INSERTION, "N"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
//            System.out.println("got " + result.get(i).toString());
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
        
        // 3I10M2I10M5I30M1I - beginning, middle and end
        System.out.println("3I10M2I10M5I30M1I");
        elements = new LinkedList<>();
        elements.add(new CigarElement(3, CigarOperator.I));
        elements.add(new CigarElement(10, CigarOperator.M));
        elements.add(new CigarElement(2, CigarOperator.I));
        elements.add(new CigarElement(10, CigarOperator.M));
        elements.add(new CigarElement(5, CigarOperator.I));
        elements.add(new CigarElement(30, CigarOperator.M));
        elements.add(new CigarElement(1, CigarOperator.I));
        record.setCigar(new Cigar(elements));
        record.setReadString("GGGAAAACCCCGGACGGTTTTAAAAGTGTGCCCCGGGGTTTTAAAACCCCGGGGTTTTAAN");
        
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(0, Alignment.AlignmentDifference.INSERTION, "GGG"));
        expResult.add(new Alignment.AlignmentDifference(10, Alignment.AlignmentDifference.INSERTION, "AC"));
        expResult.add(new Alignment.AlignmentDifference(20, Alignment.AlignmentDifference.INSERTION, "GTGTG"));
        expResult.add(new Alignment.AlignmentDifference(50, Alignment.AlignmentDifference.INSERTION, "N"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
    }
    
    
    /**
     * Test of getDifferencesFromCigar method, of class AlignmentHelper.
     */
    @Test
    public void testGetDifferencesFromCigarDeletions() {
        SAMRecord record = new SAMRecord(new SAMFileHeader());
        List<CigarElement> elements = null;
        List expResult = null, result = null;
        String referenceSequence = "AAAACCCCGGGGTTTTAAAACCCCGGGGTTTTAAAACCCCGGGGTTTTAAAACCCCGGGGTTTTAAAACCCCGGGGTTTT"; // 80 nt
        
        // 20M2D18M - middle
        System.out.println("20M2D18M");
        elements = new LinkedList<>();
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(2, CigarOperator.D));
        elements.add(new CigarElement(18, CigarOperator.M));
        record.setCigar(new Cigar(elements));
        record.setReadString("AAAACCCCGGGGTTTTAAAACCGGGGTTTTAAAACCCC");
        
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(20, Alignment.AlignmentDifference.DELETION, "CC"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
        
        // 3D10M2D10M5D19M - beginning and middle 
        System.out.println("3D10M2D10M5D19M");
        elements = new LinkedList<>();
        elements.add(new CigarElement(3, CigarOperator.D));
        elements.add(new CigarElement(10, CigarOperator.M));
        elements.add(new CigarElement(2, CigarOperator.D));
        elements.add(new CigarElement(10, CigarOperator.M));
        elements.add(new CigarElement(5, CigarOperator.D));
        elements.add(new CigarElement(20, CigarOperator.M));
        record.setCigar(new Cigar(elements));
        record.setReadString("ACCCCGGGGTTAAAACCCCGTTAAAACCCCGGGGTTTTAA");
        
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(0, Alignment.AlignmentDifference.DELETION, "AAA"));
        expResult.add(new Alignment.AlignmentDifference(13, Alignment.AlignmentDifference.DELETION, "TT"));
        expResult.add(new Alignment.AlignmentDifference(25, Alignment.AlignmentDifference.DELETION, "GGGTT"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
        
        // TODO Test deletion at the end? Does it make sense?
    }
    
    
    /**
     * Test of getDifferencesFromCigar method, of class AlignmentHelper.
     */
    @Test
    public void testGetDifferencesFromCigarMismatches() {
        SAMRecord record = new SAMRecord(new SAMFileHeader());
        List<CigarElement> elements = null;
        List expResult = null, result = null;
        String referenceSequence = "AAAACCCCGGGGTTTTAAAACCCCGGGGTTTTAAAACCCC"; // 40 nt
        
        // 20M2D18M - middle and end
        System.out.println("20M2D18M");
        elements = new LinkedList<>();
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(2, CigarOperator.D));
        elements.add(new CigarElement(18, CigarOperator.M));
        record.setCigar(new Cigar(elements));
        record.setReadString("ACAACGGGTGGGTTTTAAAACCGGGGTTTAAAAACCGT");
        
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(1, Alignment.AlignmentDifference.MISMATCH, "C"));
        expResult.add(new Alignment.AlignmentDifference(5, Alignment.AlignmentDifference.MISMATCH, "GGGT"));
        expResult.add(new Alignment.AlignmentDifference(20, Alignment.AlignmentDifference.DELETION, "CC"));
        expResult.add(new Alignment.AlignmentDifference(31, Alignment.AlignmentDifference.MISMATCH, "A"));
        expResult.add(new Alignment.AlignmentDifference(38, Alignment.AlignmentDifference.MISMATCH, "GT"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
    }
    
    
    /**
     * Test of getDifferencesFromCigar method, of class AlignmentHelper.
     */
    @Test
    public void testGetDifferencesFromCigarClipping() {
        SAMRecord record = new SAMRecord(new SAMFileHeader());
        List<CigarElement> elements = null;
        List expResult = null, result = null;
        String referenceSequence = "AAAACCCCGGGGTTTTAAAACC"; // 22 nt
        
        // 2H20M8S - start (hard) and end (soft)
        System.out.println("2H20M8S");
        elements = new LinkedList<>();
        elements.add(new CigarElement(2, CigarOperator.H));
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(8, CigarOperator.S));
        record.setCigar(new Cigar(elements));
        record.setReadString("AACCCCGGGGTTTTAAAACCCCGGGGTT");
         
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(0, Alignment.AlignmentDifference.HARD_CLIPPING, "AA"));
        expResult.add(new Alignment.AlignmentDifference(22, Alignment.AlignmentDifference.SOFT_CLIPPING, "CCGGGGTT"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
//        for (int i = 0; i < result.size(); i++) {
//            System.out.println(result.get(i).toString());
//        }
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
        
        
        // 2S20M10H - start (soft) and end (hard)
        System.out.println("2S20M10H");
        elements = new LinkedList<>();
        elements.add(new CigarElement(2, CigarOperator.S));
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(2, CigarOperator.H));
        record.setCigar(new Cigar(elements));
        record.setReadString("GTAAAACCCCGGGGTTTTAAAA");
         
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(0, Alignment.AlignmentDifference.SOFT_CLIPPING, "GT"));
        expResult.add(new Alignment.AlignmentDifference(22, Alignment.AlignmentDifference.HARD_CLIPPING, "CC"));
        result = AlignmentHelper.getDifferencesFromCigar(record, "GTAAAACCCCGGGGTTTTAAAACC");
//        for (int i = 0; i < result.size(); i++) {
//            System.out.println(result.get(i).toString());
//        }
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
    }
    
    /**
     * Test of getDifferencesFromCigar method, of class AlignmentHelper.
     */
    @Test
    public void testGetDifferencesFromCigarPadding() {
        SAMRecord record = new SAMRecord(new SAMFileHeader());
        List<CigarElement> elements = null;
        List expResult = null, result = null;
        String referenceSequence = "AGATAAGATA"; // 10 nt
        
        // 6M1P1I4M - middle (padding and insert)
        System.out.println("6M1P1I4M");
        elements = new LinkedList<>();
        elements.add(new CigarElement(6, CigarOperator.M));
        elements.add(new CigarElement(1, CigarOperator.P));
        elements.add(new CigarElement(1, CigarOperator.I));
        elements.add(new CigarElement(4, CigarOperator.M));
        record.setCigar(new Cigar(elements));
        record.setReadString("AGATAAGGATA");
         
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(6, Alignment.AlignmentDifference.PADDING, ""));
        expResult.add(new Alignment.AlignmentDifference(6, Alignment.AlignmentDifference.INSERTION, "G"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
//        for (int i = 0; i < result.size(); i++) {
//            System.out.println(result.get(i).toString());
//        }
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
    }
    @Test
    public void getSequence(){

        try {
            String sequence = AlignmentHelper.getSequence(new Region("1", 1000000, 1001000), new QueryOptions());

            assertEquals(sequence, "TGGGCACAGCCTCACCCAGGAAAGCAGCTGGGGGTCCACTGGGCTCAGGGAAGACCCCCTGCCAG" +
                    "GGAGACCCCAGGCGCCTGAATGGCCACGGGAAGGAAAACCTACCAGCCCCTCCGTGTGTCCTCCTGGCACATGGCGACCT" +
                    "CCATGACCCGACGAGGGTGCGGGGCCCGGGGCAGGGTGGCCAGGTGCGGGGGTGCGGGGCCCGGGGCAGCTGCCCTCGGT" +
                    "GGGAGGGGTGTGGTGTGGTCTGCGGGGCCCTGGGGGGGTGTGGTGGGGTCTGCGGGGCCCTGGGGGGGTGTGGTGTGGTC" +
                    "TGCGGGGCCCTGGGGGGGTGTGGTGGGGTCTGCGGGGCCCTGGGGGGGTGTGGTGGGGTCTGCGGGGCCCTGGGGGGGTG" +
                    "TGGTGGGGTCTGCGGGGCCCTGGGGGGGTGTGGTGTGGTCTGCGGGGCCCTGGGGGGGTGTGGTGGGGTCTGCGGGGCCC" +
                    "TGGGGGGGTGTGGTGTGGTCTGCGGGGCCCTGGGGGGGTGTGGTGGGGTCTGCGGGGCCCTGGGGGGGTGTGGTGGGGTC" +
                    "TGCGGGGCCCTGGGGGGGGTGGGGTCTGCGGGGCCCTGGGGGTGTTGTGGTGGGGTCTGCGGGGCCCTGGGGGGGTGTGG" +
                    "TGGGGTCTGCGGTGCCCTCGGGGGGTGTGGTGGGGTCTGCGGGGCCCTGGGGGGGTGTGGTGGGGTCTGGGGGGCCCTAA" +
                    "GCTTAGATGCAGGTCTCTCCCTGGCAGCCCCTCAAGGCCACGAGGATCAGTGCTCGGAGCCTGGAGGGCTGTGTGCAGGA" +
                    "GTAGCAGGGCCACTGATGCCAGCGGGAAGGCCAGGCAGGGCTTCTGGGTGGAGTTCAAGGTGCATCCTGACCGCTGTCAC" +
                    "CTTCAGACTCTGTCCCCTGGGGCTGGGGCAAGTGCCCGATGGGAGCGCAGGGTCTGGGACTGTAGGGTCCAGCCCTACGG" +
                    "AGCTTAGCAGGTGTTCTCCCCGTGTGTGGAGATGAGAGATTGTAATAAATAAAGAC");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
