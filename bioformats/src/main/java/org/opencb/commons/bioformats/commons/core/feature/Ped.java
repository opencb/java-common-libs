package org.opencb.commons.bioformats.commons.core.feature;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Ped {
    private String family;
    private String sample;
    private String parental_id;
    private String maternal_id;
    private String sex;
    private String phenotype;

    public Ped(String family, String sample, String parental_id, String maternal_id, String sex, String phenotype) {
        this.family = family;
        this.sample = sample;
        this.parental_id = parental_id;
        this.maternal_id = maternal_id;
        this.sex = sex;
        this.phenotype = phenotype;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public String getParental_id() {
        return parental_id;
    }

    public void setParental_id(String parental_id) {
        this.parental_id = parental_id;
    }

    public String getMaternal_id() {
        return maternal_id;
    }

    public void setMaternal_id(String maternal_id) {
        this.maternal_id = maternal_id;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(String phenotype) {
        this.phenotype = phenotype;
    }

    @Override
    public String toString() {
        return "Ped{" +
                "family='" + family + '\'' +
                ", sample='" + sample + '\'' +
                ", parental_id='" + parental_id + '\'' +
                ", maternal_id='" + maternal_id + '\'' +
                ", sex='" + sex + '\'' +
                ", phenotype='" + phenotype + '\'' +
                '}';
    }
}
