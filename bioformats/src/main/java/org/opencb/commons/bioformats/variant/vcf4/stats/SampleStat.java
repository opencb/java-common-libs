package org.opencb.commons.bioformats.variant.vcf4.stats;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/29/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class SampleStat {

    private Map<String, SingleSampleStat> samplesStats;


    public SampleStat(List<String> sampleNames) {
        samplesStats = new LinkedHashMap<>(sampleNames.size());
        SingleSampleStat s;

        for (String name : sampleNames) {
            s = new SingleSampleStat(name);
            samplesStats.put(name, s);
        }
    }

    public SampleStat(List<String> sampleNames, List<SampleStat> sampleStats) {
        this(sampleNames);
        String sampleName;
        SingleSampleStat ss, ssAux;
        Map<String, SingleSampleStat> map;
        for (SampleStat sampleStat : sampleStats) {
            map = sampleStat.getSamplesStats();
            for (Map.Entry<String, SingleSampleStat> entry : map.entrySet()) {
                sampleName = entry.getKey();
                ss = entry.getValue();
                ssAux = this.getSamplesStats().get(sampleName);
                ssAux.incrementMendelianErrors(ss.getMendelianErrors());
                ssAux.incrementMissingGenotypes(ss.getMissingGenotypes());
                ssAux.incrementHomozygotesNumber(ss.getHomozygotesNumber());
            }
        }
    }

    public Map<String, SingleSampleStat> getSamplesStats() {
        return samplesStats;
    }

    public void incrementMendelianErrors(String sampleName) {
        SingleSampleStat s = samplesStats.get(sampleName);
        s.incrementMendelianErrors();
    }

    public void incrementMissingGenotypes(String sampleName) {
        SingleSampleStat s = samplesStats.get(sampleName);
        s.incrementMissingGenotypes();
    }

    public void incrementHomozygotesNumber(String sampleName) {
        SingleSampleStat s = samplesStats.get(sampleName);
        s.incrementHomozygotesNumber();
    }

    @Override
    public String toString() {
        SingleSampleStat s;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s%-10s%-10s%-10s\n", "Sample", "MissGt", "Mendel Err", "Homoz Count"));
        for (Map.Entry<String, SingleSampleStat> entry : samplesStats.entrySet()) {
            s = entry.getValue();
            sb.append(String.format("%-10s%-10d%-10d%10d\n", s.getId(), s.getMissingGenotypes(), s.getMendelianErrors(), s.getHomozygotesNumber()));

        }
        return sb.toString();
    }
}
