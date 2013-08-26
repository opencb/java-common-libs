package org.opencb.commons.bioformats.commons.core.variant.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bioinfo.commons.io.BeanReader;
import org.bioinfo.commons.io.utils.IOUtils;
import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.bioinfo.formats.core.variant.SoapSnp;
import org.bioinfo.formats.exception.FileFormatException;

public class SoapSnpReader extends AbstractFormatReader<SoapSnp> {
	
	private BeanReader<SoapSnp> beanReader;
	
	public SoapSnpReader(String filename) throws IOException, SecurityException, NoSuchMethodException {
		this(new File(filename));
	}
	
	public SoapSnpReader(File file) throws IOException, SecurityException, NoSuchMethodException {
		super(file);
		beanReader = new BeanReader<SoapSnp>(file, SoapSnp.class);
	}
	
	@Override
	public SoapSnp read() throws FileFormatException {
		try {
			return beanReader.read();
		} catch (Exception e) {
			throw new FileFormatException(e);
		} 
	}

	@Override
	public SoapSnp read(String pattern) throws FileFormatException {
		try {
			return beanReader.read(pattern);
		} catch (Exception e) {
			throw new FileFormatException(e);
		} 
	}

	@Override
	public List<SoapSnp> read(int numberLines) throws FileFormatException {
		try {
			return beanReader.read(numberLines);
		} catch (Exception e) {
			throw new FileFormatException(e);
		} 
	}

	@Override
	public List<SoapSnp> readAll() throws FileFormatException {
		try {
			return beanReader.readAll();
		} catch (Exception e) {
			throw new FileFormatException(e);
		}
	}

	@Override
	public List<SoapSnp> readAll(String pattern) throws FileFormatException {
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
