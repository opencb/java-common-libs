package org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters;

import com.google.common.base.Predicate;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 10:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfRegionFilter implements Predicate<VcfRecord> {

    private String chromosome;
    private long start;
    private long end;

    public VcfRegionFilter(String chromosome, long start, long end) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean apply(VcfRecord vcfRecord) {
        return (vcfRecord.getChromosome().equalsIgnoreCase(chromosome) &&
                vcfRecord.getPosition() >= start
                && vcfRecord.getPosition() <= end);
    }
}
