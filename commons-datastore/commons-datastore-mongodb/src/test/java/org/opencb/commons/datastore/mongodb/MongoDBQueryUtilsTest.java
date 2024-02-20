package org.opencb.commons.datastore.mongodb;

import org.junit.jupiter.api.Test;
import org.opencb.commons.datastore.core.QueryParam;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void testPatterns() throws Exception {
//        Matcher matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("=^hello");
//        String op = "";
//        if (matcher.find()) {
//            op = matcher.group(1);
//        }
//        assertEquals(MongoDBQueryUtils.ComparisonOperator.STARTS_WITH, MongoDBQueryUtils.getComparisonOperator(op, QueryParam.Type.STRING));

        Matcher matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("~/^hello/");
        String op = "";
        String op2 = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.REGEX, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("~/hello$/");
        op = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.REGEX, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

//        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("=$hello");
//        op = "";
//        if (matcher.find()) {
//            op = matcher.group(1);
//        }
//        assertEquals(MongoDBQueryUtils.ComparisonOperator.ENDS_WITH, MongoDBQueryUtils.getComparisonOperator(op, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("=hello");
        op = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.EQUALS, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("==hello");
        op = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.EQUALS, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("!hello");
        op = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.NOT_EQUALS, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("!=hello");
        op = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.NOT_EQUALS, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("~hello");
        op = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.REGEX, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("=~hello");
        op = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.REGEX, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("~/hello/");
        op = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.REGEX, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("=~/hello/");
        op = "";
        op2 = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.REGEX, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("~/hello/i");
        op = "";
        op2 = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.CASE_INSENSITIVE_REGEX, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));

        matcher = MongoDBQueryUtils.getPattern(QueryParam.Type.STRING).matcher("=~/hello/i");
        op = "";
        op2 = "";
        if (matcher.find()) {
            op = matcher.group(1);
            op2 = MongoDBQueryUtils.getOp2(op, matcher.group(2));
        }
        assertEquals(MongoDBQueryUtils.ComparisonOperator.CASE_INSENSITIVE_REGEX, MongoDBQueryUtils.getComparisonOperator(op, op2, QueryParam.Type.STRING));
    }
}