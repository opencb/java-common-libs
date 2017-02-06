/*
 * Copyright 2015-2017 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.commons.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 10/24/13
 * Time: 12:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class SqliteSingletonConnection {

    private static Connection con = null;
    private static String dbName;

    public SqliteSingletonConnection(String dbName) {
        this.dbName = dbName;
    }

    public static Connection getConnection() {
        if (con == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                con = DriverManager.getConnection("jdbc:sqlite:" + dbName);
                con.setAutoCommit(false);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println(e.getClass().getName() + ": " + e.getMessage());

            }
        }
        return con;
    }

    public static boolean closeConnection() {
        boolean res = false;
        if (con != null) {
            try {
                con.close();
                con = null;
                res = true;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static String getDbName() {
        return dbName;
    }
}
