package org.opencb.commons.bioformats.alignment.io.writers.coverage;

import org.opencb.commons.bioformats.alignment.stats.AlignmentCoverage;
import org.opencb.commons.io.DataWriter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/4/13
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AlignmentCoverageDataWriter extends DataWriter {
    public boolean writeBatch(List<AlignmentCoverage> batch);

}
