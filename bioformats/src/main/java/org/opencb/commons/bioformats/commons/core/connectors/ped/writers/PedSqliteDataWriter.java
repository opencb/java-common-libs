package org.opencb.commons.bioformats.commons.core.connectors.ped.writers;

import org.opencb.commons.bioformats.commons.core.feature.Individual;
import org.opencb.commons.bioformats.commons.core.feature.Pedigree;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/1/13
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class PedSqliteDataWriter implements PedDataWriter {

    private String dbName;
    private Connection con;
    private Statement stmt;
    private PreparedStatement pstmt;


    public PedSqliteDataWriter(String dbName) {
        this.dbName = dbName;
        stmt = null;
        pstmt = null;
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
        boolean res = true;

        String pedTable = "CREATE TABLE IF NOT EXISTS pedigree(" +
                "id_pedigree INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sample TEXT , " +
                "family TEXT, " +
                "father TEXT, " +
                "mother TEXT, " +
                "sex TEXT, " +
                "phenotype TEXT, " +
                "UNIQUE(sample));";


        try {
            stmt = con.createStatement();
            stmt.execute(pedTable);
            stmt.close();

            con.commit();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            res= false;
        }

        return res;
    }

    @Override
    public boolean post() {

        boolean res = true;
        try {

            stmt = con.createStatement();
            stmt.execute("CREATE INDEX pedigree_sample_idx ON pedigree(sample);");
            stmt.execute("CREATE INDEX pedigree_family_idx ON pedigree(family);");
            stmt.close();
            con.commit();

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            res = false;

        }

        return res;
    }

    @Override
    public boolean write(Pedigree data) {

        Map<String, Individual> individuals = data.getIndividuals();
        Individual ind;
        boolean res = true;

        String sql = "INSERT INTO pedigree (sample, family, father, mother, sex, phenotype) VALUES(?,?,?,?,?,?);";

        try {
            pstmt = con.prepareStatement(sql);
            for (Map.Entry<String, Individual> entry : individuals.entrySet()) {
                ind = entry.getValue();
                pstmt.setString(1, ind.getId());
                pstmt.setString(2, ind.getFamily());
                pstmt.setString(3, (ind.getFather() != null)? ind.getFather().getId(): "0");
                pstmt.setString(4, (ind.getMother() != null) ? ind.getMother().getId() : "0");

                pstmt.setString(5, ind.getSex());
                pstmt.setString(6, ind.getPhenotype());

                pstmt.execute();

            }

            pstmt.close();
            con.commit();

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            res = false;
        }

        return res;
    }


}
