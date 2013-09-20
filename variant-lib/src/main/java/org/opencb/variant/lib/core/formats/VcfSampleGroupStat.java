package org.opencb.variant.lib.core.formats;

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
public class VcfSampleGroupStat {

    private String group;
    private Map<String, VcfSampleStat> sampleStats;

    public VcfSampleGroupStat() {
        sampleStats = new LinkedHashMap<>();

    }

    public VcfSampleGroupStat(List<VcfSampleGroupStat> sampleGroup) {
        this();


        VcfSampleStat sampleStatAux;
        SampleStat sampleStat;


        for (VcfSampleGroupStat sgs : sampleGroup) {
            this.setGroup(sgs.getGroup());
            for (Map.Entry<String, VcfSampleStat> ss : sgs.getSampleStats().entrySet()) {
                if (!this.sampleStats.containsKey(ss.getKey())) {
                    this.sampleStats.put(ss.getKey(), ss.getValue());
                } else {
                    sampleStatAux = this.sampleStats.get(ss.getKey());
                    for (Map.Entry<String, SampleStat> entry : sampleStatAux.getSamplesStats().entrySet()) {
                        sampleStat = entry.getValue();
                        sampleStat.incrementHomozygotesNumber(ss.getValue().getSamplesStats().get(entry.getKey()).getHomozygotesNumber());
                        sampleStat.incrementMendelianErrors(ss.getValue().getSamplesStats().get(entry.getKey()).getMendelianErrors());
                        sampleStat.incrementMissingGenotypes(ss.getValue().getSamplesStats().get(entry.getKey()).getMissingGenotypes());
                    }


                }


            }
        }
    }

    public Map<String, VcfSampleStat> getSampleStats() {
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
