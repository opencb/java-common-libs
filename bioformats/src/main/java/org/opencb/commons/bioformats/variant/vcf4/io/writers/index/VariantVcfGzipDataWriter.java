package org.opencb.commons.bioformats.variant.vcf4.io.writers.index;


import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/15/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantVcfGzipDataWriter implements VariantDataWriter<VcfRecord> {

    private BufferedWriter printer;
    private String filename;


    public VariantVcfGzipDataWriter(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean open() {

        boolean res = true;
        try {
            printer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(this.filename))));
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }

        return res;
    }

    @Override
    public boolean close() {

        try {
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    public boolean writeHeader(String header) {

        boolean res = true;
        try {
            printer.append(header).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }
        return res;

    }

    @Override
    public boolean writeBatch(List<VcfRecord> batch) {

        boolean res = true;
        try {
            for (VcfRecord record : batch) {
                printer.append(record.toString()).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }
}