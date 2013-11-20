package org.opencb.commons.bioformats.variant.vcf4.stats;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/29/13
 * Time: 8:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleGroupStat {

    private String group;
    private Map<String, SampleStat> sampleStats;

    public SampleGroupStat() {
        sampleStats = new LinkedHashMap<>();

    }

    public SampleGroupStat(List<SampleGroupStat> sampleGroup) {
        this();


        SampleStat sampleStatAux;
        SingleSampleStat singleSampleStat;


        for (SampleGroupStat sgs : sampleGroup) {
            this.setGroup(sgs.getGroup());
            for (Map.Entry<String, SampleStat> ss : sgs.getSampleStats().entrySet()) {
                if (!this.sampleStats.containsKey(ss.getKey())) {
                    this.sampleStats.put(ss.getKey(), ss.getValue());
                } else {
                    sampleStatAux = this.sampleStats.get(ss.getKey());
                    for (Map.Entry<String, SingleSampleStat> entry : sampleStatAux.getSamplesStats().entrySet()) {
                        singleSampleStat = entry.getValue();
                        singleSampleStat.incrementHomozygotesNumber(ss.getValue().getSamplesStats().get(entry.getKey()).getHomozygotesNumber());
                        singleSampleStat.incrementMendelianErrors(ss.getValue().getSamplesStats().get(entry.getKey()).getMendelianErrors());
                        singleSampleStat.incrementMissingGenotypes(ss.getValue().getSamplesStats().get(entry.getKey()).getMissingGenotypes());
                    }


                }


            }
        }
    }

    public Map<String, SampleStat> getSampleStats() {
        return sampleStats;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return sampleStats.toString();
    }
}
