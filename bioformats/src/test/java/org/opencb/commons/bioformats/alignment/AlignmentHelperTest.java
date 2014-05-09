package org.opencb.commons.bioformats.alignment;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.samtools.*;
import net.sf.samtools.util.StringLineReader;
import org.junit.*;
import org.opencb.commons.bioformats.alignment.sam.io.AlignmentSamDataReader;
import org.opencb.commons.bioformats.alignment.sam.io.AlignmentSamDataWriter;
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
        String readSequence = "AAAACCCCGGGGTTTTAAAANCCCCGGGGTTTTAAAACCCC";
        // 20M1I20M - middle
        System.out.println("20M1I20M");
        elements = new LinkedList<>();
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(1, CigarOperator.I));
        elements.add(new CigarElement(20, CigarOperator.M));
        record.setCigar(new Cigar(elements));
        record.setReadString(readSequence);
        
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(20, Alignment.AlignmentDifference.INSERTION, "N"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
//            System.out.println("got " + result.get(i).toString());
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
        //CompleteDifferencesFromReferenceTest(record, referenceSequence, readSequence, expResult); //JJ Added test

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
        readSequence = "GGGAAAACCCCGGACGGTTTTAAAAGTGTGCCCCGGGGTTTTAAAACCCCGGGGTTTTAAN";
        record.setReadString(readSequence);

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

        CompleteDifferencesFromReferenceTest(record, referenceSequence, readSequence, expResult); //JJ Added test
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
        String readSequence = "AAAACCCCGGGGTTTTAAAACCGGGGTTTTAAAACCCC";
        
        // 20M2D18M - middle
        System.out.println("20M2D18M");
        elements = new LinkedList<>();
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(2, CigarOperator.D));
        elements.add(new CigarElement(18, CigarOperator.M));
        record.setCigar(new Cigar(elements));
        record.setReadString(readSequence);
        
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(20, Alignment.AlignmentDifference.DELETION, "CC"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
        CompleteDifferencesFromReferenceTest(record, referenceSequence, readSequence, expResult); //JJ Added test
        
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
        readSequence = "ACCCCGGGGTTAAAACCCCGTTAAAACCCCGGGGTTTTAA";
        record.setReadString(readSequence);
        
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
        CompleteDifferencesFromReferenceTest(record, referenceSequence, readSequence, expResult); //JJ Added test
        
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
        String readSequence =      "ACAACGGGTGGGTTTTAAAACCGGGGTTTAAAAACCGT";
        
        // 20M2D18M - middle and end
        System.out.println("20M2D18M");
        elements = new LinkedList<>();
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(2, CigarOperator.D));
        elements.add(new CigarElement(18, CigarOperator.M));
        record.setCigar(new Cigar(elements));
        record.setReadString(readSequence);
        
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
        CompleteDifferencesFromReferenceTest(record, referenceSequence, readSequence, expResult); //JJ Added test
    }


    /**
     * Test of getDifferencesFromCigar method, of class AlignmentHelper.
     */
    @Test
    public void testGetDifferencesFromCigarClipping() {
        SAMRecord record = new SAMRecord(new SAMFileHeader());
        List<CigarElement> elements = null;
        List expResult = null, result = null;
        String referenceSequence;
        String readSequence;


        // 2H20M8S - start (hard) and end (soft)
        referenceSequence = "AAAACCCCGGGGTTTTAAAACC"; // 22 nt
        readSequence=         "AACCCCGGGGTTTTAAAACCCCGGGGTT";
        System.out.println("2H20M8S");
        elements = new LinkedList<>();
        elements.add(new CigarElement(2, CigarOperator.H));
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(8, CigarOperator.S));
        record.setCigar(new Cigar(elements));
        record.setReadString(readSequence);

        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(0, Alignment.AlignmentDifference.HARD_CLIPPING, 2));
        expResult.add(new Alignment.AlignmentDifference(22, Alignment.AlignmentDifference.SOFT_CLIPPING, "CCGGGGTT"));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
//        for (int i = 0; i < result.size(); i++) {
//            System.out.println(result.get(i).toString());
//        }
//        assertEquals(expResult.size(), result.size());

        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }
        CompleteDifferencesFromReferenceTest(record, referenceSequence, readSequence, expResult); //JJ Added test

        // 2S20M10H - start (soft) and end (hard)
        referenceSequence = "GTAAAACCCCGGGGTTTTAAAACC";
        readSequence = "GTAAAACCCCGGGGTTTTAAAA";
        System.out.println("2S20M10H");
        elements = new LinkedList<>();
        elements.add(new CigarElement(2, CigarOperator.S));
        elements.add(new CigarElement(20, CigarOperator.M));
        elements.add(new CigarElement(2, CigarOperator.H));
        record.setCigar(new Cigar(elements));
        record.setReadString(readSequence);
         
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(0, Alignment.AlignmentDifference.SOFT_CLIPPING, 2));  //We expect don't save que SoftClipping if it's equal to the reference
        expResult.add(new Alignment.AlignmentDifference(22, Alignment.AlignmentDifference.HARD_CLIPPING, 2));
        result = AlignmentHelper.getDifferencesFromCigar(record, referenceSequence);
//        for (int i = 0; i < result.size(); i++) {
//            System.out.println(result.get(i).toString());
//        }
        assertEquals(expResult.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + result.get(i).toString(),
                        expResult.get(i).equals(result.get(i)));
        }


        CompleteDifferencesFromReferenceTest(record, referenceSequence, readSequence, expResult); //JJ Added test
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
        String readSequence = "AGATAAGGATA";
        
        // 6M1P1I4M - middle (padding and insert)
        System.out.println("6M1P1I4M");
        elements = new LinkedList<>();
        elements.add(new CigarElement(6, CigarOperator.M));
        elements.add(new CigarElement(1, CigarOperator.P));
        elements.add(new CigarElement(1, CigarOperator.I));
        elements.add(new CigarElement(4, CigarOperator.M));
        record.setCigar(new Cigar(elements));
        record.setReadString(readSequence);
         
        expResult = new LinkedList<>();
        expResult.add(new Alignment.AlignmentDifference(6, Alignment.AlignmentDifference.PADDING, 1));
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
        CompleteDifferencesFromReferenceTest(record, referenceSequence, readSequence, expResult); //JJ Added test
    }




    private void CompleteDifferencesFromReferenceTest(SAMRecord record, String referenceSequence, String readSequence, List<Alignment.AlignmentDifference> expResult){
        Alignment alignment = new Alignment(record, null);
        try {
            AlignmentHelper.completeDifferencesFromReference(alignment,referenceSequence,alignment.getUnclippedStart());
        } catch (ShortReferenceSequenceException e) {
            assertTrue(e.getMessage(),false);
        }

        assertEquals(expResult.size(), alignment.getDifferences().size());

        for (int i = 0; i < alignment.getDifferences().size(); i++) {
            assertTrue("Expected " + expResult.get(i).toString() + " but got " + alignment.getDifferences().get(i).toString(),
                    expResult.get(i).equals(alignment.getDifferences().get(i)));
        }
        Cigar cigar = new Cigar();
        String sequenceFromDifferences = null;
        try {
            sequenceFromDifferences = AlignmentHelper.getSequenceFromDifferences(expResult, readSequence.length(), referenceSequence, cigar);
        } catch (ShortReferenceSequenceException e) {
            assertTrue(e.getMessage(), false);
        }
        assertEquals("getSequenceFromDifferences bad sequence result. ", readSequence, sequenceFromDifferences);
        //assertEquals("getSequenceFromDifferences bad cigar result. ", record.getCigar(), cigar);
        System.out.println("original   : " + record.getCigar()  + "\n" +
                           "equivalent : " + cigar);
    }


    @Test
    public void getSequenceTest(){

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

    @Test
    public void getSequenceFromDifferencesTest(){
        //String cigar = "40S7M1D53M";
        
        List<Alignment.AlignmentDifference> differenceList = new LinkedList<>();
        differenceList.add(new Alignment.AlignmentDifference(0,'H', 8));
        differenceList.add(new Alignment.AlignmentDifference(8,'S', "AAATATAAACAATACACAATACAGGCTAATGAAGAAGGGT"));
        differenceList.add(new Alignment.AlignmentDifference(48,'D', 1));
        differenceList.add(new Alignment.AlignmentDifference(61,'I', "XXXX"));


        String readSequence =              "AAATATAAACAATACACAATACAGGCTAATGAAGAAGGGTATAAGATTTTTTXXXXTTTTTTTTTGAGACGGAATTTCACTCTTGTCACCCAGGCTGGAGTGCA";
        String referenceSequence = "HHHHHHHHAAATATAAACAATACACAATACAGGCTAATGAAGAAGGGT_ATAAGATTTTTTTTTTTTTTTGAGACGGAATTTCACTCTTGTCACCCAGGCTGGAGTGCAA";

        String sequenceFromDifferences = null;
        try {
            sequenceFromDifferences = AlignmentHelper.getSequenceFromDifferences(differenceList, readSequence.length(), referenceSequence);
        } catch (ShortReferenceSequenceException e) {
            assertTrue(e.getMessage(), false);
        }
        System.out.println("reference: " + referenceSequence);
        System.out.println("read     : " + readSequence);
        System.out.println("reconstru: " + sequenceFromDifferences);
        assertEquals("reconstructed sequence should equal to the read ", sequenceFromDifferences, readSequence);
    }
    @Test
    public void getCigarFromDifferencesTest(){
        List<Alignment.AlignmentDifference> differenceList = new LinkedList<>();
        differenceList.add(new Alignment.AlignmentDifference(3,'D', 3));
        differenceList.add(new Alignment.AlignmentDifference(6,'I', "<<insertion>>"));
        differenceList.add(new Alignment.AlignmentDifference(20,'D', 7));
        differenceList.add(new Alignment.AlignmentDifference(29,'S', "<<soft>>"));

        String expectedCigar = "3=3D13I14=7D2=8S";

        Cigar cigar = new Cigar();
        String sequenceFromDifferences = null;
        try {
            sequenceFromDifferences = AlignmentHelper.getSequenceFromDifferences(differenceList, 40,
                    "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", cigar);
        } catch (ShortReferenceSequenceException e) {
            assertTrue(e.getMessage(), false);
        }

        System.out.println(sequenceFromDifferences);
        assertEquals(expectedCigar, cigar.toString());
    }

    @Test(expected = ShortReferenceSequenceException.class)
    public void ShortReferenceSequenceExceptionTest() throws ShortReferenceSequenceException {
        List<Alignment.AlignmentDifference> differenceList = new LinkedList<>();
        differenceList.add(new Alignment.AlignmentDifference(0,'S', "AAATATAAACAATACACAATACAGGCTAATGAAGAAGGGT"));
        differenceList.add(new Alignment.AlignmentDifference(47,'D', 1));
        differenceList.add(new Alignment.AlignmentDifference(48,'H', 8));


        String referenceSequence = "VERY_SHORT_REFERENCE_SEQUENCE";

        AlignmentHelper.getSequenceFromDifferences(differenceList, 100, referenceSequence);



    }

}
