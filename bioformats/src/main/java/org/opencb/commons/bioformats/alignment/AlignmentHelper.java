package org.opencb.commons.bioformats.alignment;

import java.util.LinkedList;
import java.util.List;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
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
    public static List<Alignment.AlignmentDifference> getDifferencesFromCigar(SAMRecord record, String refStr, boolean showSoftClipping) {
        List<Alignment.AlignmentDifference> differences = new LinkedList<>();
        
        String readStr = record.getReadString();
        StringBuilder diffStr = new StringBuilder();
        int index = 0;
        int indexRef = 0;
        
        for (CigarElement cigarEl : record.getCigar().getCigarElements()) {
            CigarOperator cigarOp = cigarEl.getOperator();
            int cigarLen = cigarEl.getLength();
//            logger.info(cigarOp + " found" + " index:" + index + " indexRef:" + indexRef + " cigarLen:" + cigarLen);

            switch (cigarOp) {
                case M:
                case EQ:
                case X:
                    String subref = refStr.substring(indexRef, indexRef + cigarLen);
                    String subread = readStr.substring(index, index + cigarLen);
                    diffStr.append(getDiff(subref, subread));
                    index = index + cigarLen;
                    indexRef = indexRef + cigarLen;
                    break;
                case I:
                    diffStr.append(readStr.substring(index, index + cigarLen).toLowerCase());
                    index = index + cigarLen;
                    // TODO save insertions
                    break;
                case D:
                    for (int bi = 0; bi < cigarLen; bi++) {
                        diffStr.append("d");
                    }
                    indexRef = indexRef + cigarLen;
                    break;
                case N:
                    for (int bi = 0; bi < cigarLen; bi++) {
                        diffStr.append("n");
                    }
                    indexRef = indexRef + cigarLen;
                    break;
                case S:
                    if (showSoftClipping) {
                        subread = readStr.substring(index, index + cigarLen);
                        diffStr.append(subread);
                        index = index + cigarLen;
                        indexRef = indexRef + cigarLen;
                    } else {
                        for (int bi = 0; bi < cigarLen; bi++) {
                            diffStr.append(" ");
                        }
                        index = index + cigarLen;
                        indexRef = indexRef + cigarLen;
                    }
                    break;
                case H:
                    for (int bi = 0; bi < cigarLen; bi++) {
                        diffStr.append("h");
                    }
                    indexRef = indexRef + cigarLen;
                    break;
                case P:
                    for (int bi = 0; bi < cigarLen; bi++) {
                        diffStr.append("p");
                    }
                    indexRef = indexRef + cigarLen;
                    break;
            }
        }
        
        return differences;
    }
    
    private static String getDiff(String refStr, String readStr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < refStr.length(); i++) {
            if (refStr.charAt(i) != readStr.charAt(i)) {
                sb.append(readStr.charAt(i));
            } else {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

}
