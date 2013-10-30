package org.opencb.commons.bioformats.variant.vcf4.io.writers.stats;

import com.google.common.base.Joiner;
import org.opencb.commons.bioformats.commons.SqliteSingletonConnection;
import org.opencb.commons.bioformats.feature.Genotype;
import org.opencb.commons.bioformats.variant.vcf4.VariantEffect;
import org.opencb.commons.bioformats.variant.vcf4.stats.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantStatsSqliteDataWriter implements VariantStatsDataWriter {

    private Statement stmt;
    private PreparedStatement pstmt;
    private boolean createdSampleTable;
    private SqliteSingletonConnection connection;


    public VariantStatsSqliteDataWriter(String dbName) {

        this.stmt = null;
        this.pstmt = null;
        this.createdSampleTable = false;
        this.connection = new SqliteSingletonConnection(dbName);
    }

    @Override
    public boolean open() {

        return SqliteSingletonConnection.getConnection() != null;

    }

    @Override
    public boolean close() {

        try {
            SqliteSingletonConnection.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
                "id_variant INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "chromosome TEXT, " +
                "position INT64, " +
                "allele_ref TEXT, " +
                "allele_alt TEXT, " +
                "id TEXT, " +
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
                "controls_percent_recessive DOUBLE, " +
                "genotypes TEXT);";
        String sample_stats = "CREATE TABLE IF NOT EXISTS sample_stats(" +
                "name TEXT, " +
                "mendelian_errors INT, " +
                "missing_genotypes INT, " +
                "homozygotesNumber INT, " +
                "PRIMARY KEY (name));";


        try {
            stmt = SqliteSingletonConnection.getConnection().createStatement();

            stmt.execute(globalStatsTable);
            stmt.execute(variant_stats);
            stmt.execute(sample_stats);

            stmt.close();

            SqliteSingletonConnection.getConnection().commit();
        } catch (SQLException e) {
            System.err.println("PRE: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean post() {

        try {

            stmt = SqliteSingletonConnection.getConnection().createStatement();
            stmt.execute("CREATE INDEX variant_stats_chromosome_position_idx ON variant_stats (chromosome, position);");
            stmt.close();
            SqliteSingletonConnection.getConnection().commit();

        } catch (SQLException e) {
            System.err.println("POST: " + e.getClass().getName() + ": " + e.getMessage());
            return false;

        }

        return true;
    }

    @Override
    public boolean writeVariantStats(List<VcfVariantStat> data) {

        String sql = "INSERT INTO variant_stats (chromosome, position, allele_ref, allele_alt, id, maf, mgf, allele_maf, genotype_maf, miss_allele, miss_gt, mendel_err, is_indel, cases_percent_dominant, controls_percent_dominant, cases_percent_recessive, controls_percent_recessive, genotypes) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        boolean res = true;

        List<String> genotypes = new ArrayList<>(10);

        try {
            pstmt = SqliteSingletonConnection.getConnection().prepareStatement(sql);

            for (VcfVariantStat v : data) {
                pstmt.setString(1, v.getChromosome());
                pstmt.setLong(2, v.getPosition());
                pstmt.setString(3, v.getRefAlleles());
                pstmt.setString(4, Joiner.on(",").join(v.getAltAlleles()));
                pstmt.setString(5, v.getId());
                pstmt.setDouble(6, v.getMaf());
                pstmt.setDouble(7, v.getMgf());
                pstmt.setString(8, v.getMafAllele());
                pstmt.setString(9, v.getMgfAllele());
                pstmt.setInt(10, v.getMissingAlleles());
                pstmt.setInt(11, v.getMissingGenotypes());
                pstmt.setInt(12, v.getMendelinanErrors());
                pstmt.setInt(13, (v.isIndel() ? 1 : 0));
                pstmt.setDouble(14, v.getCasesPercentDominant());
                pstmt.setDouble(15, v.getControlsPercentDominant());
                pstmt.setDouble(16, v.getCasesPercentRecessive());
                pstmt.setDouble(17, v.getControlsPercentRecessive());

                for (Genotype g : v.getGenotypes()) {
                    genotypes.add(g.toString());
                }
                pstmt.setString(18, Joiner.on(",").join(genotypes));

                pstmt.execute();
                genotypes.clear();

            }
            SqliteSingletonConnection.getConnection().commit();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("VARIANT_STATS: " + e.getClass().getName() + ": " + e.getMessage());
            res = false;
        }


        return res;
    }

    @Override
    public boolean writeGlobalStats(VcfGlobalStat globalStats) {
        boolean res = true;
        float titv = 0;
        float pass = 0;
        float avg = 0;
        try {
            String sql;
            stmt = SqliteSingletonConnection.getConnection().createStatement();

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
            sql = "INSERT INTO global_stats VALUES ('NUM_TRANSVERSIONS', 'Number of transversions'," + globalStats.getTransversionsCount() + ");";
            stmt.executeUpdate(sql);
            if (globalStats.getTransversionsCount() > 0) {
                titv = globalStats.getTransitionsCount() / (float) globalStats.getTransversionsCount();
            }
            sql = "INSERT INTO global_stats VALUES ('TITV_RATIO', 'Ti/TV ratio'," + titv + ");";
            stmt.executeUpdate(sql);
            if (globalStats.getVariantsCount() > 0) {
                pass = globalStats.getPassCount() / (float) globalStats.getVariantsCount();
                avg = globalStats.getAccumQuality() / (float) globalStats.getVariantsCount();
            }

            sql = "INSERT INTO global_stats VALUES ('PERCENT_PASS', 'Percentage of PASS'," + (pass * 100) + ");";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO global_stats VALUES ('AVG_QUALITY', 'Average quality'," + avg + ");";
            stmt.executeUpdate(sql);

            SqliteSingletonConnection.getConnection().commit();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("GLOBAL_STATS: " + e.getClass().getName() + ": " + e.getMessage());
            res = false;
        }

        return res;
    }

    @Override
    public boolean writeSampleStats(VcfSampleStat sampleStat) {
        String sql = "INSERT INTO sample_stats VALUES(?,?,?,?);";
        SampleStat s;
        String name;
        boolean res = true;
        try {
            pstmt = SqliteSingletonConnection.getConnection().prepareStatement(sql);

            for (Map.Entry<String, SampleStat> entry : sampleStat.getSamplesStats().entrySet()) {
                s = entry.getValue();
                name = entry.getKey();

                pstmt.setString(1, name);
                pstmt.setInt(2, s.getMendelianErrors());
                pstmt.setInt(3, s.getMissingGenotypes());
                pstmt.setInt(4, s.getHomozygotesNumber());
                pstmt.execute();

            }
            SqliteSingletonConnection.getConnection().commit();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("SAMPLE_STATS: " + e.getClass().getName() + ": " + e.getMessage());
            res = false;
        }


        return res;
    }

    @Override
    public boolean writeSampleGroupStats(VcfSampleGroupStat sampleGroupStats) {
        return false;
    }

    @Override
    public boolean writeVariantGroupStats(VcfVariantGroupStat groupStats) {
        return false;
    }


}