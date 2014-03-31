package org.opencb.commons.bioformats.alignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.sf.samtools.*;
import org.opencb.commons.bioformats.feature.Region;
import org.opencb.commons.containers.map.QueryOptions;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cgonzalez@cipf.es>
 */
public class AlignmentHelper {



    /**
     * Given a cigar string, returns a list of alignment differences with 
     * the reference sequence.
     *
     * If the reference sequence is null, M tags in the CIGAR can't be assumed to be
     * correct, so they will be stored as mismatches.
     *
     *
     * @param record The input cigar string
     * @param refStr reference sequence.
     * @param maxStoredSequence Max length for stored sequences. Default = 30
     * @return The list of alignment differences
     */
    public static List<Alignment.AlignmentDifference> getDifferencesFromCigar(SAMRecord record, String refStr, int maxStoredSequence) {
        List<Alignment.AlignmentDifference> differences = new LinkedList<>();

        int index = 0, indexRef = 0, indexMismatchBlock = 0, realStart;
        AlignmentBlock blk;
//        System.out.println("align start = " + record.getAlignmentStart() + 
//                "\t1st block start = " + record.getAlignmentBlocks().get(0).getReferenceStart() + 
//                "\n*****\n" + refStr + "\n" + record.getReadString());

        for (CigarElement element : record.getCigar().getCigarElements()) {
            int cigarLen = element.getLength();
            String subref = null, subread = null;
            Alignment.AlignmentDifference currentDifference = null;

            switch (element.getOperator()) {
                case EQ:
                    blk = record.getAlignmentBlocks().get(indexMismatchBlock);
                    realStart = blk.getReferenceStart() - record.getAlignmentStart();
                    // Picard ignores hard clipping, the indices could be necessary
                    indexRef = realStart >= indexRef ? realStart : indexRef;

                    index = index + record.getAlignmentBlocks().get(indexMismatchBlock).getLength();
                    indexRef = indexRef + record.getAlignmentBlocks().get(indexMismatchBlock).getLength();
                    indexMismatchBlock++;
                    break;
                case M:
                case X:
                    blk = record.getAlignmentBlocks().get(indexMismatchBlock);
                    realStart = blk.getReferenceStart() - record.getAlignmentStart();
                    // Picard ignores hard clipping, the indices could be necessary
                    indexRef = realStart >= indexRef ? realStart : indexRef;
                    subread = record.getReadString().substring(index, Math.min(index + blk.getLength(), record.getReadString().length()));
                    if (refStr == null) {
                        currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.MISMATCH, cigarLen);
                        currentDifference.setSeq(subread);
                    } else {
                        subref = refStr.substring(indexRef, indexRef + blk.getLength());
                        differences.addAll(getMismatchDiff(subref, subread, indexRef));
                    }
                    index = index + record.getAlignmentBlocks().get(indexMismatchBlock).getLength();
                    indexRef = indexRef + record.getAlignmentBlocks().get(indexMismatchBlock).getLength();
                    indexMismatchBlock++;
                    break;
                case I:
                    if (cigarLen < maxStoredSequence) {
                        subread = record.getReadString().substring(index, index + cigarLen);
                    } else { // Get only first 30 characters in the sequence to copy
                        subread = record.getReadString().substring(index, index + maxStoredSequence-3).concat("...");
                    }
                    currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.INSERTION, subread, cigarLen);
                    index = index + cigarLen;
                    break;
                case D:
                    if (refStr == null) {
                        currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.DELETION, cigarLen);
                    } else {
                        if (cigarLen < maxStoredSequence) {
                            subref = refStr.substring(indexRef, indexRef + cigarLen);
                        } else { // Get only first 30 characters in the sequence to copy
                            subref = refStr.substring(indexRef, indexRef + maxStoredSequence-3).concat("...");
                        }
                        currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.DELETION, subref, cigarLen);
                    }
                    indexRef = indexRef + cigarLen;
                    break;
                case N:
                    if (refStr == null) {
                        currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.SKIPPED_REGION, cigarLen);
                    } else {
                        if (cigarLen < maxStoredSequence) {
                            subref = refStr.substring(indexRef, indexRef + cigarLen);
                        } else { // Get only first 30 characters in the sequence to copy
                            subref = refStr.substring(indexRef, indexRef + maxStoredSequence-3).concat("...");
                        }
                        currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.SKIPPED_REGION, subref, cigarLen);
                    }
                    indexRef = indexRef + cigarLen;
                    break;
                case S:
                    subread = record.getReadString().substring(index, index + cigarLen);
                    currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.SOFT_CLIPPING, subread);
                    index = index + cigarLen;
                    indexRef = indexRef + cigarLen;
                    break;
                case H:
                    if (refStr == null) {
                        currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.HARD_CLIPPING, cigarLen);
                    } else {
                        subref = refStr.substring(indexRef, Math.min(indexRef + cigarLen, refStr.length()));
                        currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.HARD_CLIPPING, subref);
                    }
                    indexRef = indexRef + cigarLen;
                    break;
                case P: //TODO jj: Check this
//                  subref = refStr.substring(indexRef, indexRef + cigarLen);
                    currentDifference = new Alignment.AlignmentDifference(indexRef, Alignment.AlignmentDifference.PADDING, "");

//                  indexRef = indexRef + cigarLen;
                    break;
            }

            if (currentDifference != null) {
                differences.add(currentDifference);
            }
        }

        return differences;
    }
    public static List<Alignment.AlignmentDifference> getDifferencesFromCigar(SAMRecord record, String refStr) {
        return getDifferencesFromCigar(record, refStr, 30);
    }

    public static boolean completeDifferencesFromReference(Alignment alignment, String referenceSequence, long referenceSequenceStart){
        int offset = (int) (alignment.getUnclippedStart() - referenceSequenceStart);
        String subRef;
        String subRead;

        List<Alignment.AlignmentDifference> newDifferences =  new LinkedList<>();
        for(Alignment.AlignmentDifference alignmentDifference : alignment.getDifferences()){
            Alignment.AlignmentDifference currentDifference = null;
            try{
                switch (alignmentDifference.getOp()){
                    case Alignment.AlignmentDifference.DELETION:
                        //If is a deletion, there is no seq.
                        if(!alignmentDifference.isAllSequenceStored()){
                            subRef = referenceSequence.substring(
                                    alignmentDifference.getPos() + offset,
                                    alignmentDifference.getPos() + offset + alignmentDifference.getLength()
                            );
                            alignmentDifference.setSeq( subRef );
                        }
                        currentDifference = alignmentDifference;
                        break;
                    case Alignment.AlignmentDifference.MISMATCH:
                        //
                        subRef = referenceSequence.substring(
                                alignmentDifference.getPos() + offset,
                                alignmentDifference.getPos() + offset + alignmentDifference.getLength()
                        );
                        subRead = alignmentDifference.getSeq();
                        newDifferences.addAll(getMismatchDiff(subRef, subRead, alignmentDifference.getPos()));
                        break;
                    case Alignment.AlignmentDifference.HARD_CLIPPING:
                        //
                        subRef = referenceSequence.substring(
                                alignmentDifference.getPos() + offset,
                                alignmentDifference.getPos() + offset + alignmentDifference.getLength()
                        );
                        alignmentDifference.setSeq(subRef);
                        currentDifference = alignmentDifference;
                        break;
                    case Alignment.AlignmentDifference.SOFT_CLIPPING:
                        offset -= alignmentDifference.getLength();
                    case Alignment.AlignmentDifference.INSERTION:
                    case Alignment.AlignmentDifference.PADDING:


                    case Alignment.AlignmentDifference.SKIPPED_REGION:
                        //
                        currentDifference = alignmentDifference;
                        break;
                }

                if(currentDifference != null){
                    newDifferences.add(currentDifference);
                }
            } catch (StringIndexOutOfBoundsException e){
                System.out.println("referenceSequence Out of Bounds in \"Alignment.completeDifferences()\"" + e.toString());
                return false;
            }
        }
        alignment.setDifferences(newDifferences);

        return true;
    }

    /**
     *
     * @param referenceSequence
     * @param readSequence
     * @param baseIndex Position of the subSequence inside the whole sequence
     * @return
     */
    private static List<Alignment.AlignmentDifference> getMismatchDiff(String referenceSequence, String readSequence, int baseIndex) {
        List<Alignment.AlignmentDifference> differences = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        int foundIndex = 0;
        for (int i = 0; i < Math.min(referenceSequence.length(), readSequence.length()); i++) {
            if (referenceSequence.charAt(i) != readSequence.charAt(i)) {
                if (sb.length() == 0) {
                    foundIndex = i;
                }
                sb.append(readSequence.charAt(i));
            } else {
                if (sb.length() > 0) {
                    Alignment.AlignmentDifference difference =
                            new Alignment.AlignmentDifference(baseIndex + foundIndex, Alignment.AlignmentDifference.MISMATCH, sb.toString());
                    differences.add(difference);
                    sb.setLength(0);
                }
            }
        }

        // If a mismatch was found at the end, it can't be appended inside the loop
        if (sb.length() > 0) {
            Alignment.AlignmentDifference difference =
                    new Alignment.AlignmentDifference(baseIndex + foundIndex, Alignment.AlignmentDifference.MISMATCH, sb.toString());
            differences.add(difference);
        }

        return differences;
    }



    public static String getSequenceFromDifferences(List<Alignment.AlignmentDifference> differences, int sequenceSize, String referenceSequence){
        return getSequenceFromDifferences(differences, sequenceSize, referenceSequence, 0);
    }
    public static Cigar getCigarFromDifferences(List<Alignment.AlignmentDifference> differences, int sequenceSize){

        Cigar cigar = new Cigar();
        int index = 0;

        for(Alignment.AlignmentDifference alignmentDifference : differences){
            if(index < alignmentDifference.getPos()){
                cigar.add(new CigarElement(alignmentDifference.getPos()-index, CigarOperator.MATCH_OR_MISMATCH));
                index    += alignmentDifference.getPos()-index;
            } else if(index > alignmentDifference.getPos()) {
                System.out.println("[ERROR] BAD DIFFERENCES ");
            }



            switch (alignmentDifference.getOp()){
                case Alignment.AlignmentDifference.INSERTION:
                    cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.INSERTION));
                    index += alignmentDifference.getLength();
                    break;

                case Alignment.AlignmentDifference.DELETION:
                    cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.DELETION));
                    break;

                case Alignment.AlignmentDifference.MISMATCH:
                    cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.MATCH_OR_MISMATCH));
                    index += alignmentDifference.getLength();
                    break;

                case Alignment.AlignmentDifference.SOFT_CLIPPING:
                    cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.SOFT_CLIP));
                    index += alignmentDifference.getLength();
                    break;

                case Alignment.AlignmentDifference.HARD_CLIPPING:
                    cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.HARD_CLIP));
                    break;

                case Alignment.AlignmentDifference.SKIPPED_REGION:
                    cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.SKIPPED_REGION));
                    break;

                case Alignment.AlignmentDifference.PADDING:
                    cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.PADDING));
                    break;
            }

        }

        if(index < sequenceSize){
            cigar.add(new CigarElement(sequenceSize-index, CigarOperator.MATCH_OR_MISMATCH));
        } else if(index > sequenceSize) {
            System.out.println("[ERROR] TOO MUCH DIFFERENCES ");
        }

        return cigar;
    }
    public static String getSequenceFromDifferences(List<Alignment.AlignmentDifference> differences, int sequenceSize, String referenceSequence, int offset){

        String sequence = "";
        int index = 0;
        int indexRef = offset;

        for(Alignment.AlignmentDifference alignmentDifference : differences){
           // CigarOperator cigarOperator = null;

            if(index < alignmentDifference.getPos()){
                System.out.println("Sequence : " + sequence + " + " + (alignmentDifference.getPos()-index) + "M" + " indexRef="+indexRef);
               // cigar.add(new CigarElement(alignmentDifference.getPos()-index, CigarOperator.MATCH_OR_MISMATCH));
                sequence += referenceSequence.substring(indexRef, indexRef+alignmentDifference.getPos()-index);
                indexRef += alignmentDifference.getPos()-index;
                index    += alignmentDifference.getPos()-index;
            } else if(index > alignmentDifference.getPos()) {
                System.out.println("[ERROR] BAD DIFFERENCES ");
            }
            System.out.println("Sequence2: " + sequence + " + " + alignmentDifference.getLength()+alignmentDifference.getOp() + " indexRef="+indexRef);


            switch (alignmentDifference.getOp()){
                case Alignment.AlignmentDifference.INSERTION:
              //      cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.INSERTION));
                    sequence += alignmentDifference.getSeq();
                    index += alignmentDifference.getLength();
                    break;

                case Alignment.AlignmentDifference.DELETION:
              //      cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.DELETION));
                    //sequence += referenceSequence.substring(indexRef, indexRef+alignmentDifference.getLength());
                    indexRef += alignmentDifference.getLength();
                    break;

                case Alignment.AlignmentDifference.MISMATCH:
              //      cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.MATCH_OR_MISMATCH));
                    sequence += referenceSequence.substring(indexRef, indexRef+alignmentDifference.getLength());

                    indexRef += alignmentDifference.getLength();
                    index += alignmentDifference.getLength();
                    break;

                case Alignment.AlignmentDifference.SOFT_CLIPPING:
              //      cigar.add(new CigarElement(alignmentDifference.getLength(), CigarOperator.SOFT_CLIP));
                    sequence += alignmentDifference.getSeq();

                    indexRef += alignmentDifference.getLength();
                    index += alignmentDifference.getLength();
                    break;

                case Alignment.AlignmentDifference.HARD_CLIPPING:
              //      cigarOperator = CigarOperator.HARD_CLIP;
                    break;

                case Alignment.AlignmentDifference.SKIPPED_REGION:
              //      cigarOperator = CigarOperator.SKIPPED_REGION;
                    break;
                case Alignment.AlignmentDifference.PADDING:
              //      cigarOperator = CigarOperator.PADDING;
                    break;
            }
//            cigar.add(new CigarElement(alignmentDifference.getLength(), cigarOperator));
//            if(alignmentDifference.isAllSequenceStored()){
//                sequence += alignmentDifference.getSeq();
//            } else {
//
//            }

        }

        if(index < sequenceSize){
          //  cigar.add(new CigarElement(sequenceSize-index, CigarOperator.MATCH_OR_MISMATCH));
            System.out.println(indexRef + " " + (indexRef+sequenceSize-index));
            sequence += referenceSequence.substring(indexRef, indexRef+sequenceSize-index);
        } else if(index > sequenceSize) {
            System.out.println("[ERROR] TOO MUCH DIFFERENCES ");
        }
        System.out.println(sequence);

        return sequence;
    }

    public static String getSequence(Region region, QueryOptions params) throws IOException {
        if(params == null){
            params = new QueryOptions();
        }
        String cellbaseHost = params.getString("cellbasehost", "http://ws.bioinfo.cipf.es/cellbase/rest/latest");
        String species = params.getString("species", "hsa");

        String urlString = cellbaseHost + "/" + species + "/genomic/region/" + region.toString() + "/sequence?of=json";
        //System.out.println(urlString);

        URL url = new URL(urlString);
        InputStream is = url.openConnection().getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonParser jp = factory.createParser(br);
        JsonNode o = mapper.readTree(jp);

        String sequence = o.get(0).get("sequence").asText();
        br.close();
        return sequence;
    }



}
