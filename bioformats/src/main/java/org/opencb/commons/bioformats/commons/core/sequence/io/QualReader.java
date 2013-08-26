package org.opencb.commons.bioformats.commons.core.sequence.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bioinfo.commons.io.TextFileReader;
import org.bioinfo.commons.utils.ArrayUtils;
import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.bioinfo.formats.core.sequence.Qual;
import org.bioinfo.formats.exception.FileFormatException;

public class QualReader extends AbstractFormatReader<Qual> {
	
	private TextFileReader fileReader;

	private String lastLineRead = null;

	private boolean endOfFileReached = false;	

	public QualReader(String fileName) throws IOException{
		this(new File(fileName));
	}

	public QualReader(File file) throws IOException {
		super(file);
		this.fileReader = new TextFileReader(file.getAbsolutePath());
	}	
	
	@Override
	public void close() throws IOException {
		fileReader.close();
	}

	@Override
	public Qual read() throws FileFormatException {
		Qual qual = null;	

		if (!this.endOfFileReached){
			try {
				// Read Id and Desc
				String idLine = this.readIdLine();
				String id = idLine.split("\\s")[0].substring(1);
				String desc = idLine.substring(id.length()+1);

				// Read Sequence
				int[] qualities = this.readQualityLines();

				// Build Fasta object
				qual = new Qual(id, desc.trim(), qualities);

			}catch (IOException ex){
				throw new FileFormatException(ex);
			}
		}

		return qual;
	}

	@Override
	public Qual read(String regexFilter) throws FileFormatException {
		Qual qual = this.read();
		boolean found = false;
		while (!found && qual != null){
			if (qual.getId().matches(regexFilter)){
				found = true;
			} else {
				qual = this.read();
			}
		}
		return qual;
	}

	@Override
	public List<Qual> readAll() throws FileFormatException {
		List<Qual> qualList = new ArrayList<Qual>();

		Qual qual;
		while ((qual = this.read()) != null){
			qualList.add(qual);
		}

		return qualList;		
	}

	@Override
	public List<Qual> readAll(String regexFilter) throws FileFormatException {
		List<Qual> qualList = new ArrayList<Qual>();

		Qual qual;
		while ((qual = this.read(regexFilter)) != null){
			qualList.add(qual);
		}

		return qualList;	
	}

	@Override
	public int size() throws IOException, FileFormatException {
		int size = 0;
		String line;
		while ((line = this.fileReader.readLine()) != null){
			if (line.startsWith(Qual.SEQ_ID_CHAR)){
				size ++;
			}
		}
		return size;
	}
	
	private String readIdLine() throws  FileFormatException,IOException {
		String idLine;
		// If no previous sequences have been read, read the first(s) line(s)		
		if (this.lastLineRead == null){
			// TODO: Comprobar si hay lineas de basura antes de la primera secuencia,
			//		 en lugar de lanzar una excepcion directamente
			idLine = this.fileReader.readLine();
			if (!idLine.startsWith(Qual.SEQ_ID_CHAR)){
				throw new FileFormatException("Incorrect ID Line: "+idLine);				
			}
		} else {
			idLine = this.lastLineRead;
		}	
		return idLine;
	}
	
	private int[] readQualityLines() throws FileFormatException, IOException {
		int[] qualities;
		StringBuilder qualStringBuilder = new StringBuilder();
		String line = this.fileReader.readLine();
		while (line != null && !line.startsWith(Qual.SEQ_ID_CHAR)){
			qualStringBuilder.append(line);
			line = this.fileReader.readLine();
		}
		
		// convert the stringBuilder into a int array
		qualities = ArrayUtils.toIntArray(qualStringBuilder.toString().replaceAll("255", "0").split("\\s"), 0);
		
		// Check if we have reached a new sequence or the end of file 
		if (line !=null){
			this.lastLineRead = line;
		} else {
			this.endOfFileReached = true;
		}

		return qualities;
	}

	@Override
	public List<Qual> read(int size) throws FileFormatException {
		// TODO Auto-generated method stub
		return null;
	}	

}
