package org.opencb.commons.bioformats.variant.filters;


import org.opencb.commons.bioformats.feature.Region;
import org.opencb.commons.bioformats.variant.Variant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantRegionFilter extends VariantFilter {

    private List<Region> regionList;

    public VariantRegionFilter(String chromosome, long start, long end) {
        super();
        regionList = new ArrayList<>();
        regionList.add(new Region(chromosome, start, end));
    }

    public VariantRegionFilter(String chromosome, long start, long end, int priority) {
        super(priority);
        regionList = new ArrayList<>();
        regionList.add(new Region(chromosome, start, end));

    }

    public VariantRegionFilter(String regions) {
        super();
        regionList = new ArrayList<>();

        String[] splits = regions.split(",");
        for (String split : splits) {
            regionList.add(new Region(split));
        }
    }

    public VariantRegionFilter(String regions, int priority) {
        super(priority);
        regionList = new ArrayList<>();

        String[] splits = regions.split(",");
        for (String split : splits) {
            regionList.add(new Region(split));
        }
    }

    @Override
    public boolean apply(Variant variant) {
        for (Region r : regionList) {
            if (r.contains(variant.getChromosome(), variant.getPosition())) {
                return true;
            }
        }
        return false;
    }
}
