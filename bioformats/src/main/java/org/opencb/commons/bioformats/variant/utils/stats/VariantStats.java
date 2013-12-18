package org.opencb.commons.bioformats.variant.utils.stats;


import org.opencb.commons.bioformats.feature.Genotype;

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
public class VariantStats {
    private String chromosome;
    private long position;
    private String refAllele;
    private String[] altAlleles;
    private String id;
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
    private boolean isSNP;
    private boolean pass;
    private float casesPercentDominant;
    private float controlsPercentDominant;
    private float casesPercentRecessive;
    private float controlsPercentRecessive;
    private int transitionsCount;
    private int transversionsCount;
    private float qual;
    private int samples;
    private VariantHardyWeinbergStats hw;


    public VariantStats() {
        this.chromosome = "";
        this.refAllele = "";
        this.altAlleles = null;
        this.mafAllele = "";
        this.mgfAllele = "";
        this.position = (long) 0;
        this.numAlleles = 0;
        this.allelesCount = null;
        this.genotypesCount = null;
        this.missingAlleles = 0;
        this.missingGenotypes = 0;
        this.mendelinanErrors = 0;
        this.allelesFreq = null;
        this.genotypesFreq = null;
        this.maf = 0;
        this.mgf = 0;
        this.casesPercentDominant = 0;
        this.controlsPercentDominant = 0;
        this.casesPercentRecessive = 0;
        this.controlsPercentRecessive = 0;
        this.isIndel = false;
        this.genotypes = new ArrayList<>((int) Math.pow(this.numAlleles, 2));
        this.transitionsCount = 0;
        this.transversionsCount = 0;
        this.hw = new VariantHardyWeinbergStats();
    }

    public VariantStats(String chromosome, int position, String allele_ref, String allele_alt, double maf,
                        double mgf, String allele_maf, String genotype_maf, int miss_allele, int miss_gt,
                        int mendel_err, int is_indel, double cases_percent_dominant,
                        double controls_percent_dominant, double cases_percent_recessive,
                        double controls_percent_recessive) {

        this.chromosome = chromosome;
        this.position = position;
        this.refAllele = allele_ref;
        this.altAlleles = allele_alt.split(",");
        this.maf = (float) maf;
        this.mgf = (float) mgf;
        this.mafAllele = allele_maf;
        this.mgfAllele = genotype_maf;
        this.missingAlleles = miss_allele;
        this.missingGenotypes = miss_gt;
        this.mendelinanErrors = mendel_err;
        this.isIndel = (is_indel == 1);
        this.casesPercentDominant = (float) cases_percent_dominant;
        this.controlsPercentDominant = (float) controls_percent_dominant;
        this.casesPercentRecessive = (float) cases_percent_recessive;
        this.controlsPercentRecessive = (float) controls_percent_recessive;

    }


    public VariantStats(String chromosome, int position, String allele_ref, String allele_alt, float maf,
                        float mgf, String allele_maf, String genotype_maf, int miss_allele, int miss_gt,
                        int mendel_err, boolean is_indel, float cases_percent_dominant,
                        float controls_percent_dominant, float cases_percent_recessive,
                        float controls_percent_recessive) {

        this.chromosome = chromosome;
        this.position = position;
        this.refAllele = allele_ref;
        this.altAlleles = allele_alt.split(",");
        this.maf =  maf;
        this.mgf =  mgf;
        this.mafAllele = allele_maf;
        this.mgfAllele = genotype_maf;
        this.missingAlleles = miss_allele;
        this.missingGenotypes = miss_gt;
        this.mendelinanErrors = mendel_err;
        this.isIndel = is_indel;
        this.casesPercentDominant = cases_percent_dominant;
        this.controlsPercentDominant = controls_percent_dominant;
        this.casesPercentRecessive =  cases_percent_recessive;
        this.controlsPercentRecessive = controls_percent_recessive;

    }
    @Override
    public String toString() {
        return "VariantStats{" +
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

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getRefAlleles() {
        return refAllele;
    }

    public String[] getAltAlleles() {
        return altAlleles;
    }

    public void setAltAlleles(String[] altAlleles) {
        this.altAlleles = altAlleles;
    }

    public String getMafAllele() {
        return mafAllele;
    }

    public void setMafAllele(String mafAllele) {
        this.mafAllele = mafAllele;
    }

    public String getMgfAllele() {
        return mgfAllele;
    }

    public void setMgfAllele(String mgfAllele) {
        this.mgfAllele = mgfAllele;
    }

    public Integer getNumAlleles() {
        return numAlleles;
    }

    public void setNumAlleles(int numAlleles) {
        this.numAlleles = numAlleles;
    }

    public int[] getAllelesCount() {
        return allelesCount;
    }

    public void setAllelesCount(int[] allelesCount) {
        this.allelesCount = allelesCount;
    }

    public int[] getGenotypesCount() {
        return genotypesCount;
    }

//    public boolean getIndel() {
//        return isIndel;
//    }

    public void setGenotypesCount(int[] genotypesCount) {
        this.genotypesCount = genotypesCount;
    }

    public float[] getAllelesFreq() {
        return allelesFreq;
    }

    public void setAllelesFreq(float[] allelesFreg) {
        this.allelesFreq = allelesFreg;
    }

    public float[] getGenotypesFreq() {
        return genotypesFreq;
    }

    public void setGenotypesFreq(float[] genotypesFreq) {
        this.genotypesFreq = genotypesFreq;
    }

    public float getMaf() {
        return maf;
    }

    public void setMaf(float maf) {
        this.maf = maf;
    }

    public float getMgf() {
        return mgf;
    }

    public void setMgf(float mgf) {
        this.mgf = mgf;
    }

    public int getMissingAlleles() {
        return missingAlleles;
    }

    public void setMissingAlleles(int missingAlleles) {
        this.missingAlleles = missingAlleles;
    }

    public int getMissingGenotypes() {
        return missingGenotypes;
    }

    public void setMissingGenotypes(int missingGenotypes) {
        this.missingGenotypes = missingGenotypes;
    }

    public int getMendelinanErrors() {
        return mendelinanErrors;
    }

    public void setMendelinanErrors(int mendelinanErrors) {
        this.mendelinanErrors = mendelinanErrors;
    }

    public float getCasesPercentDominant() {
        return casesPercentDominant;
    }

    public void setCasesPercentDominant(float casesPercentDominant) {
        this.casesPercentDominant = casesPercentDominant;
    }

    public float getControlsPercentDominant() {
        return controlsPercentDominant;
    }

    public void setControlsPercentDominant(float controlsPercentDominant) {
        this.controlsPercentDominant = controlsPercentDominant;
    }

    public float getCasesPercentRecessive() {
        return casesPercentRecessive;
    }

    public void setCasesPercentRecessive(float casesPercentRecessive) {
        this.casesPercentRecessive = casesPercentRecessive;
    }

    public float getControlsPercentRecessive() {
        return controlsPercentRecessive;
    }

    public void setControlsPercentRecessive(float controlsPercentRecessive) {
        this.controlsPercentRecessive = controlsPercentRecessive;
    }

    public void setRefAllele(String refAllele) {
        this.refAllele = refAllele;
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

    public VariantHardyWeinbergStats getHw() {
        return hw;
    }

    public boolean isIndel() {
        return isIndel;
    }

    public void setIndel(boolean indel) {
        this.isIndel = indel;
    }

    public boolean isSNP() {
        return isSNP;
    }

    public void setSNP(boolean SNP) {
        isSNP = SNP;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public float getQual() {
        return qual;
    }

    public void setQual(float qual) {
        this.qual = qual;
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
