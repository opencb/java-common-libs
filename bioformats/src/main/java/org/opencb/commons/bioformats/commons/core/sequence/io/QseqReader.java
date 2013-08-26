package org.opencb.commons.bioformats.commons.core.sequence.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bioinfo.commons.io.TextFileReader;
import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.opencb.commons.bioformats.commons.core.sequence.Qseq;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

public class QseqReader extends AbstractFormatReader<Qseq> {
	
	private TextFileReader fileReader;	

	public QseqReader(File file) throws IOException {
		super(file);	
		this.fileReader = new TextFileReader(file.getAbsolutePath());				
	}
	
	public QseqReader(String fileName) throws IOException {
		this(new File(fileName));

	}

	@Override
	public void close() throws IOException {
		fileReader.close();
	}

	@Override
	public Qseq read() throws FileFormatException {
		Qseq qseq = null;
		
		try {
			String line = fileReader.readLine();
			if (line != null) {
				// Parse all line columns
				String[] fields = line.split("\t");
				if (fields.length != 11) {
					throw new FileFormatException ("Incorrect Qseq line: "+ line);
				}
				
				String machineId = fields[0];
				int run = Integer.parseInt(fields[1]);
				int lane = Integer.parseInt(fields[2]);
				int tile = Integer.parseInt(fields[3]);
				int xCoord = Integer.parseInt(fields[4]);
				int yCoord = Integer.parseInt(fields[5]);
				int index = Integer.parseInt(fields[6]);
				int readId = Integer.parseInt(fields[7]);
				String seq = fields[8];
				String quality = fields[9];
				int filteringPassed = Integer.parseInt(fields[10]);
				
				// Build Qseq object
				qseq = new Qseq(machineId, run, lane, tile, xCoord, yCoord, index, readId, seq, quality, filteringPassed);
			}
		} catch (IOException ex) {
			throw new FileFormatException(ex);
		}
		
		return qseq;
	}

	@Override
	public Qseq read(String regexFilter) throws FileFormatException {
		Qseq qseq = this.read();
		boolean found = false;
		while (!found && qseq != null){
			if (qseq.getMachineId().matches(regexFilter)){
				found = true;
			} else {
				qseq = this.read();
			}
		}
		return qseq;
	}

	@Override
	public List<Qseq> readAll() throws FileFormatException {
		List<Qseq> fastaList = new ArrayList<Qseq>();

		Qseq qseq;
		while ((qseq = this.read()) != null){
			fastaList.add(qseq);
		}

		return fastaList;		
	}

	@Override
	public List<Qseq> readAll(String regexFilter) throws FileFormatException {
		List<Qseq> fastaList = new ArrayList<Qseq>();

		Qseq qseq;
		while ((qseq = this.read(regexFilter)) != null){
			fastaList.add(qseq);
		}

		return fastaList;	
	}

	@Override
	public int size() throws IOException, FileFormatException {
		int size = 0;
		while (this.read() != null){
			size ++;
		}
		return size;
	}

	@Override
	public List<Qseq> read(int size) throws FileFormatException {
		// TODO Auto-generated method stub
		return null;
	}

}
