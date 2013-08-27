package org.opencb.commons.bioformats.commons.core.variant.io;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.opencb.commons.bioformats.commons.core.variant.Vcf4;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfFilter;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfFormat;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfInfo;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Vcf4Reader extends AbstractFormatReader<VcfRecord> {

    private static final int DEFAULT_NUMBER_RECORDS = 40000;
    private Vcf4 vcf4;
    private BufferedReader bufferedReader;
    private List<Predicate<VcfRecord>> vcfFilters;
    private Predicate<VcfRecord> andVcfFilters;


    public Vcf4Reader(String filename) throws IOException, FileFormatException {
        this(new File(filename));
    }

    public Vcf4Reader(File file) throws IOException, FileFormatException {
        super(file);
        this.file = file;

        init();

    }

    public Vcf4Reader(File file, List<Predicate<VcfRecord>> list_filters) throws IOException, FileFormatException {
        this(file);
        this.vcfFilters = list_filters;
        this.andVcfFilters = Predicates.and(this.vcfFilters);
    }

    public void init() throws FileFormatException, IOException {
        vcf4 = new Vcf4();
        // vcfFilters = new ArrayList<VcfGenericFilter>();

        FileUtils.checkFile(file);
        bufferedReader = new BufferedReader(new FileReader(file));

        // read meta data from VCF4 file
        processMetaInformation();
    }

    private void processMetaInformation() throws IOException, FileFormatException {
        VcfInfo vcfInfo;
        VcfFilter vcfFilter;
        VcfFormat vcfFormat;
        List<String> headerLine;
        String line = "";
        String[] fields;
        BufferedReader localBufferedReader = new BufferedReader(new FileReader(file));
        while ((line = localBufferedReader.readLine()) != null && line.startsWith("#")) {
//			logger.debug("line: "+line);
            if (line.startsWith("##fileformat")) {
                if (line.split("=").length > 1) {
//					this.fileFormat = line.split("=")[1].trim();
                    vcf4.setFileFormat(line.split("=")[1].trim());
                } else {
                    throw new FileFormatException("");
                }
            } else {
                if (line.startsWith("##INFO")) {
//					System.out.println(line);
//					System.out.println(new VcfInfo(line).toString()+"\n");
                    vcfInfo = new VcfInfo(line);
                    vcf4.getInfo().put(vcfInfo.getId(), vcfInfo);
                } else {
                    if (line.startsWith("##FILTER")) {
//						System.out.println(line);
//						System.out.println(new VcfGenericFilter(line).toString()+"\n");
                        vcfFilter = new VcfFilter(line);
                        vcf4.getFilter().put(vcfFilter.getId(), vcfFilter);
                    } else {
                        if (line.startsWith("##FORMAT")) {
//							System.out.println(line);
//							System.out.println(new VcfFormat(line).toString()+"\n");
                            vcfFormat = new VcfFormat(line);
                            vcf4.getFormat().put(vcfFormat.getId(), vcfFormat);
                        } else {
                            if (line.startsWith("#CHROM")) {
                                headerLine = StringUtils.toList(line.replace("#", ""), "\t");
//								System.out.println(headerLine.toString());
                                vcf4.setHeaderLine(headerLine);
                            } else {
                                fields = line.replace("#", "").split("=", 2);
                                vcf4.getMetaInformation().put(fields[0], fields[1]);
//								System.out.println(metaInformation.toString());
//								logger.warn("Warning in 'processMetaInformation': Execution cannot reach this code, line: "+line);
                            }
                        }
                    }
                }
            }
        }
        localBufferedReader.close();
    }

    public Vcf4 parse() throws FileFormatException, IOException {
//		init();
        vcf4.setRecords(readAll());
        close();
        return vcf4;
    }

    public void addFilter(Predicate<VcfRecord> vcfFilter) {
        vcfFilters.add(vcfFilter);
        this.andVcfFilters = Predicates.and(this.vcfFilters);
    }

    @Override
    public VcfRecord read() throws FileFormatException {
        String line;
        try {
//			line = bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null && (line.trim().equals("") || line.startsWith("#"))) {
                ;
            }
            if (line != null) {
//				logger.debug("line: "+line);
                String[] fields = line.split("\t");
                VcfRecord vcfRecord = null;
                if (fields.length == 8) {
                    vcfRecord = new VcfRecord(fields[0], Integer.parseInt(fields[1]), fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);
                } else {
                    if (fields.length > 8) {
                        vcfRecord = new VcfRecord(fields);
                    }
                }
                return vcfRecord;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public VcfRecord read(String regexFilter) throws FileFormatException {
//		new VcfRecord(chromosome, position, id, reference, alternate, quality, filter, info, format, samples)
        return null;
    }

    @Override
    public List<VcfRecord> read(int size) throws FileFormatException {

        List<VcfRecord> list_records = new ArrayList<VcfRecord>(size);
        VcfRecord vcf_record;
        int i = 0;

        while ((i < size) && (vcf_record = this.read()) != null) {

            if (vcfFilters != null && vcfFilters.size() > 0) {
                if (andVcfFilters.apply(vcf_record)) {
                    list_records.add(vcf_record);
                    i++;
                }
            }

        }
        return list_records;
    }

    @Override
    public List<VcfRecord> readAll() throws FileFormatException, IOException {
        List<VcfRecord> records = new ArrayList<VcfRecord>(DEFAULT_NUMBER_RECORDS);
        String line;
        String[] fields;
        VcfRecord vcfRecord = null;
        boolean passFilter;
        bufferedReader = new BufferedReader(new FileReader(file));
        while ((line = bufferedReader.readLine()) != null) {
            if (!line.startsWith("#")) {
                fields = line.split("\t");
                if (fields.length == 8) {
                    vcfRecord = new VcfRecord(fields[0], Integer.parseInt(fields[1]), fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);
                } else {
                    if (fields.length > 8) {
                        vcfRecord = new VcfRecord(fields);
                    }
                }
                if (vcfFilters != null && vcfFilters.size() > 0) {
                    if (andVcfFilters.apply(vcfRecord)) {
                        records.add(vcfRecord);
                    }


                } else {
                    records.add(vcfRecord);
                }
            }
        }
        bufferedReader.close();
        return records;
    }

    @Override
    public List<VcfRecord> readAll(String pattern) throws FileFormatException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() throws IOException, FileFormatException {
        int total = IOUtils.countLines(file);
        int comment = IOUtils.grep(file, "#·+").size();
        return total - comment;
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }

    /**
     * @return the vcfFilters
     */
    public List<Predicate<VcfRecord>> getVcfFilters() {
        return vcfFilters;
    }

    /**
     * @param vcfFilters the vcfFilters to set
     */
    public void setVcfFilters(List<Predicate<VcfRecord>> vcfFilters) {
        this.vcfFilters = vcfFilters;
        this.andVcfFilters = Predicates.and(this.vcfFilters);
    }

    public List<String>
    getSampleNames() {
        return this.vcf4.getSamples();
    }

}
