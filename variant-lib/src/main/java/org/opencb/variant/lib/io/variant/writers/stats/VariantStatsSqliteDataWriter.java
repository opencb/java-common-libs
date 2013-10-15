package org.opencb.variant.lib.io.variant.writers.stats;

import org.bioinfo.commons.utils.StringUtils;
import org.opencb.variant.lib.core.formats.*;

import java.sql.*;
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

    private String dbName;
    private Connection con;
    private Statement stmt;
    private PreparedStatement pstmt;
    private boolean createdSampleTable;


    public VariantStatsSqliteDataWriter(String dbName) {

        this.dbName = dbName;
        this.stmt = null;
        this.pstmt = null;
        this.createdSampleTable = false;
    }

    @Override
    public boolean open() {

        try {
            Class.forName("org.sqlite.JDBC");
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
                "controls_percent_recessive DOUBLE);";
        String sample_stats = "CREATE TABLE IF NOT EXISTS sample_stats(" +
                "name TEXT, " +
                "mendelian_errors INT, " +
                "missing_genotypes INT, " +
                "homozygotesNumber INT, " +
                "PRIMARY KEY (name));";


        String variantEffectTable = "CREATE TABLE IF NOT EXISTS variant_effect(" +
                "id_variant_effect INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "chromosome	TEXT, " +
                "position INT64, " +
                "reference_allele TEXT, " +
                "alternative_allele TEXT, " +
                "feature_id TEXT, " +
                "feature_name TEXT, " +
                "feature_type TEXT, " +
                "feature_biotype TEXT, " +
                "feature_chromosome TEXT, " +
                "feature_start INT64, " +
                "feature_end INT64, " +
                "feature_strand TEXT, " +
                "snp_id TEXT, " +
                "ancestral TEXT, " +
                "alternative TEXT, " +
                "gene_id TEXT, " +
                "transcript_id TEXT, " +
                "gene_name TEXT, " +
                "consequence_type TEXT, " +
                "consequence_type_obo TEXT, " +
                "consequence_type_desc TEXT, " +
                "consequence_type_type TEXT, " +
                "aa_position INT64, " +
                "aminoacid_change TEXT, " +
                "codon_change TEXT); ";

        try {
            stmt = con.createStatement();

            stmt.execute(globalStatsTable);
            stmt.execute(variant_stats);
            stmt.execute(sample_stats);
            stmt.execute(variantEffectTable);

            stmt.close();

            con.commit();
        } catch (SQLException e) {
            System.err.println("PRE: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean post() {

        try {

            stmt = con.createStatement();
            stmt.execute("CREATE INDEX variant_stats_chromosome_position_idx ON variant_stats (chromosome, position);");
            stmt.execute("CREATE INDEX variant_effect_chromosome_position_idx ON variant_effect (chromosome, position);");
            stmt.execute("CREATE INDEX variant_effect_feature_biotype_idx ON variant_effect (feature_biotype);");
            stmt.execute("CREATE INDEX variant_effect_consequence_type_obo_idx ON variant_effect (consequence_type_obo);");
            stmt.close();
            con.commit();

        } catch (SQLException e) {
            System.err.println("POST: " + e.getClass().getName() + ": " + e.getMessage());
            return false;

        }

        return true;
    }

    @Override
    public boolean writeVariantStats(List<VcfVariantStat> data) {

        String sql = "INSERT INTO variant_stats (chromosome, position, allele_ref, allele_alt, id, maf, mgf, allele_maf, genotype_maf, miss_allele, miss_gt, mendel_err, is_indel, cases_percent_dominant, controls_percent_dominant, cases_percent_recessive, controls_percent_recessive) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        boolean res = true;

        try {
            pstmt = con.prepareStatement(sql);

            for (VcfVariantStat v : data) {
                pstmt.setString(1, v.getChromosome());
                pstmt.setLong(2, v.getPosition());
                pstmt.setString(3, v.getRefAlleles());
                pstmt.setString(4, StringUtils.join(v.getAltAlleles(), ","));
                pstmt.setString(5, v.getId());
                pstmt.setDouble(6, v.getMaf());
                pstmt.setDouble(7, v.getMgf());
                pstmt.setString(8, v.getMafAllele());
                pstmt.setString(9, v.getMgfAllele());
                pstmt.setInt(10, v.getMissingAlleles());
                pstmt.setInt(11, v.getMissingGenotypes());
                pstmt.setInt(12, v.getMendelinanErrors());
                pstmt.setInt(13, (v.getIndel() ? 1 : 0));
                pstmt.setDouble(14, v.getCasesPercentDominant());
                pstmt.setDouble(15, v.getControlsPercentDominant());
                pstmt.setDouble(16, v.getCasesPercentRecessive());
                pstmt.setDouble(17, v.getControlsPercentRecessive());

                pstmt.execute();

            }
            con.commit();
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

            con.commit();
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
            pstmt = con.prepareStatement(sql);

            for (Map.Entry<String, SampleStat> entry : sampleStat.getSamplesStats().entrySet()) {
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

    @Override
    public boolean writeVariantEffect(List<VariantEffect> batchEffect) {

        String sql = "INSERT INTO variant_effect(chromosome	, position , reference_allele , alternative_allele , " +
                "feature_id , feature_name , feature_type , feature_biotype , feature_chromosome , feature_start , " +
                "feature_end , feature_strand , snp_id , ancestral , alternative , gene_id , transcript_id , gene_name , " +
                "consequence_type , consequence_type_obo , consequence_type_desc , consequence_type_type , aa_position , " +
                "aminoacid_change , codon_change) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

        boolean res = true;

        try {
            pstmt = con.prepareStatement(sql);

            for (VariantEffect v : batchEffect) {
                pstmt.setString(1, v.getChromosome());
                pstmt.setInt(2, v.getPosition());
                pstmt.setString(3, v.getReferenceAllele());
                pstmt.setString(4, v.getAlternativeAllele());
                pstmt.setString(5, v.getFeatureId());
                pstmt.setString(6, v.getFeatureName());
                pstmt.setString(7, v.getFeatureType());
                pstmt.setString(8, v.getFeatureBiotype());
                pstmt.setString(9, v.getFeatureChromosome());
                pstmt.setInt(10, v.getFeatureStart());
                pstmt.setInt(11, v.getFeatureEnd());
                pstmt.setString(12, v.getFeatureStrand());
                pstmt.setString(13, v.getSnpId());
                pstmt.setString(14, v.getAncestral());
                pstmt.setString(15, v.getAlternative());
                pstmt.setString(16, v.getGeneId());
                pstmt.setString(17, v.getTranscriptId());
                pstmt.setString(18, v.getGeneName());
                pstmt.setString(19, v.getConsequenceType());
                pstmt.setString(20, v.getConsequenceTypeObo());
                pstmt.setString(21, v.getConsequenceTypeDesc());
                pstmt.setString(22, v.getConsequenceTypeType());
                pstmt.setInt(23, v.getAaPosition());
                pstmt.setString(24, v.getAminoacidChange());
                pstmt.setString(25, v.getCodonChange());

                pstmt.execute();

            }
            con.commit();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("VARIANT_EFFECT: " + e.getClass().getName() + ": " + e.getMessage());
            res = false;
        }
        return res;
    }
}