package org.opencb.commons.bioformats.alignment.io;

import net.sf.samtools.SAMRecord;
import org.opencb.commons.bioformats.alignment.AlignmentCoverage;
import org.opencb.commons.bioformats.alignment.io.readers.AlignmentDataReader;
import org.opencb.commons.bioformats.alignment.io.writers.AlignmentDataWriter;
import org.opencb.commons.bioformats.alignment.io.writers.coverage.AlignmentCoverageDataWriter;
import org.opencb.commons.bioformats.alignment.sam.stats.CoverageCalculator;
import org.opencb.commons.bioformats.commons.DataWriter;
import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.variant.vcf4.effect.EffectCalculator;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.effect.VariantEffectDataWriter;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/4/13
 * Time: 7:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentCoverageRunner {

    protected org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    protected AlignmentDataReader reader;
    protected DataWriter writer;
    protected AlignmentCoverageRunner prev;
    protected CoverageCalculator coverageCalculator = new CoverageCalculator();
    protected int batchSize = 1000;

    public AlignmentCoverageRunner(AlignmentDataReader reader, DataWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public AlignmentCoverageRunner(AlignmentDataReader reader, AlignmentDataWriter writer, AlignmentCoverageRunner prev) {
        this(reader, writer);
        this.prev = prev;
    }



    public List<SAMRecord> apply(List<SAMRecord> batch) throws IOException{


        if (writer != null) {
            List<AlignmentCoverage> list = coverageCalculator.getCoverage(batch);
            ((AlignmentCoverageDataWriter) writer).writeBatch(list);
        }

        return batch;
    }

    public void pre() throws IOException {
        logger.debug(this.getClass().getSimpleName() + " Empty pre");
    }

    public void post() throws IOException {
        logger.debug(this.getClass().getSimpleName() + " Empty post");
    }

    public void run() throws IOException {
        List<SAMRecord> batch;

        int cont = 0;
        reader.open();
        reader.pre();

        this.writerOpen();
        this.writerPre();

        this.launchPre();

        batch = reader.read(batchSize);
        while (!batch.isEmpty()) {

            System.out.println("Batch: " + cont++);
            batch = this.launch(batch);
            batch.clear();
            batch = reader.read(batchSize);

        }

        this.launchPost();

        reader.post();
        reader.close();

        this.writerPost();
        this.writerClose();

    }

    public List<SAMRecord> launch(List<SAMRecord> batch) throws IOException {

        if (prev != null) {
            batch = prev.launch(batch);
        }

        batch = this.apply(batch);
        return batch;
    }

    public void launchPre() throws IOException {
        if (prev != null) {
            prev.launchPre();
        }
        this.pre();
    }

    public void launchPost() throws IOException {
        if (prev != null) {
            prev.launchPost();
        }
        this.post();
    }

    public void writerPre() {
        if (prev != null) {
            prev.writerPre();
        }
        if (writer != null)
            writer.pre();
    }

    public void writerOpen() {
        if (prev != null) {
            prev.writerOpen();
        }
        if (writer != null)
            writer.open();
    }

    public void writerPost() {
        if (prev != null) {
            prev.writerPost();
        }
        if (writer != null)
            writer.post();
    }

    public void writerClose() {
        if (prev != null) {
            prev.writerClose();
        }
        if (writer != null)
            writer.close();
    }


}
