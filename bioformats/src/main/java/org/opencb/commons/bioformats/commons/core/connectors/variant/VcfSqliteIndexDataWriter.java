package org.opencb.commons.bioformats.commons.core.connectors.variant;

import org.bioinfo.commons.utils.StringUtils;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.Genotype;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;

import java.beans.Statement;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/30/13
 * Time: 7:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfSqliteIndexDataWriter implements VcfIndexDataWriter {

    private String dbName;
    private Connection con;
    private java.sql.Statement stmt;
    private boolean createdSampleTable;

    public VcfSqliteIndexDataWriter(String dbName) {
        this.dbName = dbName;
        createdSampleTable = false;
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
            return false;
        }
        return true;
    }

    @Override
    public boolean pre() {

        String variantTable = "CREATE TABLE IF NOT EXISTS variant (" +
                "id_variant INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "chromosome TEXT, " +
                "position INT64, " +
                "id TEXT, " +
                "ref TEXT, " +
                "alt TEXT, " +
                "qual DOUBLE, " +
                "filter TEXT, " +
                "info TEXT, " +
                "format TEXT," +
                "UNIQUE(chromosome, position, ref));";

        String sampleTable = "CREATE TABLE IF NOT EXISTS sample(" +
                "name TEXT PRIMARY KEY);";

        String sampleInfoTable = "CREATE TABLE IF NOT EXISTS sample_info(" +
                "id_sample_info INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_variant INTEGER, " +
                "sample_name TEXT, " +
                "allele_1 INTEGER, " +
                "allele_2 INTEGER, " +
                "data TEXT, " +
                "FOREIGN KEY(id_variant) REFERENCES variant(id_variant)," +
                "FOREIGN KEY(sample_name) REFERENCES sample(name));";


        try {

            stmt = con.createStatement();
            stmt.execute(variantTable);
            stmt.execute(sampleTable);
            stmt.execute(sampleInfoTable);
            stmt.close();
            con.commit();

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean post() {

        try {

            stmt = con.createStatement();
            stmt.execute("CREATE INDEX variant_chromosome_position_idx ON variant (chromosome, position);");
            stmt.execute("CREATE INDEX variant_pass_idx ON variant (filter);");
            stmt.execute("CREATE INDEX variant_id_idx ON variant (id);");
            stmt.execute("CREATE INDEX sample_name_idx ON sample (name);");
            stmt.close();
            con.commit();

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

        }

        return true;
    }

    @Override
    public boolean write(VcfRecord data) {
        return false;
    }

    @Override
    public boolean write(List<VcfRecord> data) {
        String sql, sqlAux;
        PreparedStatement pstmt_aux;
        String sampleName;
        String sampleData;
        int allele_1, allele_2;
        Genotype g;
        int id;
        boolean res = true;

        PreparedStatement pstmt;
        if (!createdSampleTable) {
            try {
                sql = "INSERT INTO sample (name) VALUES(?);";
                pstmt = con.prepareStatement(sql);
                VcfRecord v = data.get(0);
                for (Map.Entry<String, Integer> entry : v.getSampleIndex().entrySet()) {
                    pstmt.setString(1, entry.getKey());
                    pstmt.execute();

                }

                pstmt.close();
                con.commit();
                createdSampleTable = true;
            } catch (SQLException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                res = false;
            }
        }

        sql = "INSERT INTO variant (chromosome, position, id, ref, alt, qual, filter, info, format) VALUES(?,?,?,?,?,?,?,?,?);";
        sqlAux = "INSERT INTO sample_info(id_variant, sample_name, allele_1, allele_2, data) VALUES (?,?,?,?,?);";
        try {

            pstmt = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt_aux = con.prepareStatement(sqlAux);

            for (VcfRecord v : data) {

                pstmt.setString(1, v.getChromosome());
                pstmt.setInt(2, v.getPosition());
                pstmt.setString(3, v.getId());
                pstmt.setString(4, v.getReference());
                pstmt.setString(5, StringUtils.join(v.getAltAlleles(), ","));
                pstmt.setDouble(6, (v.getQuality().equals(".") ? 0 : Double.valueOf(v.getQuality())));
                pstmt.setString(7, v.getFilter());
                pstmt.setString(8, v.getInfo());
                pstmt.setString(9, v.getFormat());

                pstmt.execute();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    id = rs.getInt(1);
                    for (Map.Entry<String, Integer> entry : v.getSampleIndex().entrySet()) {
                        sampleName = entry.getKey();
                        sampleData = v.getSamples().get(entry.getValue());
                        g = v.getSampleGenotype(sampleName);

                        allele_1 = (g.getAllele1() == null) ? -1 : g.getAllele1();
                        allele_2 = (g.getAllele2() == null) ? -1 : g.getAllele2();

                        pstmt_aux.setInt(1, id);
                        pstmt_aux.setString(2, sampleName);
                        pstmt_aux.setInt(3, allele_1);
                        pstmt_aux.setInt(4, allele_2);
                        pstmt_aux.setString(5, sampleData);
                        pstmt_aux.execute();

                    }

                } else {
                    res = false;
                }


            }
            pstmt.close();
            pstmt_aux.close();

            con.commit();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            res = false;
        }

        return res;
    }
}
