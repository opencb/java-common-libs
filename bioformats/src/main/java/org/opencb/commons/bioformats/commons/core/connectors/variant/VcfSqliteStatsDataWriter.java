package org.opencb.commons.bioformats.commons.core.connectors.variant;

import org.bioinfo.commons.utils.StringUtils;
import org.opencb.commons.bioformats.commons.core.vcfstats.*;

import java.io.IOException;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    private PreparedStatement pstmt = null;

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
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
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
        String globalStatsTable = "CREATE TABLE IF NOT EXISTS global_stats (" +
                "name TEXT," +
                " title TEXT," +
                " value TEXT," +
                "PRIMARY KEY (name));";
        String variant_stats = "CREATE TABLE IF NOT EXISTS variant_stats (" +
                "chromosome TEXT, " +
                "position INT64, " +
                "allele_ref TEXT, " +
                "allele_alt TEXT, " +
                "maf DOUBLE, " +
                "mgf DOUBLE," +
                "allele_maf TEXT, " +
                "genotype_maf TEXT, " +
                "miss_allele INT, " +
                "miss_gt INT, " +
                "mendel_err INT, " +
                "is_indel INT, " +
                "cases_percent_dominant DOUBLE, " +
                "controls_percent_dominant DOUBLE, " +
                "cases_percent_recessive DOUBLE, " +
                "controls_percent_recessive DOUBLE);";
        String sample_stats = "CREATE TABLE IF NOT EXISTS sample_stats(" +
                "id TEXT, " +
                "mendelian_errors INT, " +
                "missing_genotypes INT, " +
                "homozygotesNumber INT, " +
                "PRIMARY KEY (id));";


        try {
            stmt = con.createStatement();
            stmt.execute(globalStatsTable);
            stmt.execute(variant_stats);
            stmt.execute(sample_stats);
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        return true;
    }

    @Override
    public boolean post() {
        String sql = "CREATE INDEX variant_stats_chromosome_position_idx ON variant_stats (chromosome, position);";

        try {

            stmt = con.createStatement();
            stmt.execute(sql);
            stmt.close();
            con.commit();

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

        }

        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(VcfRecordStat data) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(List<VcfRecordStat> data) {

        String sql = "INSERT INTO variant_stats VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

        try {
            pstmt = con.prepareStatement(sql);

            for (VcfRecordStat v : data) {
                pstmt.setString(1, v.getChromosome());
                pstmt.setLong(2, v.getPosition());
                pstmt.setString(3, v.getRefAlleles());
                pstmt.setString(4, StringUtils.join(v.getAltAlleles(), ","));
                pstmt.setDouble(5, v.getMaf());
                pstmt.setDouble(6, v.getMgf());
                pstmt.setString(7, v.getMafAllele());
                pstmt.setString(8, v.getMgfAllele());
                pstmt.setInt(9, v.getMissingAlleles());
                pstmt.setInt(10, v.getMissingGenotypes());
                pstmt.setInt(11, v.getMendelinanErrors());
                pstmt.setInt(12, (v.getIndel() ? 1 : 0));
                pstmt.setDouble(13, v.getCasesPercentDominant());
                pstmt.setDouble(14, v.getControlsPercentDominant());
                pstmt.setDouble(15, v.getCasesPercentRecessive());
                pstmt.setDouble(16, v.getControlsPercentRecessive());

                pstmt.execute();

            }
            con.commit();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }


        return true;
    }

    @Override
    public boolean write(VcfGlobalStat globalStats) {
        try {
            String sql;
            stmt = con.createStatement();

            sql = "INSERT INTO global_stats VALUES ('NUM_VARIANTS', 'Number of variants'," + globalStats.getVariantsCount() + ");";
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

            con.commit();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(VcfSampleStat sampleStat) {
        String sql = "INSERT INTO sample_stats VALUES(?,?,?,?);";
        SampleStat s;
        String name;
        try {
            pstmt = con.prepareStatement(sql);

            for (Map.Entry<String,SampleStat> entry : sampleStat.getSamplesStats().entrySet()) {
                     s = entry.getValue();
                name = entry.getKey();

                pstmt.setString(1, name);
                pstmt.setInt(2, s.getMendelianErrors());
                pstmt.setInt(3, s.getMissingGenotypes());
                pstmt.setInt(4, s.getHomozygotesNumber());

                pstmt.execute();

            }
            con.commit();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }


        return true;    }

    @Override
    public boolean write(VcfSampleGroupStats sampleGroupStats) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(VcfVariantGroupStat groupStats) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
