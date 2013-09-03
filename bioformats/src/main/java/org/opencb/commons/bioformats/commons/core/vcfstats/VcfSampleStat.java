package org.opencb.commons.bioformats.commons.core.vcfstats;

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
public class VcfSampleStat {

    private Map<String, SampleStat> samplesStats;


    public VcfSampleStat(List<String> sampleNames) {
        samplesStats = new LinkedHashMap<>(sampleNames.size());
        SampleStat s;

        for (String name : sampleNames) {
            s = new SampleStat(name);
            samplesStats.put(name, s);
        }
    }


    public Map<String, SampleStat> getSamplesStats() {
        return samplesStats;
    }

    public void incrementMendelianErrors(String sampleName) {
        SampleStat s = samplesStats.get(sampleName);
        s.incrementMendelianErrors();
    }

    public void incrementMissingGenotypes(String sampleName) {
        SampleStat s = samplesStats.get(sampleName);
        s.incrementMissingGenotypes();
    }

    public void incrementHomozygotesNumber(String sampleName) {
        SampleStat s = samplesStats.get(sampleName);
        s.incrementHomozygotesNumber();
    }

    @Override
    public String toString() {
        SampleStat s;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s%-10s%-10s%-10s\n", "Sample", "MissGt", "Mendel Err", "Homoz Count"));
        for(Map.Entry<String, SampleStat> entry: samplesStats.entrySet()){
            s = entry.getValue();
            sb.append(String.format("%-10s%-10d%-10d%10d\n",s.getId(), s.getMissingGenotypes(), s.getMendelianErrors(), s.getHomozygotesNumber()));

        }
        return sb.toString();
    }
}
