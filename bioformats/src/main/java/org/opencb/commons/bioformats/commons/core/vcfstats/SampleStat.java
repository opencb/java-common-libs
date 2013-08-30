package org.opencb.commons.bioformats.commons.core.vcfstats;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/29/13
 * Time: 10:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class SampleStat {

    private String id;
    private int mendelianErrors;
    private int missingGenotypes;
    private int homozygotesNumber;


    public SampleStat(String id) {
        this.id = id;
        this.mendelianErrors = 0;
        this.missingGenotypes = 0;
        this.homozygotesNumber = 0;
    }

    public void incrementMendelianErrors(){
        this.mendelianErrors++;
    }

    public void incrementMissingGenotypes(){
        this.missingGenotypes++;
    }

    public void incrementHomozygotesNumber(){
        this.homozygotesNumber++;
    }

    public String getId() {
        return id;
    }

    public int getMendelianErrors() {
        return mendelianErrors;
    }

    public int getMissingGenotypes() {
        return missingGenotypes;
    }

    public int getHomozygotesNumber() {
        return homozygotesNumber;
    }

}
