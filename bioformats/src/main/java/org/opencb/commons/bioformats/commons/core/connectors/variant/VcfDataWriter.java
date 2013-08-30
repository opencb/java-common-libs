package org.opencb.commons.bioformats.commons.core.connectors.variant;

import org.opencb.commons.bioformats.commons.core.connectors.DataWriter;
import org.opencb.commons.bioformats.commons.core.vcfstats.*;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VcfDataWriter extends DataWriter<VcfRecordStat>{
    public boolean write(VcfGlobalStat globalStats);

    public boolean write(VcfSampleStat sampleStat);

    public boolean write(VcfSampleGroupStats sampleGroupStats) throws IOException;

    public boolean write(VcfVariantGroupStat groupStats) throws IOException;
}
