package org.opencb.commons.bioformats.alignment.sam.io;

import net.sf.samtools.*;
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
 * This class needs the SAMFileHeader to write. If it is not set, it will fail.
 */
public class AlignmentSamDataWriter implements AlignmentDataWriter<Alignment, SAMFileHeader> {

    protected SAMFileWriterImpl writer;
    AlignmentSamDataReader reader;
    protected String filename;
    private SAMFileHeader samFileHeader;
    private int maxSequenceSize = defaultMaxSequenceSize;   // max length of the reference sequence.
    private static final int defaultMaxSequenceSize = 100000;
    private String referenceSequence;
    private long referenceSequenceStart = -1;
    private boolean headerWritten = false;
    private boolean validSequence = false;


    public AlignmentSamDataWriter(String filename, SAMFileHeader header) {
        this.filename = filename;
        this.samFileHeader = header;
        this.reader = null;
    }
    public AlignmentSamDataWriter(String filename, AlignmentSamDataReader reader) {
        this.samFileHeader = null;
        this.filename = filename;
        this.reader = reader;
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
        if(samFileHeader != null){
            writeHeader(samFileHeader);
            headerWritten = true;
        }
        return true;
    }

    @Override
    public boolean post() {
        return true;
    }

    @Override
    public boolean write(Alignment element) {
        if (!headerWritten) {  // if samFileHeader wasn't available at pre()
            if (samFileHeader == null) {
                samFileHeader = reader.getHeader();
            }
            writeHeader(samFileHeader);
            headerWritten = true;
        }

        System.out.println("write");
        System.out.println("element.getUnclippedStart()" + element.getUnclippedStart());
        System.out.println("element.getUnclippedEnd() = " + element.getUnclippedEnd());
        System.out.println("referenceSequenceStart = " + referenceSequenceStart);
        System.out.println("maxSequenceSize = " + maxSequenceSize);
        if (!validSequence || element.getUnclippedStart() < referenceSequenceStart || element.getUnclippedEnd() > referenceSequenceStart + maxSequenceSize) {
            System.out.println("pidiendo la referencia...");
            validSequence = true;
            referenceSequenceStart = element.getUnclippedStart();
            try {
                referenceSequence = AlignmentHelper.getSequence(
                        new Region(element.getChromosome(), element.getUnclippedStart(), element.getUnclippedStart() + maxSequenceSize)
                        , null);
            } catch (IOException e) {
                System.out.println("could not get reference sequence");
            }
        }
        // assert refseq correct

        SAMRecord SamElement = element.createSAMRecord(samFileHeader, referenceSequence, referenceSequenceStart);
        writer.addAlignment(SamElement);
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
        setSamFileHeader(head);
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
