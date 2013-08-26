package org.opencb.commons.bioformats.commons.core.vcfstats;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Genotype {
    private Integer allele_1;
    private Integer allele_2;
    private AllelesCode code;

    public Integer getAllele_1() {
        return allele_1;
    }

    public void setAllele_1(Integer allele_1) {
        this.allele_1 = allele_1;
    }

    public Integer getAllele_2() {
        return allele_2;
    }

    public void setAllele_2(Integer allele_2) {
        this.allele_2 = allele_2;
    }

    public AllelesCode getCode() {
        return code;
    }

    public void setCode(AllelesCode code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return allele_1 + "/" + allele_2 + "(" + code + ")";
    }

    public Genotype(String genotype){
        this.code = null;
        if(genotype.length() < 3){
            this.allele_1 = null;
            this.allele_2 = null;
            this.code = AllelesCode.ALL_ALLELES_MISSING;
        }else{
            String[] aux_alleles = genotype.split("/");
            if(aux_alleles[0].equals(".")){
                this.allele_1 = null;
                this.code = AllelesCode.FIRST_ALLELE_MISSING;
            }
            else{
                this.allele_1 = Integer.valueOf(aux_alleles[0]);
            }

            if(aux_alleles.length == 1){ // Haploid
                this.allele_2 = null;
                this.code = AllelesCode.HAPLOID;

            }else{
                if(aux_alleles[1].equals(".")){
                    this.allele_2 = null;
                    this.code = (this.code == AllelesCode.FIRST_ALLELE_MISSING) ? AllelesCode.ALL_ALLELES_MISSING : AllelesCode.SECOND_ALLELE_MISSING;
                }else{
                    this.allele_2 = Integer.valueOf(aux_alleles[1]);
                }
            }


        }
        if(this.code == null){
            this.code = AllelesCode.ALLELES_OK  ;

        }

    }
}
