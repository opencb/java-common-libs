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
    }

    private void parse() throws Exception {

        BufferedReader reader = new BufferedReader(new FileReader(this.filename));
        String line = "";
        Individual ind, father, mother;
        String[]  fields;
        String sample_id, family_id, father_id, mother_id, sex, phenotype;
        Set<Individual> family;
        Queue<Individual> queue = new LinkedList<>();
        String[] aux_fields = null;

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

                if(fields.length > 6){
                     aux_fields  = Arrays.copyOfRange(fields, 6, fields.length);
                }

                family =  this.getFamily(family_id);
                if(family == null){
                    family = new TreeSet<>();
                    families.put(family_id, family);
                }


                    if( father_id.equals("0") && mother_id.equals("0")){
                        ind = new Individual(sample_id, family_id, null, null, sex, phenotype, aux_fields);
                        individuals.put(ind.getId(), ind);
                        family.add(ind);
                    }else{
                        ind = new Individual(sample_id, family_id, null, null, sex, phenotype, aux_fields);
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

                    // Añadimos "ind" como hijo a los padres
                    father.addChild(ind);
                    mother.addChild(ind);

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

                    // Añadimos "ind" como hijo al padre
                    father.addChild(ind);

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

                    // Añadimos "ind" como hijo a la madre
                    mother.addChild(ind);

                    family =  this.getFamily(ind.getFamily());
                    family.add(ind);
                }
            }
        }

        reader.close();
    }

    private Set<Individual> getFamily(String family_id) {
        return families.get(family_id);
    }

    private void parseHeader(String lineHeader) {

        String header = lineHeader.substring(1, lineHeader.length());
        String[] all_fields = header.split("\t");

        all_fields = Arrays.copyOfRange(all_fields, 6, all_fields.length);
        for (int i = 0; i< all_fields.length; i++){
            this.fields.put(all_fields[i], i);
        }
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
