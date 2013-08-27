package org.opencb.commons.bioformats.commons.core.feature;

import org.opencb.commons.bioformats.commons.core.feature.io.PedReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
        String sample_id, family_id, father_id, mother_id;
        Set<Individual> family;

        while( (line = reader.readLine()) != null){
            if(line.startsWith("#")){
                this.parseHeader(line);
            }else{
                fields = line.split("\t");
                family_id = fields[0];
                sample_id = fields[1];
                father_id = fields[2];
                mother_id = fields[3];

                ind = this.getIndividual(sample_id);
                family =  this.getFamily(family_id);
                if(family == null){
                    family = new TreeSet<>();
                    families.put(family_id, family);
                }

                if(ind == null){
                    father = this.getIndividual(father_id);
                    mother = this.getIndividual(mother_id);
                    ind = new Individual(sample_id, family_id, father, mother);
                    individuals.put(ind.getId(), ind);

                    family.add(ind);
                }else{
                    // error
                    // throw new Exception("Duplicate Element" + line);


                    // Creamos el padre aunque sea NULL y aquí lo recogemos y le ponemos los valores bien

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
