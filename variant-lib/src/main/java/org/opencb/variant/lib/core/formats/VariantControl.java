package org.opencb.variant.lib.core.formats;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 10/1/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantControl {

    @JsonProperty
    private float maf;

    @JsonProperty
    private String allele;

    VariantControl(float maf, String allele) {
        this.maf = maf;
        this.allele = allele;
    }

    public VariantControl() {
        this.maf = -1;
        this.allele = "";
    }

    public float getMaf() {
        return maf;
    }

    public void setMaf(float maf) {
        this.maf = maf;
    }

    public String getAllele() {
        return allele;
    }

    public void setAllele(String allele) {
        this.allele = allele;
    }
}
