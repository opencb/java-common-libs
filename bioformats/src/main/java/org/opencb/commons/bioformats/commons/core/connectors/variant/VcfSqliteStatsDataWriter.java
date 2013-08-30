package org.opencb.commons.bioformats.commons.core.connectors.variant;

import org.opencb.commons.bioformats.commons.core.vcfstats.*;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfSqliteStatsDataWriter implements VcfStatsDataWriter {

    private String dbName;
    private Connection con;
    private Statement stmt = null;

    public VcfSqliteStatsDataWriter(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public boolean open() throws IOException {

        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("dbName = " + dbName);
            con = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            con.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;

        }

        return true;
    }

    @Override
    public boolean close() {

        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean pre() {
        String globalStatsTable =  "CREATE TABLE IF NOT EXISTS global_stats (name TEXT PRIMARY KEY, title TEXT, value TEXT);";
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(globalStatsTable);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return true;
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
        try {
            String sql;
            stmt = con.createStatement();

            sql = "INSERT INTO global_stats VALUES ('NUM_VARIANTS', 'Number of variants'," + globalStats.getVariantsCount() + ");";
            System.out.println("sql = " + sql);
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('NUM_SAMPLES', 'Number of samples'," + globalStats.getSamplesCount() + ");";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('NUM_BIALLELIC', 'Number of biallelic variants'," + globalStats.getBiallelicsCount() + ");";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('NUM_MULTIALLELIC', 'Number of multiallelic variants'," + globalStats.getMultiallelicsCount() + ");";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('NUM_SNPS', 'Number of SNP'," + globalStats.getSnpsCount() + ");";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('NUM_INDELS', 'Number of indels'," + globalStats.getIndelsCount() + ");";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('NUM_TRANSITIONS', 'Number of transitions'," + globalStats.getTransitionsCount() + ");";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('NUM_TRANSVERSSIONS', 'Number of transversions'," + globalStats.getTransversionsCount() + ");";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('TITV_RATIO', 'Ti/TV ratio'," + ((float) globalStats.getTransitionsCount() / (float) globalStats.getTransversionsCount()) + ");";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('PERCENT_PASS', 'Percentage of PASS'," + (((float) globalStats.getPassCount() / (float) globalStats.getVariantsCount()) * 100) + ");";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO global_stats VALUES ('AVG_QUALITY', 'Average quality'," + (globalStats.getAccumQuality() / (float) globalStats.getVariantsCount()) + ");";
            stmt.executeUpdate(sql);

            stmt.close();
            con.commit();

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

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
