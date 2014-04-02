package org.opencb.commons.bioformats.alignment.sam.io;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMTextWriter;
import org.opencb.commons.bioformats.alignment.Alignment;
import org.opencb.commons.bioformats.alignment.AlignmentHelper;
import org.opencb.commons.bioformats.alignment.io.writers.AlignmentDataWriter;
import org.opencb.commons.bioformats.feature.Region;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/3/13
 * Time: 5:31 PM
 *
 * This class needs the SAMFileHeader to write. If it is not setted, it will fail.
 */
public class AlignmentSamDataWriter implements AlignmentDataWriter<Alignment, SAMFileHeader> {

    private SAMTextWriter writer;
    private String filename;
    private SAMFileHeader samFileHeader;
    private int maxSequenceSize;
    private static final int defaultMaxSequenceSize = 100000;
    private String referenceSequence;
    private long referenceSequenceStart;

    public AlignmentSamDataWriter(String filename) {
        this.filename = filename;
        maxSequenceSize = defaultMaxSequenceSize;
    }

    @Override
    public boolean open() {
        Path path;
        File file;
        path = Paths.get(this.filename);
        file = path.toFile();

        this.writer = new SAMTextWriter(file);

        return true;
    }

    @Override
    public boolean close() {
        writer.close();
        return true;
    }

    @Override
    public boolean pre() {
        return true;
    }

    @Override
    public boolean post() {
        return true;
    }

    @Override
    public boolean write(Alignment element) {
        if (element.getUnclippedStart() < referenceSequenceStart || element.getUnclippedEnd() > referenceSequenceStart + referenceSequence.length()) {
            try {
                AlignmentHelper.getSequence(new Region(element.getChromosome(), element.getUnclippedStart(), element.getUnclippedStart() + maxSequenceSize), null);
            } catch (IOException e) {
                System.out.println("could not get reference sequence");
            }
        }
        // assert refseq correct
        
        SAMRecord SamElement = element.createSAMRecord(samFileHeader, referenceSequence, referenceSequenceStart);
        writer.writeAlignment(SamElement);
        return true;
    }

    @Override
    public boolean write(List<Alignment> batch) {
        for(Alignment r : batch){
            write(r);
        }
        return true;
    }

    @Override
    public boolean writeHeader(SAMFileHeader head) {
        writer.setSortOrder(head.getSortOrder(), true);
        writer.setHeader(head);
        return true;
    }

    public SAMFileHeader getSamFileHeader() {
        return samFileHeader;
    }

    public void setSamFileHeader(SAMFileHeader samFileHeader) {
        this.samFileHeader = samFileHeader;
    }

    public int getMaxSequenceSize() {
        return maxSequenceSize;
    }

    public void setMaxSequenceSize(int maxSequenceSize) {
        this.maxSequenceSize = maxSequenceSize;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getReferenceSequence() {
        return referenceSequence;
    }

    public void setReferenceSequence(String referenceSequence) {
        this.referenceSequence = referenceSequence;
    }

    public long getReferenceSequenceStart() {
        return referenceSequenceStart;
    }

    public void setReferenceSequenceStart(long referenceSequenceStart) {
        this.referenceSequenceStart = referenceSequenceStart;
    }
}
