package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.opencb.commons.bioformats.commons.core.variant.vcf4.Genotype;

import java.util.ArrayList;
import java.util.Arrays;
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
    private long position;
    private String refAllele;
    private String[] altAlleles;
    private String mafAllele;
    private String mgfAllele;
    private int numAlleles;
    private int[] allelesCount;
    private int[] genotypesCount;
    private List<Genotype> genotypes;


    private float[] allelesFreq;
    private float[] genotypesFreq;
    private float maf;
    private float mgf;
    private int missingAlleles;
    private int missingGenotypes;
    private int mendelinanErrors;
    private boolean isIndel;
    private float casesPercentDominant;
    private float controlsPercentDominant;
    private float casesPercentRecessive;
    private float controlsPercentRecessive;
    private int transitionsCount;
    private int transversionsCount;

    private VcfHardyWeinbergStat hw;


    public VcfRecordStat() {
        this.chromosome               = "";
        this.refAllele                = "";
        this.altAlleles               = null;
        this.mafAllele                = "";
        this.mgfAllele                = "";
        this.position                 = (long) 0;
        this.numAlleles               = 0;
        this.allelesCount             = null;
        this.genotypesCount           = null;
        this.missingAlleles           = 0;
        this.missingGenotypes         = 0;
        this.mendelinanErrors         = 0;
        this.allelesFreq              = null;
        this.genotypesFreq            = null;
        this.maf                      = 0;
        this.mgf                      = 0;
        this.casesPercentDominant     = 0;
        this.controlsPercentDominant  = 0;
        this.casesPercentRecessive    = 0;
        this.controlsPercentRecessive = 0;
        this.isIndel                  = false;
        this.genotypes                = new ArrayList<>((int) Math.pow(this.numAlleles, 2));
        this.transitionsCount         = 0;
        this.transversionsCount       = 0;
        this.hw = new VcfHardyWeinbergStat();
    }

    @Override
    public String toString() {
        return "VcfRecordStat{" +
                "chromosome='" + chromosome + '\'' +
                ", position=" + position +
                ", refAllele='" + refAllele + '\'' +
                ", altAlleles=" + Arrays.toString(altAlleles) +
                ", mafAllele='" + mafAllele + '\'' +
                ", mgfAllele='" + mgfAllele + '\'' +
                ", numAlleles=" + numAlleles +
                ", allelesCount=" + Arrays.toString(allelesCount) +
                ", genotypesCount=" + Arrays.toString(genotypesCount) +
                ", genotypes=" + genotypes +
                ", allelesFreq=" + Arrays.toString(allelesFreq) +
                ", genotypesFreq=" + Arrays.toString(genotypesFreq) +
                ", maf=" + maf +
                ", mgf=" + mgf +
                ", missingAlleles=" + missingAlleles +
                ", missingGenotypes=" + missingGenotypes +
                ", mendelinanErrors=" + mendelinanErrors +
                ", isIndel=" + isIndel +
                ", casesPercentDominant=" + casesPercentDominant +
                ", controlsPercentDominant=" + controlsPercentDominant +
                ", casesPercentRecessive=" + casesPercentRecessive +
                ", controlsPercentRecessive=" + controlsPercentRecessive +
                ", transitionsCount=" + transitionsCount +
                ", transversionsCount=" + transversionsCount +
                '}';
    }

    public String getChromosome() {
        return chromosome;
    }

    public Long getPosition() {
        return position;
    }

    public String getRefAlleles() {
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

    public int[] getAllelesCount() {
        return allelesCount;
    }

    public int[] getGenotypesCount() {
        return genotypesCount;
    }

    public float[] getAllelesFreq() {
        return allelesFreq;
    }

    public float[] getGenotypesFreq() {
        return genotypesFreq;
    }

    public float getMaf() {
        return maf;
    }

    public float getMgf() {
        return mgf;
    }

    public int getMissingAlleles() {
        return missingAlleles;
    }

    public int getMissingGenotypes() {
        return missingGenotypes;
    }

    public int getMendelinanErrors() {
        return mendelinanErrors;
    }

    public boolean getIndel() {
        return isIndel;
    }

    public float getCasesPercentDominant() {
        return casesPercentDominant;
    }

    public float getControlsPercentDominant() {
        return controlsPercentDominant;
    }

    public float getCasesPercentRecessive() {
        return casesPercentRecessive;
    }

    public float getControlsPercentRecessive() {
        return controlsPercentRecessive;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public void setPosition(long position) {
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

    public void setNumAlleles(int numAlleles) {
        this.numAlleles = numAlleles;
    }

    public void setAllelesCount(int[] allelesCount) {
        this.allelesCount = allelesCount;
    }

    public void setGenotypesCount(int[] genotypesCount) {
        this.genotypesCount = genotypesCount;
    }

    public void setAllelesFreq(float[] allelesFreg) {
        this.allelesFreq = allelesFreg;
    }

    public void setGenotypesFreq(float[] genotypesFreq) {
        this.genotypesFreq = genotypesFreq;
    }

    public void setMaf(float maf) {
        this.maf = maf;
    }

    public void setMgf(float mgf) {
        this.mgf = mgf;
    }

    public void setMissingAlleles(int missingAlleles) {
        this.missingAlleles = missingAlleles;
    }

    public void setMissingGenotypes(int missingGenotypes) {
        this.missingGenotypes = missingGenotypes;
    }

    public void setMendelinanErrors(int mendelinanErrors) {
        this.mendelinanErrors = mendelinanErrors;
    }

    public void setIndel(boolean indel) {
        this.isIndel = indel;
    }

    public void setCasesPercentDominant(float casesPercentDominant) {
        this.casesPercentDominant = casesPercentDominant;
    }

    public void setControlsPercentDominant(float controlsPercentDominant) {
        this.controlsPercentDominant = controlsPercentDominant;
    }

    public void setCasesPercentRecessive(float casesPercentRecessive) {
        this.casesPercentRecessive = casesPercentRecessive;
    }

    public void setControlsPercentRecessive(float controlsPercentRecessive) {
        this.controlsPercentRecessive = controlsPercentRecessive;
    }


    public List<Genotype> getGenotypes() {
        return genotypes;
    }

    public void setGenotypes(List<Genotype> genotypes) {
        this.genotypes = genotypes;
    }

    public int getTransitionsCount() {
        return transitionsCount;
    }

    public void setTransitionsCount(int transitionsCount) {
        this.transitionsCount = transitionsCount;
    }

    public int getTransversionsCount() {
        return transversionsCount;
    }

    public void setTransversionsCount(int transversionsCount) {
        this.transversionsCount = transversionsCount;
    }

    public VcfHardyWeinbergStat getHw() {
        return hw;
    }
}
