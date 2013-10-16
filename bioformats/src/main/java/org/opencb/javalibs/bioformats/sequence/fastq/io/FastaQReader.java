package org.opencb.javalibs.bioformats.sequence.fastq.io;

import org.bioinfo.commons.io.TextFileReader;
import org.opencb.javalibs.bioformats.commons.exception.FileFormatException;
import org.opencb.javalibs.bioformats.commons.io.AbstractFormatReader;
import org.opencb.javalibs.bioformats.sequence.fastq.FastQ;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FastaQReader extends AbstractFormatReader<FastQ> {

    private TextFileReader fileReader;

    private static final String SEQ_ID_CHAR = "@";

    private static final String QUALITY_ID_CHAR = "+";

    private int encoding;

    public FastaQReader(InputStream input, int encoding) throws IOException {
        this.fileReader = new TextFileReader(input);
        this.encoding = encoding;
    }

    public FastaQReader(String fileName, int encoding) throws IOException {
        this(new File(fileName), encoding);
    }

    public FastaQReader(String fileName) throws IOException {
        this(new File(fileName));
    }

    public FastaQReader(File file) throws IOException {
        this(file, FastQ.SANGER_ENCODING);
    }

    public FastaQReader(File file, int encoding) throws IOException {
        super(file);
        this.fileReader = new TextFileReader(file.getAbsolutePath());
        this.encoding = encoding;
    }

    @Override
    public void close() throws IOException {
        this.fileReader.close();
    }

    @Override
    public List<FastQ> readAll() throws FileFormatException {
        List<FastQ> fastaList = new ArrayList<FastQ>();

        FastQ fasta;
        while ((fasta = this.read()) != null) {
            fastaList.add(fasta);
        }

        return fastaList;
    }

    @Override
    public List<FastQ> readAll(String regexFilter) throws FileFormatException {
        List<FastQ> fastaList = new ArrayList<FastQ>();

        FastQ fasta;
        while ((fasta = this.read(regexFilter)) != null) {
            fastaList.add(fasta);
        }

        return fastaList;
    }

    public FastQ read() throws FileFormatException {
        FastQ fasta = null;

        try {
            // Read Id Line. If it's null, the end of file has been reached
            String idLine = this.readIdLine();
            if (idLine != null) {
                // Obtain Id and Desc from Id Line
                String id = idLine.split("\\s")[0].substring(1);
                String desc = idLine.substring(id.length() + 1);

                // Read Sequence
                StringBuilder sequenceBuilder = new StringBuilder();
                int numSequenceLines = this.readSequenceLines(sequenceBuilder);
                String sequence = sequenceBuilder.toString().trim();

                // Read Quality
                StringBuilder qualityBuilder = this.readQualityLines(numSequenceLines);
                String quality = qualityBuilder.toString().trim();

                // Check that sequence and quality sizes are equal
                this.checkQualitySize(id, sequence, quality);

                // Build Fasta object
                fasta = new FastQ(id, desc.trim(), sequence, quality, this.encoding);
            }
        } catch (IOException ex) {
            throw new FileFormatException(ex);
        }

        return fasta;
    }

    public int size() throws IOException, FileFormatException {
        int size = 0;
        while (this.read() != null) {
            size++;
        }
        return size;
    }

    @Override
    public FastQ read(String regexFilter) throws FileFormatException {
        FastQ seq = this.read();
        boolean found = false;
        while (!found && seq != null) {
            if (seq.getId().matches(regexFilter)) {
                found = true;
            } else {
                seq = this.read();
            }
        }
        return seq;
    }

    private String readIdLine() throws FileFormatException, IOException {
        String idLine;

        // TODO: Comprobar si hay lineas de basura antes de la primera secuencia,
        //		 en lugar de lanzar una excepcion directamente
        idLine = this.fileReader.readLine();
        if ((idLine != null) && !idLine.startsWith(FastaQReader.SEQ_ID_CHAR)) {
            throw new FileFormatException("Incorrect ID Line: " + idLine);
        }

        return idLine;
    }

    private int readSequenceLines(StringBuilder sequenceBuilder) throws FileFormatException, IOException {
        int numSequenceLines = 0;
        // read the sequence string
        String line = this.fileReader.readLine();
        while (line != null && !line.startsWith(FastaQReader.QUALITY_ID_CHAR)) {
            // check the sequence format and throws a FileFormatException if it's wrong
            checkSequence(line);
            sequenceBuilder.append(line);
            numSequenceLines++;
            line = this.fileReader.readLine();
        }

        return numSequenceLines;
    }


    private StringBuilder readQualityLines(int numSequenceLines) throws IOException, FileFormatException {
        StringBuilder qualityBuilder = new StringBuilder();

        String line;
        int numLinesRead = 1;
        while ((numLinesRead <= numSequenceLines) && (line = this.fileReader.readLine()) != null) {
            // check the sequence format and throws a FileFormatException if it's wrong
            checkQuality(line);
            qualityBuilder.append(line);
            numLinesRead++;
        }

        return qualityBuilder;
    }

    private void checkSequence(String sequence) throws FileFormatException {
        // TODO: Por ahora no hacemos comprobacion alguna y nos creemos que la secuencia viene bien
    }

    private void checkQuality(String sequence) throws FileFormatException {
        // TODO: Por ahora no hacemos comprobacion alguna y nos creemos que la secuencia viene bien
    }

    /**
     * Check that the sequence and quality strings have the same length
     *
     * @param id       - FastQ id
     * @param sequence - FastQ sequence string
     * @param quality  - FastQ quality string
     * @throws FileFormatException - If the sequence and quality strings have different lengths
     */
    private void checkQualitySize(String id, String sequence, String quality) throws FileFormatException {
        if (sequence.length() != quality.length()) {
            throw new FileFormatException("Quality and Sequence lenghts are different in Fasta " + id);
        }
    }

    @Override
    public List<FastQ> read(int size) throws FileFormatException {
        // TODO Auto-generated method stub
        return null;
    }

}
