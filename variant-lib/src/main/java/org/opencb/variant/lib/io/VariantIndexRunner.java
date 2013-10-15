package org.opencb.variant.lib.io;

import org.opencb.variant.lib.core.formats.*;
import org.opencb.variant.lib.filters.customfilters.VcfFilter;
import org.opencb.variant.lib.io.ped.readers.PedDataReader;
import org.opencb.variant.lib.io.ped.writers.PedDataWriter;
import org.opencb.variant.lib.io.variant.readers.VariantDataReader;
import org.opencb.variant.lib.io.variant.writers.index.VariantIndexDataWriter;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/2/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantIndexRunner {

    private List<VcfFilter> filters;
    private int numThreads;
    private VariantDataReader vcfReader;
    private VariantIndexDataWriter vcfWriter;
    private PedDataReader pedReader;
    private PedDataWriter pedWriter;
    private boolean effect;
    private boolean stats;
    private boolean index;


    public VariantIndexRunner() {
        this.filters = null;
        this.numThreads = 1;
        this.stats = true;
        this.index = true;
        this.effect = true;
    }


    public VariantIndexRunner reader(VariantDataReader reader) {
        this.vcfReader = reader;
        return this;
    }

    public VariantIndexRunner writer(VariantIndexDataWriter writer) {
        this.vcfWriter = writer;
        return this;
    }


    public VariantIndexRunner(VariantDataReader vcfReader, VariantIndexDataWriter vcfWriter) {
        this();
        this.vcfReader = vcfReader;
        this.vcfWriter = vcfWriter;
    }

    public VariantIndexRunner filter(List<VcfFilter> filterList) {
        this.filters = filterList;
        return this;
    }


    public VariantIndexRunner parallel(int numThreads) {
        this.numThreads = numThreads;
        return this;
    }

    public void run() throws IOException {
        int batchSize = 1000;

        List<VcfRecord> batch;

        vcfReader.open();
        vcfWriter.open();

        vcfReader.pre();
        vcfWriter.pre();

        batch = vcfReader.read(batchSize);

        while (!batch.isEmpty()) {
            vcfWriter.writeVariantIndex(batch);
            batch.clear();
            batch = vcfReader.read(batchSize);
        }


        vcfReader.post();
        vcfWriter.post();

        vcfReader.close();
        vcfWriter.close();
    }
}
