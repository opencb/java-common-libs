package org.opencb.commons.bioformats.commons.core.connectors;

import com.google.common.base.Predicates;
import org.opencb.commons.bioformats.commons.core.connectors.ped.readers.PedDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.ped.readers.PedFileDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.ped.writers.PedDataWriter;
import org.opencb.commons.bioformats.commons.core.connectors.variant.readers.VcfDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.readers.VcfFileDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfDataWriter;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfFileDataWriter;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfSqliteDataWriter;
import org.opencb.commons.bioformats.commons.core.feature.Pedigree;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfFilter;
import org.opencb.commons.bioformats.commons.core.vcffilter.VcfRecordFilters;
import org.opencb.commons.bioformats.commons.core.vcfstats.*;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/2/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantRunner {

    private List<VcfFilter> filters;
    private int numThreads;
    private VcfDataReader vcfReader;
    private VcfDataWriter vcfWriter;
    private PedDataReader pedReader;
    private PedDataWriter pedWriter;
    private boolean effect;
    private boolean stats;
    private boolean index;


    public VariantRunner() {
        this.filters = null;
        this.numThreads = 1;
        this.stats = true;
        this.index = true;
        this.effect = false;
    }

    public VariantRunner(String vcfFilePath, String sqliteFileName, String pedFilePath) {
        this();
        vcfReader = new VcfFileDataReader(vcfFilePath);
        vcfWriter = new VcfSqliteDataWriter(sqliteFileName);

        if (pedFilePath != null) {
            pedReader = new PedFileDataReader(pedFilePath);
        }

    }

    public VariantRunner stats() {
        this.stats = true;
        return this;
    }

    public VariantRunner effect() {
        this.effect = true;
        return this;
    }

    public VariantRunner index() {
        this.index = true;
        return this;
    }

    public VariantRunner reader(VcfDataReader reader) {
        this.vcfReader = reader;
        return this;
    }

    public VariantRunner writer(VcfDataWriter writer) {
        this.vcfWriter = writer;
        return this;
    }


    public VariantRunner(VcfDataReader vcfReader, VcfDataWriter vcfWriter) {
        this();
        this.vcfReader = vcfReader;
        this.vcfWriter = vcfWriter;
    }

    public VariantRunner filter(List<VcfFilter> filterList) {
        this.filters = filterList;
        return this;
    }


    public VariantRunner parallel(int numThreads) {
        this.numThreads = numThreads;
        return this;
    }

    public void run() throws IOException {
        int batchSize = 10000;

        Pedigree ped;

        VcfFilter andFilter;

        List<VcfRecord> batch;
        List<VcfRecordStat> statsList;

        pedReader.open();
        ped = pedReader.read();
        pedReader.close();


        vcfReader.open();
        vcfWriter.open();

        vcfReader.pre();
        vcfWriter.pre();

        VcfGlobalStat globalStats = new VcfGlobalStat();
        VcfSampleStat vcfSampleStat = new VcfSampleStat(vcfReader.getSampleNames());

        VcfSampleGroupStats vcfSampleGroupStatsPhen = new VcfSampleGroupStats();
        VcfSampleGroupStats vcfSampleGroupStatsFam = new VcfSampleGroupStats();

        VcfVariantGroupStat groupStatsBatchPhen;
        VcfVariantGroupStat groupStatsBatchFam;

        batch = vcfReader.read(batchSize);

        while (!batch.isEmpty()) {

            if (filters != null) {
                batch = VcfRecordFilters.filter(batch, filters);
            }

            if (stats) {
                statsList = CalculateStats.variantStats(batch, vcfReader.getSampleNames(), ped, globalStats);

                CalculateStats.sampleStats(batch, vcfReader.getSampleNames(), ped, vcfSampleStat);

                groupStatsBatchPhen = CalculateStats.groupStats(batch, ped, "phenotype");
                groupStatsBatchFam = CalculateStats.groupStats(batch, ped, "family");

                CalculateStats.sampleGroupStats(batch, ped, "phenotype", vcfSampleGroupStatsPhen);
                CalculateStats.sampleGroupStats(batch, ped, "family", vcfSampleGroupStatsFam);


                vcfWriter.writeVariantStats(statsList);
                vcfWriter.writeVariantGroupStats(groupStatsBatchPhen);
                vcfWriter.writeVariantGroupStats(groupStatsBatchFam);
            }

            if(index){
                vcfWriter.writeVariantIndex(batch);
            }

            if(effect){
                ;
            }

            batch = vcfReader.read(batchSize);
        }

        vcfWriter.writeGlobalStats(globalStats);
        vcfWriter.writeSampleStats(vcfSampleStat);

        vcfWriter.writeSampleGroupStats(vcfSampleGroupStatsFam);
        vcfWriter.writeSampleGroupStats(vcfSampleGroupStatsPhen);

        vcfReader.post();
        vcfWriter.post();

        vcfReader.close();
        vcfWriter.close();
    }
}
