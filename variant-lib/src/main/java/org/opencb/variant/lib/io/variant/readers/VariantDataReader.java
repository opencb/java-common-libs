package org.opencb.variant.lib.io.variant.readers;


import org.opencb.variant.lib.core.formats.VcfRecord;
import org.opencb.variant.lib.io.DataReader;

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
    public String getHeader();
}
