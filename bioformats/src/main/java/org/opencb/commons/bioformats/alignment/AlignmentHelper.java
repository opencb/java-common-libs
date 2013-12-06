package org.opencb.commons.bioformats.alignment;

import java.util.LinkedList;
import java.util.List;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cgonzalez@cipf.es>
 */
public class AlignmentHelper {
    
    /**
     * Given a cigar string, returns a list of alignment differences with 
     * the reference sequence.
     * 
     * @param cigar The input cigar string
     * @return The list of alignment differences
     */
    public static List<Alignment.AlignmentDifference> getDifferencesFromCigar(SAMRecord record, String refStr) {
        List<Alignment.AlignmentDifference> differences = new LinkedList<>();
        
        int index = 0;
        int indexRef = 0;
        
        for (CigarElement element : record.getCigar().getCigarElements()) {
            int cigarLen = element.getLength();
            String subref = refStr.substring(indexRef, indexRef + cigarLen);
            String subread = record.getReadString().substring(index, index + cigarLen);
            Alignment.AlignmentDifference currentDifference = null;

            switch (element.getOperator()) {
                case M:
                case EQ:
                case X:
                    differences.addAll(getMismatchDiff(subref, subread, indexRef));
                    index = index + cigarLen;
                    indexRef = indexRef + cigarLen;
                    break;
                case I:
                    currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.INSERTION, subread);
                    index = index + cigarLen;
                    break;
                case D:
                    currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.DELETION, subref);
                    indexRef = indexRef + cigarLen;
                    break;
                case N:
                    currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.SKIPPED_REGION, subref);
                    indexRef = indexRef + cigarLen;
                    break;
                case S:
                    currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.SOFT_CLIPPING, subread);
                    index = index + cigarLen;
                    indexRef = indexRef + cigarLen;
                    break;
                case H:
                    currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.HARD_CLIPPING, subref);
                    indexRef = indexRef + cigarLen;
                    break;
                case P:
                    currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.PADDING, subref);
                    indexRef = indexRef + cigarLen;
                    break;
            }
            
            if (currentDifference != null) {
                differences.add(currentDifference);
            }
        }
        
        return differences;
    }
    
    
    private static List<Alignment.AlignmentDifference> getMismatchDiff(String referenceSequence, String readSequence, int baseIndex) {
        List<Alignment.AlignmentDifference> differences = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        int foundIndex = 0;
        for (int i = 0; i < referenceSequence.length(); i++) {
            if (referenceSequence.charAt(i) != readSequence.charAt(i)) {
                sb.append(readSequence.charAt(i));
                if (sb.length() == 0) {
                    foundIndex = i;
                }
            } else {
                if (sb.length() > 0) {
                    Alignment.AlignmentDifference difference = 
                            new Alignment.AlignmentDifference(baseIndex + foundIndex, Alignment.AlignmentDifference.MISMATCH, sb.toString());
                    differences.add(difference);
                    sb.setLength(0);
                }
            }
        }
        return differences;
    }
    
}
