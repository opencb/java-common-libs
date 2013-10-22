package org.opencb.commons.bioformats.variant.vcf4.stats;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/28/13
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfVariantGroupStat {

    private String group;
    private Map<String, List<VcfVariantStat>> variantStats;
    private Object samples;

    public VcfVariantGroupStat(String group, Set<String> groupValues) {
        this.group = group;
        variantStats = new LinkedHashMap<>(groupValues.size());
        List<VcfVariantStat> list;
        for (String groupVal : groupValues) {
            list = new ArrayList<>(1000);
            variantStats.put(groupVal, list);
        }
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Map<String, List<VcfVariantStat>> getVariantStats() {
        return variantStats;
    }

    public void setVariantStats(Map<String, List<VcfVariantStat>> variantStats) {
        this.variantStats = variantStats;
    }

    public Object getSamples() {
        return samples;
    }

    public void setSamples(Object samples) {
        this.samples = samples;
    }
}
