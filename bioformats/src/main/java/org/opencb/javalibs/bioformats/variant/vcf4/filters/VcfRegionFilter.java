package org.opencb.javalibs.bioformats.variant.vcf4.filters;


import org.opencb.javalibs.bioformats.variant.vcf4.VcfRecord;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 10:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfRegionFilter extends VcfFilter {

    private String chromosome;
    private long start;
    private long end;

    public VcfRegionFilter(String chromosome, long start, long end) {
        super();
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
    }

    public VcfRegionFilter(String chromosome, long start, long end, int priority) {
        super(priority);
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
