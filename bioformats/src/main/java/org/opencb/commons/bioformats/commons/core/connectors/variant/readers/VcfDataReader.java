package org.opencb.commons.bioformats.commons.core.connectors.variant.readers;

import org.opencb.commons.bioformats.commons.core.connectors.DataReader;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VcfDataReader extends DataReader<VcfRecord> {
    public List<String> getSampleNames();
}
