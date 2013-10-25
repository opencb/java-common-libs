package org.opencb.commons.bioformats.variant.vcf4.io.writers.effect;

import org.opencb.commons.bioformats.commons.SqliteSingletonConnection;
import org.opencb.commons.bioformats.variant.vcf4.VariantEffect;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.index.VariantIndexDataWriter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 10/24/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantEffectSqliteDataWriter implements VariantEffectDataWriter {

    private Statement stmt;
    private PreparedStatement pstmt;
    private SqliteSingletonConnection connection;

    public VariantEffectSqliteDataWriter(String dbName) {
        this.stmt = null;
        this.pstmt = null;
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
            stmt = SqliteSingletonConnection.getConnection().createStatement();
            stmt.execute(variantEffectTable);
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
            stmt.execute("CREATE INDEX variant_effect_chromosome_position_idx ON variant_effect (chromosome, position);");
            stmt.execute("CREATE INDEX variant_effect_feature_biotype_idx ON variant_effect (feature_biotype);");
            stmt.execute("CREATE INDEX variant_effect_consequence_type_obo_idx ON variant_effect (consequence_type_obo);");

            stmt.execute("CREATE TABLE IF NOT EXISTS consequence_type_count AS SELECT count(*) as count, consequence_type_obo from variant_effect group by consequence_type_obo order by consequence_type_obo ASC;  ");
            stmt.execute("CREATE TABLE IF NOT EXISTS biotype_count AS SELECT count(*) as count, feature_biotype from variant_effect group by feature_biotype order by feature_biotype ASC;  ");
            stmt.close();
            SqliteSingletonConnection.getConnection().commit();

        } catch (SQLException e) {
            System.err.println("POST: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }

        return true;
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
            pstmt = SqliteSingletonConnection.getConnection().prepareStatement(sql);

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
            SqliteSingletonConnection.getConnection().commit();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("VARIANT_EFFECT: " + e.getClass().getName() + ": " + e.getMessage());
            res = false;
        }
        return res;
    }
}
