package org.opencb.variant.lib.io;


import org.opencb.variant.lib.core.formats.VcfVariantStat;

import java.sql.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/8/13
 * Time: 9:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class WSSqliteManager {

    private static final String pathDB = "/httpd/bioinfo/www-apps/bierapp/data/";

    public static List<VcfVariantStat> getRecords(HashMap<String, String> options) {

        Connection con = null;
        Statement stmt;
        List<VcfVariantStat> list = new ArrayList<>(100);
        ResultSet res = null;

        String dbName = options.get("db_name");

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + pathDB + dbName);

            List<String> whereClauses = new ArrayList<>(10);

            if (options.containsKey("chrpos") && !options.get("chrpos").equals("")) {
                String chrPos = options.get("chrpos").split(":")[0];
                int start = Integer.parseInt(options.get("chrpos").split(":")[1].split("-")[0]);
                int end = Integer.parseInt(options.get("chrpos").split(":")[1].split("-")[1]);

                whereClauses.add("chromosome='" + chrPos + "'");
                whereClauses.add("position>=" + start);
                whereClauses.add("position<=" + end);

            }


            if (options.containsKey("mend_error") && !options.get("mend_error").equals("")) {
                String val = options.get("mend_error");
                String opt = options.get("option_mend_error");
                whereClauses.add("mendel_err " + opt + " " + val);

            }

            if (options.containsKey("is_indel") && options.get("is_indel").equalsIgnoreCase("on")) {
                whereClauses.add("is_indel=1");
            }

            if (options.containsKey("maf") && !options.get("maf").equals("")){
                String val = options.get("maf");
                String opt = options.get("option_maf");
                whereClauses.add("maf " + opt + " " + val);

            }

            if (options.containsKey("mgf") && !options.get("mgf").equals("")){
                String val = options.get("mgf");
                String opt = options.get("option_mgf");
                whereClauses.add("mgf " + opt + " " + val);

            }

            if (options.containsKey("miss_allele") && !options.get("miss_allele").equals("")) {
                String val = options.get("miss_allele");
                String opt = options.get("option_miss_allele");
                whereClauses.add("miss_allele " + opt + " " + val);
            }
            if (options.containsKey("miss_gt") && !options.get("miss_gt").equals("")) {
                String val = options.get("miss_gt");
                String opt = options.get("option_miss_gt");
                whereClauses.add("miss_gt " + opt + " " + val);

            }
            if (options.containsKey("cases_percent_dominant") && !options.get("cases_percent_dominant").equals("")) {
                String val = options.get("cases_percent_dominant");
                String opt = options.get("option_cases_dom");
                whereClauses.add("cases_percent_dominant " + opt + " " + val);
            }

            if (options.containsKey("controls_percent_dominant") && !options.get("controls_percent_dominant").equals("")) {
                String val = options.get("controls_percent_dominant");
                String opt = options.get("option_controls_dom");
                whereClauses.add("controls_percent_dominant " + opt + " " + val);
            }

            if (options.containsKey("cases_percent_recessive") && !options.get("cases_percent_recessive").equals("")) {
                String val = options.get("cases_percent_recessive");
                String opt = options.get("option_cases_rec");
                whereClauses.add("cases_percent_recessive " + opt + " " + val);
            }

            if (options.containsKey("controls_percent_recessive") && !options.get("controls_percent_recessive").equals("")) {
                String val = options.get("controls_percent_recessive");
                String opt = options.get("option_controls_rec");
                whereClauses.add("controls_percent_recessive " + opt + " " + val);
            }


            String sql = "SELECT * FROM variant_stats ";


            if (whereClauses.size() > 0) {
                StringBuilder where = new StringBuilder(" where ");

                for (int i = 0; i < whereClauses.size(); i++) {
                    where.append(whereClauses.get(i));
                    if (i < whereClauses.size() - 1) {
                        where.append(" AND ");
                    }
                }

                sql += where.toString() + " ;";
            }

            System.out.println(sql);

            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            VcfVariantStat vs;
            while (rs.next()) {
                vs = new VcfVariantStat(rs.getString("chromosome"), rs.getInt("position"), rs.getString("allele_ref"), rs.getString("allele_alt"),
                        rs.getDouble("maf"), rs.getDouble("mgf"), rs.getString("allele_maf"), rs.getString("genotype_maf"), rs.getInt("miss_allele"),
                        rs.getInt("miss_gt"), rs.getInt("mendel_err"), rs.getInt("is_indel"), rs.getDouble("cases_percent_dominant"), rs.getDouble("controls_percent_dominant"),
                        rs.getDouble("cases_percent_recessive"), rs.getDouble("controls_percent_recessive"));

                list.add(vs);
            }


            con.close();
            stmt.close();

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }


        return list;
    }
}
