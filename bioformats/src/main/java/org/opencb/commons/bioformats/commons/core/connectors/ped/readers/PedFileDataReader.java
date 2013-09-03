package org.opencb.commons.bioformats.commons.core.connectors.ped.readers;

import org.opencb.commons.bioformats.commons.core.feature.Individual;
import org.opencb.commons.bioformats.commons.core.feature.Pedigree;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

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


                    if (fatherId.equals("0") && motherId.equals("0")) {
                        ind = new Individual(sampleId, familyId, null, null, sex, phenotype, auxFields);
                        ped.addIndividual(ind);
                        family.add(ind);
                    } else {
                        ind = new Individual(sampleId, familyId, null, null, sex, phenotype, auxFields);
                        ind.setFatherId(fatherId);
                        ind.setMotherId(motherId);
                        queue.offer(ind);
                    }


                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        while (!queue.isEmpty()) {
            ind = queue.poll();
            fatherId = ind.getFatherId();
            motherId = ind.getMotherId();

            if (!fatherId.equals("0") && !motherId.equals("0")) { // Existen padre y Madre (hay ID)
                father = ped.getIndividual(fatherId);
                mother = ped.getIndividual(motherId);
                if (father == null || mother == null) {  // Existen pero aún no se han metido en la HASH
                    queue.offer(ind);
                } else { // Tenemos el padre y la madre y están en la HASH
                    ind.setFather(father);
                    ind.setMother(mother);
                    ped.addIndividual(ind);

                    // Añadimos "ind" como hijo a los padres
                    father.addChild(ind);
                    mother.addChild(ind);

                    ped.addIndividualToFamily(ind.getFamily(), ind);

                }
            } else if (!fatherId.equals("0") && motherId.equals("0")) { // No existe la madre
                father = ped.getIndividual(fatherId);
                mother = null;
                if (father == null) { // Existe el padre pero aún no se ha metido en la hash
                    queue.offer(ind);
                } else {
                    ind.setFather(father);
                    ind.setMother(mother);
                    ped.addIndividual(ind);

                    // Añadimos "ind" como hijo al padre
                    father.addChild(ind);
                    ped.addIndividualToFamily(ind.getFamily(), ind);

                }
            } else if (fatherId.equals("0") && !motherId.equals("0")) { // No existe el padre
                father = null;
                mother = ped.getIndividual(motherId);
                if (mother == null) { // Existe la madre pero aún no se ha metido en la hash
                    queue.offer(ind);
                } else {
                    ind.setFather(father);
                    ind.setMother(mother);
                    ped.addIndividual(ind);

                    // Añadimos "ind" como hijo a la madre
                    mother.addChild(ind);

                    ped.addIndividualToFamily(ind.getFamily(), ind);

                }
            }
        }
        return ped;
    }

    @Override
    public List<Pedigree> read(int batchSize) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
