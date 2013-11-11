package org.opencb.commons.bioformats.commons;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.test.GenericTest;

import java.sql.Connection;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 11/5/13
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SqliteSingletonConnectionTest extends GenericTest {

    @Test
    public void testGetConnection() throws Exception {
        SqliteSingletonConnection singleton = new SqliteSingletonConnection("test.db");
        Connection c1 = SqliteSingletonConnection.getConnection();
        Connection c2 = SqliteSingletonConnection.getConnection();

        assertEquals(c1, c2);

    }
}
