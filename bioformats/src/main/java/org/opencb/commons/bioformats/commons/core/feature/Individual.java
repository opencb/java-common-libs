package org.opencb.commons.bioformats.commons.core.feature;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Individual implements Comparable<Individual>{
    private String id;
    private String family;
    private Individual father;
    private String fatherId;
    private Individual mother;
    private String motherId;
    private String sex;
    private String phenotype;
    private List<String> fields;


    public Individual(){

    }
    public Individual(String id, String family, Individual father, Individual mother, String sex, String phenotype, List<String> fields) {
        this.id = id;
        this.family = family;
        this.father = father;
        this.mother = mother;
        this.sex = sex;
        this.phenotype = phenotype;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Individual{" +
                "id='" + id + '\'' +
                "family='" + family + '\'' +
                ", phenotype='" + phenotype + '\'' +
                ", sex='" + sex + '\'' +
                ", father=" + father +
                ", mother=" + mother +
                ", fields=" + fields +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(String phenotype) {
        this.phenotype = phenotype;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Individual getFather() {
        return father;
    }

    public void setFather(Individual father) {
        this.father = father;
    }

    public Individual getMother() {
        return mother;
    }

    public void setMother(Individual mother) {
        this.mother = mother;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Individual)) return false;

        Individual that = (Individual) o;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + phenotype.hashCode();
        result = 31 * result + sex.hashCode();
        result = 31 * result + father.hashCode();
        result = 31 * result + mother.hashCode();
        result = 31 * result + fields.hashCode();
        return result;
    }

    @Override
    public int compareTo(Individual o) {
        return this.getId().compareTo(o.getId());
    }

    public String getFatherId() {
        return fatherId;
    }

    public void setFatherId(String fatherId) {
        this.fatherId = fatherId;
    }

    public String getMotherId() {
        return motherId;
    }

    public void setMotherId(String motherId) {
        this.motherId = motherId;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }
}
