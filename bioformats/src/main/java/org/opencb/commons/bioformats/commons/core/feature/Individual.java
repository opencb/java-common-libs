package org.opencb.commons.bioformats.commons.core.feature;

import org.opencb.commons.bioformats.commons.core.variant.vcf4.Condition;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.Sex;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Individual implements Comparable<Individual> {
    private String id;
    private String family;
    private Individual father;
    private String fatherId;
    private Individual mother;
    private String motherId;
    private String sex;
    private Sex sexCode;
    private String phenotype;
    private Condition condition;
    private String[] fields;
    private Set<Individual> children;


    public Individual(String id, String family, Individual father, Individual mother, String sex, String phenotype, String[] fields) {
        this.id = id;
        this.family = family;
        this.father = father;
        this.mother = mother;
        this.setSex(sex);
        this.setPhenotype(phenotype);
        this.fields = fields;
        this.children = new TreeSet<>();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("id=" + id);
        sb.append(", family=" + family);
        sb.append(", father=");
        if (father != null)
            sb.append(father.getId());
        else
            sb.append("0");

        sb.append(", mother=");
        if (mother != null)
            sb.append(mother.getId());
        else
            sb.append("0");

        sb.append(", sex=" + sex);
        sb.append(" phenotype=" + phenotype);
        if (fields != null && fields.length > 0)
            sb.append(", fields=" + Arrays.toString(fields));
        if (children.size() > 0) {
            sb.append(", children=[");
            for (Individual ind : children) {
                sb.append(ind.getId() + " ");
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
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
        if(phenotype == null || phenotype.equals("")){
            condition = Condition.MISSING_CONDITION;
        }else{
            switch (phenotype){
                case "1":
                    condition = Condition.AFFECTED;
                    break;
                case "2":
                    condition = Condition.UNAFFECTED;
                    break;
                default:
                    condition = Condition.UNKNOWN_CONDITION;

            }

        }    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
        switch (sex) {
            case "1":
                this.sexCode = Sex.MALE;
                break;
            case "2":
                this.sexCode = Sex.FEMALE;
                break;
            default:
                this.sexCode = Sex.UNKNOWN_SEX;
        }

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

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
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

    public Sex getSexCode() {
        return sexCode;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public Set<Individual> getChildren() {
        return children;
    }

    public void setChildren(Set<Individual> children) {
        this.children = children;
    }

    public boolean addChild(Individual ind) {
        return this.children.add(ind);
    }

    public Condition getCondition() {
        return condition;
    }

}
