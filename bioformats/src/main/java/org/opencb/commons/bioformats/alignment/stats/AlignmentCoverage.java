package org.opencb.commons.bioformats.alignment.stats;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/4/13
 * Time: 6:14 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class AlignmentCoverage {

    private String chromosome;
    private int start;
    private int end;
    private float coverage;

    public AlignmentCoverage() {
        this("",-1,-1,-1);
    }

    public AlignmentCoverage(String chromosome, int start, int end, float coverage) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.coverage = coverage;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public float getCoverage() {
        return coverage;
    }

    public void setCoverage(float coverage) {
        this.coverage = coverage;
    }
}
