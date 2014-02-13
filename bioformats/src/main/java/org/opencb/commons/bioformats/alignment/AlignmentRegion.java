package org.opencb.commons.bioformats.alignment;

import org.opencb.commons.bioformats.alignment.stats.MeanCoverage;
import org.opencb.commons.bioformats.alignment.stats.RegionCoverage;

import java.util.List;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cgonzalez@cipf.es>
 */
public class AlignmentRegion {

    private String chromosome;
    private long start;     //Start of the first Alignment
    private long end;       //End of the last Alignment
    private boolean chromosomeTail;  //Indicates if the last alignment is the last in the chromosome

    private List<Alignment> alignments;     //Sorted Alignments
    private RegionCoverage coverage;
    private List<MeanCoverage> meanCoverage;

    public AlignmentRegion() {
    }

    public AlignmentRegion(String chromosome, long start, long end) {
        this(chromosome, start, end, null, null);
    }


    public AlignmentRegion(List<Alignment> alignments) {
        Alignment firstAlignment = alignments.get(0);
        Alignment lastAlignment = alignments.get(alignments.size()-1);
        //if(!firstAlignment.getChromosome().equals(lastAlignment.getChromosome())) //TODO jcoll: Limit this
            //System.out.println("All alignments must be in the same chromosome");
        this.chromosome = firstAlignment.getChromosome();
        this.start = firstAlignment.getStart();
        this.end = lastAlignment.getEnd();
        this.alignments = alignments;
        this.coverage = null;
    }

    public AlignmentRegion(String chromosome, long start, long end, List<Alignment> alignments, RegionCoverage coverage) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.alignments = alignments;
        this.coverage = coverage;
    }

    public List<Alignment> getAlignments() {
        return alignments;
    }

    public void setAlignments(List<Alignment> alignments) {
        this.alignments = alignments;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public RegionCoverage getCoverage() {
        return coverage;
    }

    public void setCoverage(RegionCoverage coverage) {
        this.coverage = coverage;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public boolean isChromosomeTail() {
        return chromosomeTail;
    }

    public void setChromosomeTail(boolean chromosomeTail) {
        this.chromosomeTail = chromosomeTail;
    }

    public List<MeanCoverage> getMeanCoverage() {
        return meanCoverage;
    }

    public void setMeanCoverage(List<MeanCoverage> meanCoverage) {

        this.meanCoverage = meanCoverage;
    }
}
