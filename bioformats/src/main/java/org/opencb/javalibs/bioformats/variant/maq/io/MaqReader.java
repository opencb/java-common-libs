package org.opencb.javalibs.bioformats.variant.maq.io;

import org.bioinfo.commons.io.BeanReader;
import org.bioinfo.commons.io.utils.IOUtils;
import org.opencb.javalibs.bioformats.commons.exception.FileFormatException;
import org.opencb.javalibs.bioformats.commons.io.AbstractFormatReader;
import org.opencb.javalibs.bioformats.variant.maq.Maq;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MaqReader extends AbstractFormatReader<Maq> {

    private BeanReader<Maq> beanReader;

    public MaqReader(String filename) throws IOException, SecurityException, NoSuchMethodException {
        this(new File(filename));
    }

    public MaqReader(File file) throws IOException, SecurityException, NoSuchMethodException {
        super(file);
        beanReader = new BeanReader<Maq>(file, Maq.class);
    }

    @Override
    public Maq read() throws FileFormatException {
        try {
            return beanReader.read();
        } catch (Exception e) {
            throw new FileFormatException(e);
        }
    }

    @Override
    public Maq read(String pattern) throws FileFormatException {
        try {
            return beanReader.read(pattern);
        } catch (Exception e) {
            throw new FileFormatException(e);
        }
    }

    @Override
    public List<Maq> read(int numberLines) throws FileFormatException {
        try {
            return beanReader.read(numberLines);
        } catch (Exception e) {
            throw new FileFormatException(e);
        }
    }

    @Override
    public List<Maq> readAll() throws FileFormatException {
        try {
            return beanReader.readAll();
        } catch (Exception e) {
            throw new FileFormatException(e);
        }
    }

    @Override
    public List<Maq> readAll(String pattern) throws FileFormatException {
        try {
            return beanReader.readAll(pattern);
        } catch (Exception e) {
            throw new FileFormatException(e);
        }
    }

    @Override
    public int size() throws IOException, FileFormatException {
        return IOUtils.countLines(file);
    }

    @Override
    public void close() throws IOException {
        beanReader.close();
    }

}
