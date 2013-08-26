package org.opencb.commons.bioformats.commons.core.feature.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bioinfo.commons.io.BeanReader;
import org.bioinfo.commons.io.utils.IOUtils;
import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.bioinfo.formats.core.feature.Bed;
import org.bioinfo.formats.exception.FileFormatException;

public class BedReader extends AbstractFormatReader<Bed> {

	private BeanReader<Bed> beanReader;

	public BedReader(String filename) throws IOException, SecurityException, NoSuchMethodException {
		this(new File(filename));	
	}

	public BedReader(File file) throws IOException, SecurityException, NoSuchMethodException {
		super(file);
		beanReader = new BeanReader<Bed>(file, Bed.class);
	}

	@Override
	public Bed read() throws FileFormatException {
		try {
			return beanReader.read();
		} catch (Exception e) {
			throw new FileFormatException(e);
		}
	}

	@Override
	public Bed read(String pattern) throws FileFormatException {
		try {
			return beanReader.read(pattern);
		} catch (Exception e) {
			throw new FileFormatException(e);
		}
	}
	
	@Override
	public List<Bed> read(int number) throws FileFormatException {
		try {
			return beanReader.read(number);
		} catch (Exception e) {
			throw new FileFormatException(e);
		}
	}

	@Override
	public List<Bed> readAll() throws FileFormatException {
		try {
			return beanReader.readAll();
		} catch (Exception e) {
			throw new FileFormatException(e);
		}
	}

	@Override
	public List<Bed> readAll(String pattern) throws FileFormatException {
		try {
			return beanReader.readAll(pattern);
		} catch (Exception e) {
			throw new FileFormatException(e);
		}
	}

	@Override
	public int size() throws IOException {
		return IOUtils.countLines(file);
	}

	@Override
	public void close() throws IOException {
		beanReader.close();
	}

}
