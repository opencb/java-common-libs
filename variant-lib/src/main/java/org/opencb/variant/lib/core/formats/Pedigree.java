package org.opencb.variant.lib.core.formats;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Pedigree {

    private Map<String,Individual> individuals;
    private Map<String, Set<Individual>> families;
    private Map<String, Integer> fields;

    public Pedigree() {
        individuals = new LinkedHashMap<>(100);
        families = new LinkedHashMap<>(100);
        fields= new LinkedHashMap<>(5);
    }

    public Set<Individual> getFamily(String familyId) {
        return families.get(familyId);
    }

    public Individual getIndividual(String id){
        return individuals.get(id);
    }

    public Map<String, Individual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(Map<String, Individual> individuals) {
        this.individuals = individuals;
    }

    public Map<String, Set<Individual>> getFamilies() {
        return families;
    }

    public void setFamilies(Map<String, Set<Individual>> families) {
        this.families = families;
    }

    public Map<String, Integer> getFields() {
        return fields;
    }

    public void setFields(Map<String, Integer> fields) {
        this.fields = fields;
    }

    public void addIndividual(Individual ind){
        this.individuals.put(ind.getId(), ind);
    }

    public void addIndividualToFamily(String familyId, Individual ind){

        this.families.get(familyId).add(ind);

    }

    public void addFamily(String familyId, Set<Individual> family){
        this.getFamilies().put(familyId, family);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pedigree\n");
        if(fields.size() > 0){
            sb.append("fields = " + fields.keySet().toString() + "\n");
        }

        for(Map.Entry<String,Set<Individual>> elem: this.families.entrySet()){
            sb.append(elem.getKey() + "\n");
            for(Individual ind : elem.getValue()){
                sb.append("\t" + ind.toString() + "\n");
            }
        }
        return sb.toString();
    }
}
