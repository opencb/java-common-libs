package org.opencb.javalibs.bioformats.commons.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.log.Logger;
import org.opencb.javalibs.bioformats.commons.exception.FileFormatException;

public abstract class AbstractFormatReader<T> {

	protected File file;
	protected Logger logger;
	
	protected AbstractFormatReader(){
		file = null;
		logger = new Logger();
		logger.setLevel(Logger.DEBUG_LEVEL);
	}
	
	protected AbstractFormatReader(File f) throws IOException {
		FileUtils.checkFile(f);
		this.file = f;
		logger = new Logger();
		logger.setLevel(Logger.DEBUG_LEVEL);
	}

	public abstract int size() throws IOException, FileFormatException;
	
	public abstract T read() throws FileFormatException;
	
	public abstract T read(String regexFilter) throws FileFormatException;
	
	public abstract List<T> read(int size) throws FileFormatException;
	
	public abstract List<T> readAll() throws FileFormatException, IOException;
	
	public abstract List<T> readAll(String pattern) throws FileFormatException;
	
	public abstract void close() throws IOException;
	
}
