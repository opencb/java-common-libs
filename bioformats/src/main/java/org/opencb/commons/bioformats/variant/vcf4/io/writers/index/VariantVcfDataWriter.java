package org.opencb.commons.bioformats.variant.vcf4.io.writers.index;


import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/15/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantVcfDataWriter implements VariantDataWriter<VcfRecord> {

    private PrintWriter printer;
    private String filename;


    public VariantVcfDataWriter(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean open() {

        boolean res = true;
        try {
            printer = new PrintWriter(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            res = false;
        }

        return res;
    }

    @Override
    public boolean close() {

        printer.close();

        return true;
    }

    @Override
    public boolean pre() {

        return true;
    }

    @Override
    public boolean post() {
        return true;
    }

    @Override
    public boolean write(Object elem) {
        return false;
    }

    @Override
    public boolean write(List batch) {
        return false;
    }

    @Override
    public boolean writeHeader(String header) {

        printer.append(header);

        return true;

    }

    @Override
    public boolean writeBatch(List<VcfRecord> batch) {

        for (VcfRecord record : batch) {
            printer.append(record.toString()).append("\n"); // TODO aaleman: Create a Variant2Vcf converter.
        }

        return true;
    }
}
