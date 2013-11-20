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
public class VariantStats {

    private List<VariantStat> variantStats;
    private List<GlobalStat> globalStats;
    private List<SampleStat> sampleStats;
    private Map<String, VariantGroupStat> groupStats;
    private Map<String, List<SampleGroupStat>> sampleGroupStats;
    private List<String> sampleNames;

    public VariantStats() {
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

    public List<VariantStat> getVariantStats() {
        return variantStats;
    }

    public void setVariantStats(List<VariantStat> variantStats) {
        this.variantStats = variantStats;
    }

    public void addGlobalStats(GlobalStat gs) {
        globalStats.add(gs);
    }

    public void addSampleStats(SampleStat ss) {
        sampleStats.add(ss);
    }

    public List<GlobalStat> getGlobalStats() {
        return globalStats;
    }

    public List<SampleStat> getSampleStats() {
        return sampleStats;
    }

    public void addGroupStats(String group) {
        if (!groupStats.containsKey(group)) {
            groupStats.put(group, null);
        }
    }

    public void addGroupStats(String group, VariantGroupStat gs) {
        groupStats.put(group, gs);

    }

    public VariantGroupStat getGroupStats(String group) {
        return groupStats.get(group);
    }

    public void addSampleGroupStats(String group, SampleGroupStat sgs) {

        List<SampleGroupStat> list;
        System.out.println("group = " + group);
        if (!sampleGroupStats.containsKey(group)) {
            System.out.println("group = " + group);
            list = new ArrayList<>();
            sampleGroupStats.put(group, list);
        } else {
            list = sampleGroupStats.get(group);
        }
        list.add(sgs);
    }

    public List<SampleGroupStat> getSampleGroupStats(String group) {
        return sampleGroupStats.get(group);
    }

    public GlobalStat getFinalGlobalStats() {

        GlobalStat gsFinal = new GlobalStat();

        for (GlobalStat gs : this.globalStats) {
            gsFinal.updateStats(gs.getVariantsCount(), gs.getSamplesCount(),
                    gs.getSnpsCount(), gs.getIndelsCount(), gs.getPassCount(),
                    gs.getTransitionsCount(), gs.getTransversionsCount(), gs.getBiallelicsCount(), gs.getMultiallelicsCount(), gs.getAccumQuality());
        }
        return gsFinal;
    }

    public SampleStat getFinalSampleStats() {

        SampleStat ssFinal = new SampleStat(this.getSampleNames());

        String sampleName;
        SingleSampleStat ss, ssAux;
        Map<String, SingleSampleStat> map;

        for (SampleStat sampleStat : this.sampleStats) {
            map = sampleStat.getSamplesStats();
            for (Map.Entry<String, SingleSampleStat> entry : map.entrySet()) {
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

    public SampleGroupStat getFinalSampleGroupStat(String group) {
        SampleGroupStat sgsFinal = new SampleGroupStat();

        SampleStat sampleStatAux;
        SingleSampleStat singleSampleStat;

        System.out.println(this.sampleGroupStats);
        for (SampleGroupStat sgs : this.sampleGroupStats.get(group)) {
            sgsFinal.setGroup(sgs.getGroup());
            for (Map.Entry<String, SampleStat> ss : sgs.getSampleStats().entrySet()) {
                if (!sgsFinal.getSampleStats().containsKey(ss.getKey())) {
                    sgsFinal.getSampleStats().put(ss.getKey(), ss.getValue());
                } else {
                    sampleStatAux = sgsFinal.getSampleStats().get(ss.getKey());
                    for (Map.Entry<String, SingleSampleStat> entry : sampleStatAux.getSamplesStats().entrySet()) {
                        singleSampleStat = entry.getValue();
                        singleSampleStat.incrementHomozygotesNumber(ss.getValue().getSamplesStats().get(entry.getKey()).getHomozygotesNumber());
                        singleSampleStat.incrementMendelianErrors(ss.getValue().getSamplesStats().get(entry.getKey()).getMendelianErrors());
                        singleSampleStat.incrementMissingGenotypes(ss.getValue().getSamplesStats().get(entry.getKey()).getMissingGenotypes());
                    }
                }
            }
        }

        return sgsFinal;
    }
}
