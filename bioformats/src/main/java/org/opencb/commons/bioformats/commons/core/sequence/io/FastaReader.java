package org.opencb.commons.bioformats.commons.core.sequence.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bioinfo.commons.io.TextFileReader;
import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.bioinfo.formats.core.sequence.Fasta;
import org.bioinfo.formats.exception.FileFormatException;

public class FastaReader extends AbstractFormatReader<Fasta> {

	private TextFileReader fileReader;

	private static final String SEQ_ID_CHAR = ">";

	private String lastLineRead = null;

	private boolean endOfFileReached = false;

	public FastaReader(String fileName) throws IOException{
		this(new File(fileName));
	}

	public FastaReader(File file) throws IOException {
		super(file);
		this.fileReader = new TextFileReader(file.getAbsolutePath());
	}


	@Override
	public Fasta read() throws FileFormatException {
		Fasta fasta = null;	

		if (!this.endOfFileReached){
			try {
				// Read Id and Desc
				String idLine = this.readIdLine();
				String[] fields = idLine.split("\\s");
				String id = fields[0].substring(1);
				String desc = "";
				if(fields.length > 1) {
					desc = fields[1];					
				}

				// Read Sequence
				StringBuilder sequenceBuilder = this.readSequenceLines();

				// Build Fasta object
				fasta = new Fasta(id, desc.trim(), sequenceBuilder.toString().trim());

			}catch (IOException ex){
				throw new FileFormatException(ex);
			}
		}

		return fasta;
	}

	@Override
	public int size() throws IOException {
		int size = 0;
		String line;
		while ((line = this.fileReader.readLine()) != null){
			if (line.startsWith(FastaReader.SEQ_ID_CHAR)){
				size ++;
			}
		}
		return size;
	}


	@Override
	public void close() throws IOException {
		this.fileReader.close();

	}

	@Override
	public Fasta read(String regexFilter) throws FileFormatException {
		Fasta seq = this.read();
		boolean found = false;
		while (!found && seq != null){
			if (seq.getId().matches(regexFilter)){
				found = true;
			} else {
				seq = this.read();
			}
		}
		return seq;
	}

	@Override
	public List<Fasta> readAll() throws FileFormatException {
		List<Fasta> fastaList = new ArrayList<Fasta>();

		Fasta fasta;
		while ((fasta = this.read()) != null){
			fastaList.add(fasta);
		}

		return fastaList;		
	}

	@Override
	public List<Fasta> readAll(String regexFilter) throws FileFormatException {
		List<Fasta> fastaList = new ArrayList<Fasta>();

		Fasta fasta;
		while ((fasta = this.read(regexFilter)) != null){
			fastaList.add(fasta);
		}

		return fastaList;
	}

	private String readIdLine() throws  FileFormatException,IOException {
		String idLine;
		// If no previous sequences have been read, read the first(s) line(s)		
		if (this.lastLineRead == null){
			// TODO: Comprobar si hay lineas de basura antes de la primera secuencia,
			//		 en lugar de lanzar una excepcion directamente
			idLine = this.fileReader.readLine();
			if (!idLine.startsWith(FastaReader.SEQ_ID_CHAR)){
				throw new FileFormatException("Incorrect ID Line: "+idLine);				
			}
		} else {
			idLine = this.lastLineRead;
		}	
		return idLine;
	}

	private StringBuilder readSequenceLines() throws FileFormatException, IOException {
		// read the sequence chars
		StringBuilder sequenceBuilder = new StringBuilder();
		String line = this.fileReader.readLine();
		while (line != null && !line.startsWith(FastaReader.SEQ_ID_CHAR)){
			// check the sequence format and throws a FileFormatException if it's wrong 
			checkSequence(line);
			sequenceBuilder.append(line);
			line = this.fileReader.readLine();
		}

		// Check if we have reached a new sequence or the end of file 
		if (line !=null){
			this.lastLineRead = line;
		} else {
			this.endOfFileReached = true;
		}

		return sequenceBuilder;
	}

	private void checkSequence(String sequence) throws FileFormatException {
		// Por ahora no hacemos comprobacion alguna y nos creemos que la secuencia viene bien
	}

	@Override
	public List<Fasta> read(int size) throws FileFormatException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
