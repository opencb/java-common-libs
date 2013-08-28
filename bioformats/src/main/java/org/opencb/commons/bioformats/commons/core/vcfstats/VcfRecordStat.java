package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.opencb.commons.bioformats.commons.core.variant.vcf4.Genotype;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/26/13
 * Time: 1:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfRecordStat {
    private String chromosome;
    private Long position;
    private String refAllele;
    private String[] altAlleles;
    private String mafAllele;
    private String mgfAllele;
    private Integer numAlleles;
    private Integer[] allelesCount;
    private Integer[] genotypesCount;
    private List<Genotype> genotypes;


    private Float[] allelesFreq;
    private Float[] genotypesFreq;
    private Float maf;
    private Float mgf;
    private Integer missingAlleles;
    private Integer missingGenotypes;
    private Integer mendelinanErrors;
    private Boolean isIndel;
    private Float casesPercentDominant;
    private Float controlsPercentDominant;
    private Float casesPercentRecessive;
    private Float controlsPercentRecessive;
    private Integer transitionsCount;
    private Integer transversionsCount;


    public VcfRecordStat() {
        this.chromosome                 = "";
        this.refAllele = "";
        this.altAlleles = null;
        this.mafAllele = "";
        this.mgfAllele = "";
        this.position                   = new Long(0);
        this.numAlleles = new Integer(0);
        this.allelesCount = null;
        this.genotypesCount = null;
        this.missingAlleles = new Integer(0);
        this.missingGenotypes = new Integer(0);
        this.mendelinanErrors = new Integer(0);
        this.allelesFreq = null;
        this.genotypesFreq = null;
        this.maf                        = new Float(0.0);
        this.mgf                        = new Float(0.0);
        this.casesPercentDominant = new Float(0.0);
        this.controlsPercentDominant = new Float(0.0);
        this.casesPercentRecessive = new Float(0.0);
        this.controlsPercentRecessive = new Float(0.0);
        this.isIndel = false;
        this.genotypes = new ArrayList<Genotype>((int) Math.pow(this.numAlleles, 2));
        this.transitionsCount = 0;
        this.transversionsCount = 0;
    }

    @Override
    public String toString() {
        return "";
    }

    public String getChromosome() {
        return chromosome;
    }

    public Long getPosition() {
        return position;
    }

    public String getRef_alleles() {
        return refAllele;
    }

    public String[] getAltAlleles() {
        return altAlleles;
    }

    public String getMafAllele() {
        return mafAllele;
    }

    public String getMgfAllele() {
        return mgfAllele;
    }

    public Integer getNumAlleles() {
        return numAlleles;
    }

    public Integer[] getAllelesCount() {
        return allelesCount;
    }

    public Integer[] getGenotypesCount() {
        return genotypesCount;
    }

    public Float[] getAllelesFreq() {
        return allelesFreq;
    }

    public Float[] getGenotypesFreq() {
        return genotypesFreq;
    }

    public Float getMaf() {
        return maf;
    }

    public Float getMgf() {
        return mgf;
    }

    public Integer getMissingAlleles() {
        return missingAlleles;
    }

    public Integer getMissingGenotypes() {
        return missingGenotypes;
    }

    public Integer getMendelinanErrors() {
        return mendelinanErrors;
    }

    public Boolean getIndel() {
        return isIndel;
    }

    public Float getCasesPercentDominant() {
        return casesPercentDominant;
    }

    public Float getControlsPercentDominant() {
        return controlsPercentDominant;
    }

    public Float getCasesPercentRecessive() {
        return casesPercentRecessive;
    }

    public Float getControlsPercentRecessive() {
        return controlsPercentRecessive;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public void setRefAllele(String refAllele) {
        this.refAllele = refAllele;
    }

    public void setAltAlleles(String[] altAlleles) {
        this.altAlleles = altAlleles;
    }

    public void setMafAllele(String mafAllele) {
        this.mafAllele = mafAllele;
    }

    public void setMgfAllele(String mgfAllele) {
        this.mgfAllele = mgfAllele;
    }

    public void setNumAlleles(Integer numAlleles) {
        this.numAlleles = numAlleles;
    }

    public void setAllelesCount(Integer[] allelesCount) {
        this.allelesCount = allelesCount;
    }

    public void setGenotypesCount(Integer[] genotypesCount) {
        this.genotypesCount = genotypesCount;
    }

    public void setAlleles_freg(Float[] alleles_freg) {
        this.allelesFreq = alleles_freg;
    }

    public void setGenotypesFreq(Float[] genotypesFreq) {
        this.genotypesFreq = genotypesFreq;
    }

    public void setMaf(Float maf) {
        this.maf = maf;
    }

    public void setMgf(Float mgf) {
        this.mgf = mgf;
    }

    public void setMissingAlleles(Integer missingAlleles) {
        this.missingAlleles = missingAlleles;
    }

    public void setMissingGenotypes(Integer missingGenotypes) {
        this.missingGenotypes = missingGenotypes;
    }

    public void setMendelinanErrors(Integer mendelinanErrors) {
        this.mendelinanErrors = mendelinanErrors;
    }

    public void setIndel(Boolean indel) {
        this.isIndel = indel;
    }

    public void setCasesPercentDominant(Float casesPercentDominant) {
        this.casesPercentDominant = casesPercentDominant;
    }

    public void setControlsPercentDominant(Float controlsPercentDominant) {
        this.controlsPercentDominant = controlsPercentDominant;
    }

    public void setCasesPercentRecessive(Float casesPercentRecessive) {
        this.casesPercentRecessive = casesPercentRecessive;
    }

    public void setControlsPercentRecessive(Float controlsPercentRecessive) {
        this.controlsPercentRecessive = controlsPercentRecessive;
    }


    public List<Genotype> getGenotypes() {
        return genotypes;
    }

    public void setGenotypes(List<Genotype> genotypes) {
        this.genotypes = genotypes;
    }

    public Integer getTransitionsCount() {
        return transitionsCount;
    }

    public void setTransitionsCount(Integer transitionsCount) {
        this.transitionsCount = transitionsCount;
    }

    public Integer getTransversionsCount() {
        return transversionsCount;
    }

    public void setTransversionsCount(Integer transversionsCount) {
        this.transversionsCount = transversionsCount;
    }
}
