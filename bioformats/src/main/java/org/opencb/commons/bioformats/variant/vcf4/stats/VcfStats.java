package org.opencb.commons.bioformats.variant.vcf4.stats;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 11/11/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfStats {

    private List<VcfVariantStat> variantStats;
    private List<VcfGlobalStat> globalStats;
    private List<VcfSampleStat> sampleStats;
    private Map<String, VcfVariantGroupStat> groupStats;
    private Map<String, List<VcfSampleGroupStat>> sampleGroupStats;
    private List<String> sampleNames;

    public VcfStats() {
        globalStats = new ArrayList<>();
        sampleStats = new ArrayList<>();
        groupStats = new LinkedHashMap<>();
        sampleGroupStats = new LinkedHashMap<>();
    }

    public List<String> getSampleNames() {
        return sampleNames;
    }

    public void setSampleNames(List<String> sampleNames) {
        this.sampleNames = sampleNames;
    }

    public List<VcfVariantStat> getVariantStats() {
        return variantStats;
    }

    public void setVariantStats(List<VcfVariantStat> variantStats) {
        this.variantStats = variantStats;
    }

    public void addGlobalStats(VcfGlobalStat gs) {
        globalStats.add(gs);
    }

    public void addSampleStats(VcfSampleStat ss) {
        sampleStats.add(ss);
    }

    public List<VcfGlobalStat> getGlobalStats() {
        return globalStats;
    }

    public List<VcfSampleStat> getSampleStats() {
        return sampleStats;
    }

    public void addGroupStats(String group) {
        if (!groupStats.containsKey(group)) {
            groupStats.put(group, null);
        }
    }

    public void addGroupStats(String group, VcfVariantGroupStat gs) {
        groupStats.put(group, gs);

    }

    public VcfVariantGroupStat getGroupStats(String group) {
        return groupStats.get(group);
    }

    public void addSampleGroupStats(String group, VcfSampleGroupStat sgs) {

        List<VcfSampleGroupStat> list;
        if (!sampleGroupStats.containsKey(group)) {
            list = new ArrayList<>();
            sampleGroupStats.put(group, list);
        } else {
            list = sampleGroupStats.get(group);
        }
        list.add(sgs);
    }

    public List<VcfSampleGroupStat> getSampleGroupStats(String group) {
        return sampleGroupStats.get(group);
    }

    public VcfGlobalStat getFinalGlobalStats() {

        VcfGlobalStat gsFinal = new VcfGlobalStat();

        for (VcfGlobalStat gs : this.globalStats) {
            gsFinal.updateStats(gs.getVariantsCount(), gs.getSamplesCount(),
                    gs.getSnpsCount(), gs.getIndelsCount(), gs.getPassCount(),
                    gs.getTransitionsCount(), gs.getTransversionsCount(), gs.getBiallelicsCount(), gs.getMultiallelicsCount(), gs.getAccumQuality());
        }
        return gsFinal;
    }

    public VcfSampleStat getFinalSampleStats() {

        VcfSampleStat ssFinal = new VcfSampleStat(this.getSampleNames());

        String sampleName;
        SampleStat ss, ssAux;
        Map<String, SampleStat> map;

        for (VcfSampleStat vcfSampleStat : sampleStats) {
            map = vcfSampleStat.getSamplesStats();
            for (Map.Entry<String, SampleStat> entry : map.entrySet()) {
                sampleName = entry.getKey();
                ss = entry.getValue();
                ssAux = ssFinal.getSamplesStats().get(sampleName);
                ssAux.incrementMendelianErrors(ss.getMendelianErrors());
                ssAux.incrementMissingGenotypes(ss.getMissingGenotypes());
                ssAux.incrementHomozygotesNumber(ss.getHomozygotesNumber());
            }
        }

        return ssFinal;

    }

    public VcfSampleGroupStat getFinalSampleGroupStat(String group) {
        VcfSampleGroupStat sgsFinal = new VcfSampleGroupStat();

        VcfSampleStat sampleStatAux;
        SampleStat sampleStat;


        for (VcfSampleGroupStat sgs : this.sampleGroupStats.get(group)) {
            sgsFinal.setGroup(sgs.getGroup());
            for (Map.Entry<String, VcfSampleStat> ss : sgs.getSampleStats().entrySet()) {
                if (!sgsFinal.getSampleStats().containsKey(ss.getKey())) {
                    sgsFinal.getSampleStats().put(ss.getKey(), ss.getValue());
                } else {
                    sampleStatAux = sgsFinal.getSampleStats().get(ss.getKey());
                    for (Map.Entry<String, SampleStat> entry : sampleStatAux.getSamplesStats().entrySet()) {
                        sampleStat = entry.getValue();
                        sampleStat.incrementHomozygotesNumber(ss.getValue().getSamplesStats().get(entry.getKey()).getHomozygotesNumber());
                        sampleStat.incrementMendelianErrors(ss.getValue().getSamplesStats().get(entry.getKey()).getMendelianErrors());
                        sampleStat.incrementMissingGenotypes(ss.getValue().getSamplesStats().get(entry.getKey()).getMissingGenotypes());
                    }
                }
            }
        }

        return sgsFinal;
    }
}
