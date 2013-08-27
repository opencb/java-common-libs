package org.opencb.commons.bioformats.commons.core.feature.io;

import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.opencb.commons.bioformats.commons.core.feature.Ped;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class PedReader extends AbstractFormatReader<Ped>{

    private BufferedReader reader;



    public PedReader(String filename) throws IOException {
        this(new File(filename));
    }

    public PedReader(File file) throws IOException {
        super(file);
        this.file = file;
        reader = new BufferedReader(new FileReader(file));
    }

    @Override
    public int size() throws IOException, FileFormatException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Ped read() throws FileFormatException {

        String line;
        Ped p = null;

        try {
            while ((line = reader.readLine()) != null && (line.trim().equals("") || line.startsWith("#"))) {
                ;
            }

        if(line != null){
            String[] fields = line.split("\t");
                 p = new Ped(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]) ;

        }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return p;
    }

    @Override
    public Ped read(String regexFilter) throws FileFormatException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Ped> read(int size) throws FileFormatException {

        List<Ped> list = new ArrayList<Ped>(size);
        Ped p = null;
        int i = 0;
        while((i < size) && (p = this.read()) != null){
            list.add(p);
            i++;
        }
        return list;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Ped> readAll() throws FileFormatException, IOException {
        List<Ped> list = new ArrayList<Ped>(10);
        String line;
        String[] fields;
        Ped p;
        reader = new BufferedReader(new FileReader(this.file));
        while((line = reader.readLine()) != null){
            if(!line.startsWith("#")){
                fields = line.split("\t");
                if(fields.length >= 6){
                    p = new Ped(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]) ;
                    list.add(p);
                }
            }
        }
        reader.close();


        return list;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Ped> readAll(String pattern) throws FileFormatException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws IOException {
        reader.close();

    }
}
