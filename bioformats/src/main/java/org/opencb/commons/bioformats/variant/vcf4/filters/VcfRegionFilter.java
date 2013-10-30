package org.opencb.commons.bioformats.variant.vcf4.filters;


import org.opencb.commons.bioformats.feature.Region;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 10:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfRegionFilter extends VcfFilter {

    private List<Region> regionList;

    public VcfRegionFilter(String chromosome, long start, long end) {
        super();
        regionList = new ArrayList<>();
        regionList.add(new Region(chromosome, start, end));
    }

    public VcfRegionFilter(String chromosome, long start, long end, int priority) {
        super(priority);
        regionList = new ArrayList<>();
        regionList.add(new Region(chromosome, start, end));

    }

    public VcfRegionFilter(String regions) {
        super();
        regionList = new ArrayList<>();

        String[] splits = regions.split(",");
        for (String split : splits) {
            regionList.add(new Region(split));
        }
    }

    public VcfRegionFilter(String regions, int priority) {
        super(priority);
        regionList = new ArrayList<>();

        String[] splits = regions.split(",");
        for (String split : splits) {
            regionList.add(new Region(split));
        }
    }

    @Override
    public boolean apply(VcfRecord vcfRecord) {
        for (Region r : regionList) {
            if (r.contains(vcfRecord.getChromosome(), vcfRecord.getPosition())) {
                return true;
            }
        }
        return false;
    }
}
