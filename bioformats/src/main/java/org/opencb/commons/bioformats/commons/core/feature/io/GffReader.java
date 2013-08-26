package org.opencb.commons.bioformats.commons.core.feature.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bioinfo.commons.io.BeanReader;
import org.bioinfo.commons.io.utils.IOUtils;
import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.bioinfo.formats.core.feature.Gff;
import org.bioinfo.formats.exception.FileFormatException;

public class GffReader extends AbstractFormatReader<Gff> {

	private BeanReader<Gff> beanReader;
	
	public GffReader(String filename) throws IOException, SecurityException, NoSuchMethodException {
		this(new File(filename));
	}
	
	public GffReader(File file) throws IOException, SecurityException, NoSuchMethodException {
		super(file);
		beanReader = new BeanReader<Gff>(file, Gff.class);
	}
	
	@Override
	public Gff read() throws FileFormatException {
		try {
			return beanReader.read();
		} catch (Exception e) {
			throw new FileFormatException(e);
		} 
	}

	@Override
	public Gff read(String pattern) throws FileFormatException {
		try {
			return beanReader.read(pattern);
		} catch (Exception e) {
			throw new FileFormatException(e);
		} 
	}

	@Override
	public List<Gff> read(int numberLines) throws FileFormatException {
		try {
			return beanReader.read(numberLines);
		} catch (Exception e) {
			throw new FileFormatException(e);
		} 
	}
	
	@Override
	public List<Gff> readAll() throws FileFormatException {
		try {
			return beanReader.readAll();
		} catch (Exception e) {
			throw new FileFormatException(e);
		}
	}
	
	@Override
	public List<Gff> readAll(String pattern) throws FileFormatException {
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
