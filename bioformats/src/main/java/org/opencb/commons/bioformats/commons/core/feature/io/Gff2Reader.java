package org.opencb.commons.bioformats.commons.core.feature.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bioinfo.commons.io.BeanReader;
import org.bioinfo.commons.io.utils.IOUtils;
import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.opencb.commons.bioformats.commons.core.feature.Gff2;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

public class Gff2Reader extends AbstractFormatReader<Gff2> {

	private BeanReader<Gff2> beanReader;

	public Gff2Reader(String filename) throws IOException, SecurityException, NoSuchMethodException {
		this(new File(filename));
	}

	public Gff2Reader(File file) throws IOException, SecurityException, NoSuchMethodException {
		super(file);
		beanReader = new BeanReader<Gff2>(file, Gff2.class);
	}

	@Override
	public Gff2 read() throws FileFormatException {
		try {
			return beanReader.read();
		} catch (Exception e) {
			throw new FileFormatException(e);
		} 
	}

	@Override
	public Gff2 read(String pattern) throws FileFormatException {
		try {
			return beanReader.read(pattern);
		} catch (Exception e) {
			throw new FileFormatException(e);
		} 
	}

	@Override
	public List<Gff2> read(int numberLines) throws FileFormatException {
		try {
			return beanReader.read(numberLines);
		} catch (Exception e) {
			throw new FileFormatException(e);
		} 
	}

	@Override
	public List<Gff2> readAll() throws FileFormatException {
		try {
			return beanReader.readAll();
		} catch (Exception e) {
			throw new FileFormatException(e);
		}
	}

	@Override
	public List<Gff2> readAll(String pattern) throws FileFormatException {
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
