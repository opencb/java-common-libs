package org.opencb.commons.datastore.mongodb;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 17/01/16.
 */
public class MongoDBQueryUtilsTest {

    @Test
    public void testCreateFilter() throws Exception {
        List<String> stringList = Arrays.asList("a", "b");
        MongoDBQueryUtils.createFilter("", stringList, MongoDBQueryUtils.LogicalOperator.OR);

        List<Integer> integerList = Arrays.asList(1, 2);
        MongoDBQueryUtils.createFilter("", integerList, MongoDBQueryUtils.LogicalOperator.OR);

        List<Double> doubleList = Arrays.asList(1.0, 2.0);
        MongoDBQueryUtils.createFilter("", doubleList, MongoDBQueryUtils.LogicalOperator.OR);
    }
}