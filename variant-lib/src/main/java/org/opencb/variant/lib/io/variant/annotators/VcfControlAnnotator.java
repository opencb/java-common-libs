package org.opencb.variant.lib.io.variant.annotators;

import net.sf.samtools.util.StringUtil;
import org.broad.tribble.readers.TabixReader;
import org.opencb.variant.lib.core.formats.VcfRecord;
import org.opencb.variant.lib.core.formats.VcfVariantStat;
import org.opencb.variant.lib.stats.CalculateStats;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/14/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfControlAnnotator implements VcfAnnotator {

    private TabixReader tabix;
    private List<String> samples;
    private HashMap<String, Integer> samplesMap;
    private HashMap<String, TabixReader> controlList;
    private String prefix;


    public VcfControlAnnotator(String infoPrefix, String control) throws IOException {

        this.prefix = infoPrefix;
        this.tabix = new TabixReader(control);

        String line;
        while ((line = tabix.readLine()) != null && !line.startsWith("#CHROM")) {
        }

        String[] fields = line.split("\t");

        samples = new ArrayList<>(fields.length - 9);
        samplesMap = new LinkedHashMap<>(fields.length - 9);
        for (int i = 9, j = 0; i < fields.length; i++, j++) {
            samples.add(fields[i]);
            samplesMap.put(fields[i], j);

        }


    }

    public VcfControlAnnotator(String infoPrefix, HashMap<String, String> controlList) {

        this.prefix = infoPrefix;
        this.controlList = new LinkedHashMap<>(controlList.size());

        boolean b = true;
        TabixReader t;

        try {
            for (Map.Entry<String, String> entry : controlList.entrySet()) {

                t = new TabixReader(entry.getValue());
                this.controlList.put(entry.getKey(), t);

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

            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println(this.controlList);
    }

    @Override
    public void annot(List<VcfRecord> batch) {

        VcfRecord tabixRecord;

        TabixReader currentTabix;

        List<VcfRecord> controlBatch = new ArrayList<>(batch.size());
        List<VcfVariantStat> statsBatch = null;

        for (VcfRecord record : batch) {

            System.out.println("record.getChromosome() = " + record.getChromosome());
            if(this.tabix == null && controlList != null){
                currentTabix = controlList.get(record.getChromosome());
            }else{
                currentTabix = this.tabix;

            }

            TabixReader.Iterator it = currentTabix.query(record.getChromosome() + ":" + record.getPosition() + "-" + record.getPosition());
            String line;
            try {
                while (it != null && (line = it.next()) != null) {

                    String[] fields = line.split("\t");
                    tabixRecord = new VcfRecord(fields);
                    tabixRecord.setSampleIndex(this.samplesMap);
                    controlBatch.add(tabixRecord);
                }


            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        statsBatch = CalculateStats.variantStats(controlBatch, this.samples, null);

        VcfRecord record;
        VcfVariantStat statRecord;


        for (int i = 0; i < batch.size(); i++) {
            record = batch.get(i);
            statRecord = statsBatch.get(i);
            record.addInfoField(this.prefix + "=" + StringUtil.join(",", statRecord.getGenotypes()));
        }


        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void annot(VcfRecord elem) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
