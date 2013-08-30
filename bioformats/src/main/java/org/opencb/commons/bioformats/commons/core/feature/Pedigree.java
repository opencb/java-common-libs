package org.opencb.commons.bioformats.commons.core.feature;


import java.io.BufferedReader;
import java.io.FileReader;
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
        String sampleId, familyId, fatherId, motherId, sex, phenotype;
        Set<Individual> family;
        Queue<Individual> queue = new LinkedList<>();
        String[] auxFields = null;

        while( (line = reader.readLine()) != null)  {
            if(line.startsWith("#")){
                this.parseHeader(line);
            }else{
                fields = line.split("\t");
                familyId = fields[0];
                sampleId = fields[1];
                fatherId = fields[2];
                motherId = fields[3];
                sex = fields[4];
                phenotype = fields[5];

                if(fields.length > 6){
                     auxFields  = Arrays.copyOfRange(fields, 6, fields.length);
                }

                family =  this.getFamily(familyId);
                if(family == null){
                    family = new TreeSet<>();
                    families.put(familyId, family);
                }


                    if( fatherId.equals("0") && motherId.equals("0")){
                        ind = new Individual(sampleId, familyId, null, null, sex, phenotype, auxFields);
                        individuals.put(ind.getId(), ind);
                        family.add(ind);
                    }else{
                        ind = new Individual(sampleId, familyId, null, null, sex, phenotype, auxFields);
                        ind.setFatherId(fatherId);
                        ind.setMotherId(motherId);
                        queue.offer(ind);
                    }



            }
        }

        while(!queue.isEmpty()){
            ind = queue.poll();
            father = null;
            mother = null;
            fatherId = ind.getFatherId();
            motherId = ind.getMotherId();

            if(!fatherId.equals("0") && !motherId.equals("0")){ // Existen padre y Madre (hay ID)
                father = this.getIndividual(fatherId);
                mother = this.getIndividual(motherId);
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
            }else if(!fatherId.equals("0") && motherId.equals("0")){ // No existe la madre
                father = this.getIndividual(fatherId);
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
            }else if(fatherId.equals("0") && !motherId.equals("0")){ // No existe el padre
                father = null;
                mother = this.getIndividual(motherId);
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

    private Set<Individual> getFamily(String familyId) {
        return families.get(familyId);
    }

    private void parseHeader(String lineHeader) {

        String header = lineHeader.substring(1, lineHeader.length());
        String[] allFields = header.split("\t");

        allFields = Arrays.copyOfRange(allFields, 6, allFields.length);
        for (int i = 0; i< allFields.length; i++){
            this.fields.put(allFields[i], i);
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
