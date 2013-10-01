package org.opencb.variant.lib.io.ped.readers;

import org.opencb.variant.lib.core.formats.Individual;
import org.opencb.variant.lib.core.formats.Pedigree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/31/13
 * Time: 8:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class PedFileDataReader implements PedDataReader {

    private String filename;
    private Pedigree ped;
    private BufferedReader reader;

    public PedFileDataReader(String filename) {
        this.filename = filename;

        ped = new Pedigree();
    }

    @Override
    public boolean open() {
        ped = new Pedigree();

        try {
            reader = new BufferedReader(new FileReader(this.filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean close() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean pre() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean post() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Pedigree read() {

        String line;
        Individual ind, father, mother;
        String[] fields;
        String sampleId, familyId, fatherId, motherId, sex, phenotype;
        Set<Individual> family;
        Queue<Individual> queue = new LinkedList<>();
        String[] auxFields = null;

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    this.parseHeader(line);
                } else {
                    fields = line.split("\t");
                    familyId = fields[0];
                    sampleId = fields[1];
                    fatherId = fields[2];
                    motherId = fields[3];
                    sex = fields[4];
                    phenotype = fields[5];

                    if (fields.length > 6) {
                        auxFields = Arrays.copyOfRange(fields, 6, fields.length);
                    }

                    family = ped.getFamily(familyId);
                    if (family == null) {
                        family = new TreeSet<>();
                        ped.addFamily(familyId, family);
                    }

                    ind = new Individual(sampleId, familyId, null, null, sex, phenotype, auxFields);
                    ind.setFatherId(fatherId);
                    ind.setMotherId(motherId);
                    ped.addIndividual(ind);
                    family.add(ind);


                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Individual> entry : ped.getIndividuals().entrySet()) {

            ind = entry.getValue();
            father = ped.getIndividual(ind.getFatherId());
            mother = ped.getIndividual(ind.getMotherId());

            ind.setFather(father);
            ind.setMother(mother);

            if (mother != null) {
                mother.addChild(ind);
            }
            if (father != null) {
                father.addChild(ind);

            }


        }


        return ped;
    }

    @Override
    public List<Pedigree> read(int batchSize) {
        return null;
    }

    private void parseHeader(String lineHeader) {

        String header = lineHeader.substring(1, lineHeader.length());
        String[] allFields = header.split("\t");

        allFields = Arrays.copyOfRange(allFields, 6, allFields.length);
        for (int i = 0; i < allFields.length; i++) {
            ped.getFields().put(allFields[i], i);
        }
    }
}
