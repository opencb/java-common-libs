package org.opencb.commons.bioformats.commons.core.vcfstats;

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
public class VcfSampleGroupStats{

    private String group;
    private Map<String, VcfSampleStat> sampleStats;

    public VcfSampleGroupStats() {
        sampleStats = new LinkedHashMap<>();

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
