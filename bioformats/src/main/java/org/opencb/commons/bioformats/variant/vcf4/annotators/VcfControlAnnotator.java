package org.opencb.commons.bioformats.variant.vcf4.annotators;

import java.io.IOException;
import java.util.*;
import net.sf.samtools.util.StringUtil;
import org.broad.tribble.readers.TabixReader;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.variant.vcf4.stats.CalculateStats;
import org.opencb.commons.bioformats.variant.vcf4.stats.VcfVariantStat;

/**
 * Created with IntelliJ IDEA. User: aleman Date: 9/14/13 Time: 1:11 PM To
 * change this template use File | Settings | File Templates.
 */
public class VcfControlAnnotator implements VcfAnnotator {

    // private TabixReader tabix;
    private String tabixFile;
    private List<String> samples;
    private Map<String, Integer> samplesMap;
    private Map<String, String> controlList;
    private Map<Long, Map<String, TabixReader>> multipleControlsTabix;
    private Map<Long, TabixReader> tabix;
    private String prefix;
    private boolean single;

    public VcfControlAnnotator(String infoPrefix, String control){

        this.prefix = infoPrefix;
        this.tabixFile = control;
        this.tabix = new LinkedHashMap<>();
        TabixReader tabixReader;
        try {
            tabixReader = new TabixReader(this.tabixFile);
            String line;
            while ((line = tabixReader.readLine()) != null && !line.startsWith("#CHROM")) {
            }

            String[] fields = line.split("\t");

            samples = new ArrayList<>(fields.length - 9);
            samplesMap = new LinkedHashMap<>(fields.length - 9);
            for (int i = 9, j = 0; i < fields.length; i++, j++) {
                samples.add(fields[i]);
                samplesMap.put(fields[i], j);

            }

            tabixReader.close();
            single = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public VcfControlAnnotator(String infoPrefix, Map<String, String> controlList) {

        this.prefix = infoPrefix;
        this.controlList = controlList;
        multipleControlsTabix = new LinkedHashMap<>(5);

        boolean b = true;
        TabixReader t;

        try {
            for (Map.Entry<String, String> entry : controlList.entrySet()) {

                t = new TabixReader(entry.getValue());

                if (b) {
                    b = false;

                    String line;
                    while ((line = t.readLine()) != null && !line.startsWith("#CHROM")) {
                    }

                    String[] fields = line.split("\t");

                    samples = new ArrayList<>(fields.length - 9);
                    samplesMap = new LinkedHashMap<>(fields.length - 9);
                    for (int i = 9, j = 0; i < fields.length; i++, j++) {
                        samples.add(fields[i]);
                        samplesMap.put(fields[i], j);

                    }
                }
                t.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        single = false;

    }

    @Override
    public void annot(List<VcfRecord> batch) {

        if (single) {
            singleAnnot(batch);
        } else {
            multipleAnnot(batch);
        }
    }

    private void singleAnnot(List<VcfRecord> batch) {
        VcfRecord tabixRecord;

        long pid = Thread.currentThread().getId();
        TabixReader currentTabix = null;


        if (tabix.containsKey(pid)) {
            currentTabix = tabix.get(pid);
        } else {
            try {
                currentTabix = new TabixReader(this.tabixFile);
                tabix.put(pid, currentTabix);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<VcfRecord> controlBatch = new ArrayList<>(batch.size());
        List<VcfVariantStat> statsBatch;
        Map<VcfRecord, Integer> map = new LinkedHashMap<>(batch.size());

        int cont = 0;
        for (VcfRecord record : batch) {

            if (currentTabix != null) {


                try {

                    TabixReader.Iterator it = currentTabix.query(record.getChromosome() + ":" + record.getPosition() + "-" + record.getPosition());

                    String line;
                    while (it != null && (line = it.next()) != null) {

                        String[] fields = line.split("\t");
                        tabixRecord = new VcfRecord(fields, samples);


                        if (tabixRecord.getReference().equals(record.getReference()) && tabixRecord.getAlternate().equals(record.getAlternate())) {
                            controlBatch.add(tabixRecord);
                            map.put(record, cont++);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) { // If the Chr does not exist in Controls... TabixReader throws ArrayIndexOut...
                    continue;
                }
            }
        }

        statsBatch = CalculateStats.variantStats(controlBatch, this.samples, null);

        VcfVariantStat statRecord;

        for (VcfRecord record : batch) {

            if (map.containsKey(record)) {
                statRecord = statsBatch.get(map.get(record));
                record.addInfoField(this.prefix + "_gt=" + StringUtil.join(",", statRecord.getGenotypes()));
                record.addInfoField(this.prefix + "_maf=" + String.format("%.4f", statRecord.getMaf()));
                record.addInfoField(this.prefix + "_amaf=" + statRecord.getMafAllele());
            }
        }
    }

    private void multipleAnnot(List<VcfRecord> batch) {

        VcfRecord tabixRecord;
        TabixReader currentTabix = null;

        long pid = Thread.currentThread().getId();
        Map<String, TabixReader> tabixMap;
        List<VcfRecord> controlBatch = new ArrayList<>(batch.size());
        List<VcfVariantStat> statsBatch;
        Map<VcfRecord, Integer> map = new LinkedHashMap<>(batch.size());


        if (multipleControlsTabix.containsKey(pid)) {
            tabixMap = multipleControlsTabix.get(pid);
        } else {
            tabixMap = new LinkedHashMap<>();
            multipleControlsTabix.put(pid, tabixMap);
        }


        int cont = 0;
        for (VcfRecord record : batch) {
            currentTabix = null;

            if (tabixMap.containsKey(record.getChromosome())) {
                currentTabix = tabixMap.get(record.getChromosome());
            } else {
                if (controlList.containsKey(record.getChromosome())) {
                    try {
                        currentTabix = new TabixReader(controlList.get(record.getChromosome()));
                        tabixMap.put(record.getChromosome(), currentTabix);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (currentTabix != null) {
                try {

                    TabixReader.Iterator it = currentTabix.query(record.getChromosome() + ":" + record.getPosition() + "-" + record.getPosition());

                    String line;
                    while (it != null && (line = it.next()) != null) {

                        String[] fields = line.split("\t");
                        tabixRecord = new VcfRecord(fields, samples);

                        if (tabixRecord.getReference().equals(record.getReference()) && tabixRecord.getAlternate().equals(record.getAlternate())) {

                            controlBatch.add(tabixRecord);
                            map.put(record, cont++);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) { // If the Chr does not exist in Controls... TabixReader throws ArrayIndexOut...
                    continue;
                }
            }


        }

        statsBatch = CalculateStats.variantStats(controlBatch, this.samples, null);

        VcfVariantStat statRecord;

        for (VcfRecord record : batch) {

            if (map.containsKey(record)) {
                statRecord = statsBatch.get(map.get(record));
                record.addInfoField(this.prefix + "_gt=" + StringUtil.join(",", statRecord.getGenotypes()));
                record.addInfoField(this.prefix + "_maf=" + String.format("%.4f", statRecord.getMaf()));
                record.addInfoField(this.prefix + "_amaf=" + statRecord.getMafAllele());
            }
        }

    }

    @Override
    public void annot(VcfRecord elem) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}