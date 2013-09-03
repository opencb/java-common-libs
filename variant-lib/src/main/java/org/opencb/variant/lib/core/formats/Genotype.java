package org.opencb.variant.lib.core.formats;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Genotype {
    private Integer allele1;
    private Integer allele2;
    private AllelesCode code;
    private Integer count;


    public Genotype(String genotype) {
        this.code = null;
        this.count = 0;
        if (genotype.length() < 3) {
            this.allele1 = null;
            this.allele2 = null;
            this.code = AllelesCode.ALL_ALLELES_MISSING;
        } else {
            String[] auxAlleles = genotype.split("/");
            if (auxAlleles[0].equals(".")) {
                this.allele1 = null;
                this.code = AllelesCode.FIRST_ALLELE_MISSING;
            } else {
                this.allele1 = Integer.valueOf(auxAlleles[0]);
            }

            if (auxAlleles.length == 1) { // Haploid
                this.allele2 = null;
                this.code = AllelesCode.HAPLOID;

            } else {
                if (auxAlleles[1].equals(".")) {
                    this.allele2 = null;
                    this.code = (this.code == AllelesCode.FIRST_ALLELE_MISSING) ? AllelesCode.ALL_ALLELES_MISSING : AllelesCode.SECOND_ALLELE_MISSING;
                } else {
                    this.allele2 = Integer.valueOf(auxAlleles[1]);
                }
            }

        }
        if (this.code == null) {
            this.code = AllelesCode.ALLELES_OK;

        }

    }

    public Integer getAllele1() {
        return allele1;
    }

    public void setAllele1(Integer allele1) {
        this.allele1 = allele1;
    }

    public Integer getAllele2() {
        return allele2;
    }

    public void setAllele2(Integer allele2) {
        this.allele2 = allele2;
    }

    public AllelesCode getCode() {
        return code;
    }

    public void setCode(AllelesCode code) {
        this.code = code;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(6);
        if(allele1 != null){
            sb.append(allele1);
        }else{
            sb.append(".");
        }
        sb.append("/");

        if(allele2 != null){
            sb.append(allele2);
        }else{
            sb.append(".");
        }
        sb.append(":");
        sb.append(count);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Genotype) {
            Genotype g = (Genotype) obj;
            return this.getAllele1() == g.getAllele1() &&
                    this.getAllele2() == g.getAllele2();
        } else {
            return false;
        }
    }

      public boolean isAllele1Ref(){
         return allele1 == 0;
      }

    public boolean isAllele2Ref(){
        return allele2 == 0;
    }
}
