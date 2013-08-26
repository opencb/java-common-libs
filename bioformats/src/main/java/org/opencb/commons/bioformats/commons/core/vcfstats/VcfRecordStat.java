package org.opencb.commons.bioformats.commons.core.vcfstats;

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
    private String ref_allele;
    private String alt_allele;
    private String maf_allele;
    private String mgf_allele;
    private Integer num_alleles;
    private Integer alleles_count;

    private Integer genotypes_count;

    public VcfRecordStat(){

    }

    public String getChromosome() {
        return chromosome;
    }

    public Long getPosition() {
        return position;
    }

    public String getRef_allele() {
        return ref_allele;
    }

    public String getAlt_allele() {
        return alt_allele;
    }

    public String getMaf_allele() {
        return maf_allele;
    }

    public String getMgf_allele() {
        return mgf_allele;
    }

    public Integer getNum_alleles() {
        return num_alleles;
    }

    public Integer getAlleles_count() {
        return alleles_count;
    }

    public Integer getGenotypes_count() {
        return genotypes_count;
    }

    public Float getAlleles_freg() {
        return alleles_freg;
    }

    public Float getGenotypes_freq() {
        return genotypes_freq;
    }

    public Float getMaf() {
        return maf;
    }

    public Float getMgf() {
        return mgf;
    }

    public Integer getMissing_alleles() {
        return missing_alleles;
    }

    public Integer getMissing_genotypes() {
        return missing_genotypes;
    }

    public Integer getMendelinan_errors() {
        return mendelinan_errors;
    }

    public Boolean getIs_indel() {
        return is_indel;
    }

    public Float getCases_percent_dominant() {
        return cases_percent_dominant;
    }

    public Float getControls_percent_dominant() {
        return controls_percent_dominant;
    }

    public Float getCases_percent_recessive() {
        return cases_percent_recessive;
    }

    public Float getControls_percent_recessive() {
        return controls_percent_recessive;
    }

    private Float alleles_freg;
    private Float genotypes_freq;
    private Float maf;
    private Float mgf;
    private Integer missing_alleles;
    private Integer missing_genotypes;
    private Integer mendelinan_errors;
    private Boolean is_indel;
    private Float cases_percent_dominant;
    private Float controls_percent_dominant;
    private Float cases_percent_recessive;
    private Float controls_percent_recessive;

}
