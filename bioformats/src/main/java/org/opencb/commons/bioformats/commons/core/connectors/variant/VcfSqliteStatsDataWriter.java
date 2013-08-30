package org.opencb.commons.bioformats.commons.core.connectors.variant;

import org.opencb.commons.bioformats.commons.core.vcfstats.*;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfSqliteStatsDataWriter implements VcfStatsDataWriter {

    @Override
    public boolean open() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean close() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean pre() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean post() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(VcfRecordStat data) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(List<VcfRecordStat> data) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(VcfGlobalStat globalStats) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(VcfSampleStat sampleStat) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(VcfSampleGroupStats sampleGroupStats){
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(VcfVariantGroupStat groupStats){
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
