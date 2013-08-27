package org.opencb.commons.bioformats.commons.core.feature;

import org.opencb.commons.bioformats.commons.core.feature.io.PedReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
    private String filename;

    public Pedigree(String filename) throws Exception {

        individuals = new LinkedHashMap<>(100);
        families = new LinkedHashMap<>(100);
        fields= new LinkedHashMap<>(5);

        this.filename = filename;
        this.parse();
        this.reCheckFamilyDependencies();


    }

    private void parse() throws Exception {

        BufferedReader reader = new BufferedReader(new FileReader(this.filename));
        String line = "";
        Individual ind, father, mother;
        String[]  fields;
        String sample_id, family_id, father_id, mother_id, sex, phenotype;
        Set<Individual> family;
        Queue<Individual> queue = new LinkedList<>();

        while( (line = reader.readLine()) != null)  {
            if(line.startsWith("#")){
                this.parseHeader(line);
            }else{
                fields = line.split("\t");
                family_id = fields[0];
                sample_id = fields[1];
                father_id = fields[2];
                mother_id = fields[3];
                sex = fields[4];
                phenotype = fields[5];

                family =  this.getFamily(family_id);
                if(family == null){
                    family = new TreeSet<>();
                    families.put(family_id, family);
                }


                    if( father_id.equals("0") && mother_id.equals("0")){
                        ind = new Individual(sample_id, family_id, null, null, sex, phenotype, null);
                        individuals.put(ind.getId(), ind);
                        family.add(ind);
                    }else{
                        ind = new Individual(sample_id, family_id, null, null, sex, phenotype, null);
                        ind.setFatherId(father_id);
                        ind.setMotherId(mother_id);
                        queue.offer(ind);
                    }



            }
        }

        while(!queue.isEmpty()){
            ind = queue.poll();
            father = null;
            mother = null;
            father_id = ind.getFatherId();
            mother_id = ind.getMotherId();

            if(!father_id.equals("0") && !mother_id.equals("0")){ // Existen padre y Madre (hay ID)
                father = this.getIndividual(father_id);
                mother = this.getIndividual(mother_id);
                if(father == null || mother == null){  // Existen pero aún no se han metido en la HASH
                    queue.offer(ind);
                }else{ // Tenemos el padre y la madre y están en la HASH
                    ind.setFather(father);
                    ind.setMother(mother);
                    individuals.put(ind.getId(), ind);

                    family =  this.getFamily(ind.getFamily());
                    family.add(ind);
                }
            }else if(!father_id.equals("0") && mother_id.equals("0")){ // No existe la madre
                father = this.getIndividual(father_id);
                mother = null;
                if(father == null){ // Existe el padre pero aún no se ha metido en la hash
                    queue.offer(ind);
                }else{
                    ind.setFather(father);
                    ind.setMother(mother);
                    individuals.put(ind.getId(), ind);

                    family =  this.getFamily(ind.getFamily());
                    family.add(ind);
                }
            }else if(father_id.equals("0") && !mother_id.equals("0")){ // No existe el padre
                father = null;
                mother = this.getIndividual(mother_id);
                if(mother == null){ // Existe la madre pero aún no se ha metido en la hash
                    queue.offer(ind);
                }else{
                    ind.setFather(father);
                    ind.setMother(mother);
                    individuals.put(ind.getId(), ind);

                    family =  this.getFamily(ind.getFamily());
                    family.add(ind);
                }
            }
        }

        reader.close();
    }

    private void reCheckFamilyDependencies(){

        Individual aux;
        for(Map.Entry<String, Individual> elem: this.getIndividuals().entrySet()){
            if(elem.getValue().getFather() == null){

            }
        }

    }

    private Set<Individual> getFamily(String family_id) {
        return families.get(family_id);
    }

    private void parseHeader(String lineHeader) {
        // TODO aaleman: terminar parserHeader
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
}
