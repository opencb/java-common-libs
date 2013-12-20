package org.opencb.commons.bioformats.variant.vcf4.io.readers;


import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.io.DataReader;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VariantDataReader extends DataReader<VcfRecord> {
    public List<String> getSampleNames();

//    public void setSampleNames(List<String> sampleNames);

    public String getHeader();
}
