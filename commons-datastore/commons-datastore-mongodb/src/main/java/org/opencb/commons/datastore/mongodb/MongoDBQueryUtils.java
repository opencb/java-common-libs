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

package org.opencb.commons.datastore.mongodb;

import com.mongodb.client.model.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.Accumulator.*;

/**
 * Created by imedina on 17/01/16.
 */
public class MongoDBQueryUtils {

    @Deprecated
    private static final String REGEX_SEPARATOR = "(\\w+|\\^)";
    //    private static final Pattern OPERATION_STRING_PATTERN = Pattern.compile("^(!=?|!?=?~|==?|=?\\^|=?\\$)([^=<>~!]+.*)$");
    private static final Pattern OPERATION_STRING_PATTERN = Pattern.compile("^(!=?|!?=?~/?|==?)([^=<>~!]+.*)$");
    private static final Pattern OPERATION_NUMERIC_PATTERN = Pattern.compile("^(<=?|>=?|!=|!?=?~|==?)([^=<>~!]+.*)$");
    private static final Pattern OPERATION_BOOLEAN_PATTERN = Pattern.compile("^(!=|!?=?~|==?)([^=<>~!]+.*)$");
    private static final Pattern OPERATION_DATE_PATTERN = Pattern.compile("^(<=?|>=?|!=|!?=?~|=?=?)([0-9]+)(-?)([0-9]*)");

    private static final Pattern FUNC_ACCUMULATOR_PATTERN = Pattern.compile("([a-zA-Z]+)\\(([.a-zA-Z0-9]+)\\)");
    public static final String RANGE_MARK = "..";
    public static final String RANGE_MARK1 = "[";
    public static final String RANGE_MARK2 = "]";
    private static final String RANGE_SPLIT_MARK = "\\.\\.";
    private static final Pattern RANGE_START_PATTERN = Pattern.compile("([.a-zA-Z0-9]+)\\[([.0-9]+)");
    private static final Pattern RANGE_END_PATTERN = Pattern.compile("([.0-9]+)\\]:([.0-9]+)");
    public static final String INVALID_FORMAT_MSG = "Invalid format ";
    public static final String RANGE_FORMAT_MSG = " for range aggregation. Valid format is: field[start..end]:step, e.g: size[0..1000]:200";

    public static final String INTERNAL_ID = "_id";
    public static final String AND_SEPARATOR = "_and_";
    public static final String OTHER = "Other";

    public static final String COUNTS_SUFFIX = "Counts";
    public static final String SUM_SUFFIX = "Sum";
    public static final String AVG_SUFFIX = "Avg";
    public static final String MIN_SUFFIX = "Min";
    public static final String MAX_SUFFIX = "Max";
    public static final String STD_DEV_POP_SUFFIX = "StdDevPop";
    public static final String STD_DEV_SAMP_SUFFIX = "stdDevSamp";
    public static final String RANGES_SUFFIX = "Ranges";

    // TODO: Added on 10/08/2021 to deprecate STARTS_WITH and ENDS_WITH regex. They need to be done within '/'.
    @Deprecated
    private static final Pattern DEPRECATED_PATTERN = Pattern.compile("^(=?\\^|=?\\$)([^=/<>~!]+[.]*)$");

    public static final String OR = ",";
    public static final String AND = ";";
    public static final String IS = ":";

    public enum LogicalOperator {
        AND,
        OR;
    }

    public enum ComparisonOperator {
        EQUALS,
        NOT_EQUALS,
        IN,
        NOT_IN,
        ALL,

        // String comparators
        REGEX,                       // The regular expression will look for "=~" or "~" at the beginning.
        CASE_INSENSITIVE_REGEX,
        TEXT,

        // Numeric and Date comparators
        GREATER_THAN,
        GREATER_THAN_EQUAL,
        LESS_THAN,
        LESS_THAN_EQUAL,
        BETWEEN
    }

    public enum Accumulator {
        count,
        sum,
        avg,
        min,
        max,
        stdDevPop,
        stdDevSamp,
        bucket
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query) {
        return createFilter(mongoDbField, queryParam, query, QueryParam.Type.TEXT, ComparisonOperator.EQUALS, LogicalOperator.OR);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type) {
        return createFilter(mongoDbField, queryParam, query, type, ComparisonOperator.EQUALS, LogicalOperator.OR);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type,
                                    ComparisonOperator comparator) {
        return createFilter(mongoDbField, queryParam, query, type, comparator, LogicalOperator.OR);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type,
                                    ComparisonOperator comparator, LogicalOperator operator) {
        Bson filter = null;
        if (query != null && query.containsKey(queryParam)) {
            switch (type) {
                case TEXT:
                case TEXT_ARRAY:
                    filter = createFilter(mongoDbField, query.getAsStringList(queryParam, getLogicalSeparator(operator)), comparator,
                            operator);
                    break;
                case INTEGER:
                case INTEGER_ARRAY:
                case LONG:
                case LONG_ARRAY:
                    filter = createFilter(mongoDbField, query.getAsLongList(queryParam, getLogicalSeparator(operator)), comparator,
                            operator);
                    break;
                case DECIMAL:
                case DECIMAL_ARRAY:
                    filter = createFilter(mongoDbField, query.getAsDoubleList(queryParam, getLogicalSeparator(operator)), comparator,
                            operator);
                    break;
                case BOOLEAN:
                case BOOLEAN_ARRAY:
                    filter = createFilter(mongoDbField, query.getBoolean(queryParam), comparator);
                    break;
                case DATE:
                case TIMESTAMP:
                    filter = createDateFilter(mongoDbField, query.getAsStringList(queryParam), comparator, type);
                    break;
                default:
                    break;
            }
        }
        return filter;
    }

    private static String getLogicalSeparator(LogicalOperator operator) {
        return (operator != null && operator.equals(LogicalOperator.AND)) ? AND : OR;
    }

    public static Bson createAutoFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type)
            throws NumberFormatException {
        Bson filter = null;
        if (query != null && query.containsKey(queryParam)) {
            List<String> values = query.getAsStringList(queryParam);
            LogicalOperator operator = LogicalOperator.OR;
            if (values.size() == 1) {
                operator = checkOperator(values.get(0));
            }
            filter = createAutoFilter(mongoDbField, queryParam, query, type, operator);
        }
        return filter;
    }

    public static Bson createStringFilter(String mongoDbField, String queryParam, Query query) {
        return createAutoFilter(mongoDbField, queryParam, query, QueryParam.Type.STRING);
    }

    public static Bson createStringFilter(String mongoDbField, String queryParam, Query query, Pattern pattern) {
        Bson filter = null;
        if (query != null && query.containsKey(queryParam)) {
            List<String> values = query.getAsStringList(queryParam, pattern);
            LogicalOperator operator = LogicalOperator.OR;
            if (values.size() == 1) {
                operator = checkOperator(values.get(0));
            }
            filter = createAutoFilter(mongoDbField, queryParam, QueryParam.Type.STRING, operator, values);
        }
        return filter;
    }

    private static List<String> replaceDeprecatedPatterns(List<String> queryParamList) {
        List<String> replacedQueryParamList = new ArrayList<>(queryParamList.size());
        for (String queryItem : queryParamList) {
            Matcher matcher = DEPRECATED_PATTERN.matcher(queryItem);
            if (matcher.find()) {
                String operation = matcher.group(1);
                if (StringUtils.isNotEmpty(operation)) {
                    StringBuilder strBuilder = new StringBuilder();
                    strBuilder.append("=~/");
                    if ("=^".equals(operation) || "^".equals(operation)) {
                        strBuilder.append("^");
                    }
                    strBuilder.append(matcher.group(2));
                    if ("=$".equals(operation) || "$".equals(operation)) {
                        strBuilder.append("$");
                    }
                    strBuilder.append("/");
                    replacedQueryParamList.add(strBuilder.toString());
                } else {
                    replacedQueryParamList.add(queryItem);
                }
            } else {
                replacedQueryParamList.add(queryItem);
            }
        }
        return replacedQueryParamList;
    }

    protected static String getOp2(String op, String value) {
        String op2 = "";
        if (op.endsWith("/")) {
            if (value.endsWith("/")) {
                op2 = "/";
            } else if (value.endsWith("/i")) {
                op2 = "/i";
            } else {
                throw new IllegalStateException("Unknown regex query operation " + op + ". Missing "
                        + "trailing '/'");
            }
        }
        return op2;
    }

    public static Bson createAutoFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type, LogicalOperator operator)
            throws NumberFormatException {

        List<String> queryParamList = query.getAsStringList(queryParam, getLogicalSeparator(operator));
        return createAutoFilter(mongoDbField, queryParam, type, operator, queryParamList);
    }

    public static Bson createAutoFilter(String mongoDbField, String queryParam, QueryParam.Type type,
                                        LogicalOperator operator, List<String> values)
            throws NumberFormatException {
        values = replaceDeprecatedPatterns(values);

        if (LogicalOperator.OR.equals(operator)
                && queryParamsOperatorAlwaysMatchesOperator(type, values, ComparisonOperator.EQUALS)) {
            // It is better to perform a $in operation
            return Filters.in(mongoDbField, removeOperatorsFromQueryParamList(type, values));
        } else if (LogicalOperator.AND.equals(operator)
                && queryParamsOperatorAlwaysMatchesOperator(type, values, ComparisonOperator.NOT_EQUALS)) {
            // It is better to perform a $nin operation
            return Filters.nin(mongoDbField, removeOperatorsFromQueryParamList(type, values));
        } else {
            List<Bson> bsonList = new ArrayList<>(values.size());
            for (String queryItem : values) {
                Matcher matcher = getPattern(type).matcher(queryItem);
                String op = "";
                String op2 = "";
                String queryValueString = queryItem;
                if (matcher.find()) {
                    op = matcher.group(1);
                    queryValueString = matcher.group(2);
                    op2 = getOp2(op, queryValueString);
                    if (StringUtils.isNotEmpty(op2)) {
                        // Remove the last part that was added as op2
                        queryValueString = queryValueString.substring(0, queryValueString.length() - op2.length());
                    }
                }
                ComparisonOperator comparator = getComparisonOperator(op, op2, type);
                switch (type) {
                    case STRING:
                    case TEXT:
                    case TEXT_ARRAY:
                        bsonList.add(createFilter(mongoDbField, queryValueString, comparator));
                        break;
                    case LONG:
                    case LONG_ARRAY:
                    case INTEGER:
                    case INTEGER_ARRAY:
                        bsonList.add(createFilter(mongoDbField, Long.parseLong(queryValueString), comparator));
                        break;
                    case DOUBLE:
                    case DECIMAL:
                    case DECIMAL_ARRAY:
                        bsonList.add(createFilter(mongoDbField, Double.parseDouble(queryValueString), comparator));
                        break;
                    case BOOLEAN:
                    case BOOLEAN_ARRAY:
                        bsonList.add(createFilter(mongoDbField, Boolean.parseBoolean(queryValueString), comparator));
                        break;
                    case DATE:
                    case TIMESTAMP:
                        List<String> dateList = new ArrayList<>();
                        dateList.add(queryValueString);
                        if (!matcher.group(3).isEmpty()) {
                            dateList.add(matcher.group(4));
                            comparator = ComparisonOperator.BETWEEN;
                        }
                        bsonList.add(createDateFilter(mongoDbField, dateList, comparator, type));
                        break;
                    default:
                        break;
                }
            }

            Bson filter;
            if (bsonList.size() == 0) {
                filter = Filters.size(queryParam, 0);
            } else if (bsonList.size() == 1) {
                filter = bsonList.get(0);
            } else {
                if (operator.equals(LogicalOperator.OR)) {
                    filter = Filters.or(bsonList);
                } else {
                    filter = Filters.and(bsonList);
                }
            }

            return filter;
        }
    }

    /**
     * Auxiliary method to check if the operator of each of the values in the queryParamList matches the operator passed.
     *
     * @param type QueryParam type.
     * @param queryParamList List of values.
     * @param operator Operator to be checked.
     * @return boolean indicating whether the list of values have always the same operator or not.
     */
    private static boolean queryParamsOperatorAlwaysMatchesOperator(QueryParam.Type type, List<String> queryParamList,
                                                                    ComparisonOperator operator) {
        for (String queryItem : queryParamList) {
            Matcher matcher = getPattern(type).matcher(queryItem);
            String op = "";
            String op2 = "";
            if (matcher.find()) {
                op = matcher.group(1);
                op2 = getOp2(op, matcher.group(2));
            }
            if (operator != getComparisonOperator(op, op2, type)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Removes any operators present in the queryParamList and gets a list of the values parsed to the corresponding data type.
     *
     * @param type QueryParam type.
     * @param queryParamList List of values.
     * @return a list of the values parsed to the corresponding data type.
     */
    private static List<Object> removeOperatorsFromQueryParamList(QueryParam.Type type, List<String> queryParamList) {
        List<Object> newQueryParamList = new ArrayList<>();

        for (String queryItem : queryParamList) {
            Matcher matcher = getPattern(type).matcher(queryItem);
            String queryValueString = queryItem;
            if (matcher.find()) {
                queryValueString = matcher.group(2);
            }
            switch (type) {
                case STRING:
                case TEXT:
                case TEXT_ARRAY:
                    newQueryParamList.add(queryValueString);
                    break;
                case LONG:
                case LONG_ARRAY:
                case INTEGER:
                case INTEGER_ARRAY:
                    newQueryParamList.add(Long.parseLong(queryValueString));
                    break;
                case DOUBLE:
                case DECIMAL:
                case DECIMAL_ARRAY:
                    newQueryParamList.add(Double.parseDouble(queryValueString));
                    break;
                case BOOLEAN:
                case BOOLEAN_ARRAY:
                    newQueryParamList.add(Boolean.parseBoolean(queryValueString));
                    break;
                default:
                    break;
            }
        }

        return newQueryParamList;
    }


    public static <T> Bson createFilter(String mongoDbField, T queryValue) {
        return createFilter(mongoDbField, queryValue, ComparisonOperator.EQUALS);
    }

    public static <T> Bson createFilter(String mongoDbField, T queryValue, ComparisonOperator comparator) {
        Bson filter = null;

        if (queryValue != null) {
            if (queryValue instanceof String) {
                switch (comparator) {
                    case EQUALS:
                        filter = Filters.eq(mongoDbField, queryValue);
                        break;
                    case NOT_EQUALS:
                        filter = Filters.ne(mongoDbField, queryValue);
                        break;
                    case REGEX:
                        filter = Filters.regex(mongoDbField, queryValue.toString());
                        break;
                    case CASE_INSENSITIVE_REGEX:
                        filter = Filters.regex(mongoDbField, queryValue.toString(), "i");
                        break;
                    case TEXT:
                        filter = Filters.text(String.valueOf(queryValue));
                        break;
                    default:
                        break;
                }
            } else {
                switch (comparator) {
                    case EQUALS:
                        filter = Filters.eq(mongoDbField, queryValue);
                        break;
                    case NOT_EQUALS:
                        filter = Filters.ne(mongoDbField, queryValue);
                        break;
                    case GREATER_THAN:
                        filter = Filters.gt(mongoDbField, queryValue);
                        break;
                    case GREATER_THAN_EQUAL:
                        filter = Filters.gte(mongoDbField, queryValue);
                        break;
                    case LESS_THAN:
                        filter = Filters.lt(mongoDbField, queryValue);
                        break;
                    case LESS_THAN_EQUAL:
                        filter = Filters.lte(mongoDbField, queryValue);
                        break;
                    default:
                        break;
                }
            }
        }
        return filter;
    }

    public static <T> Bson createFilter(String mongoDbField, List<T> queryValues) {
        return createFilter(mongoDbField, queryValues, ComparisonOperator.EQUALS, LogicalOperator.OR);
    }

    public static <T> Bson createFilter(String mongoDbField, List<T> queryValues, LogicalOperator operator) {
        return createFilter(mongoDbField, queryValues, ComparisonOperator.EQUALS, operator);
    }

    public static <T> Bson createFilter(String mongoDbField, List<T> queryValues, ComparisonOperator comparator) {
        return createFilter(mongoDbField, queryValues, comparator, LogicalOperator.OR);
    }

    public static <T> Bson createFilter(String mongoDbField, List<T> queryValues, ComparisonOperator comparator, LogicalOperator operator) {
        Bson filter = null;

        if (queryValues != null && queryValues.size() > 0) {

            if (comparator.equals(ComparisonOperator.IN) || comparator.equals(ComparisonOperator.NOT_IN)
                    || comparator.equals(ComparisonOperator.ALL)) {
                switch (comparator) {
                    case IN:
                        filter = Filters.in(mongoDbField, queryValues);
                        break;
                    case NOT_IN:
                        filter = Filters.nin(mongoDbField, queryValues);
                        break;
                    case ALL:
                        filter = Filters.all(mongoDbField, queryValues);
                        break;
                    default:
                        break;
                }
            } else {
                // If there is only on element in the array then it does not make sense to create an OR or AND filter
                if (queryValues.size() == 1) {
                    filter = createFilter(mongoDbField, queryValues.get(0), comparator);
                } else {
                    List<Bson> bsonList = new ArrayList<>(queryValues.size());
                    for (T queryItem : queryValues) {
                        Bson filter1 = createFilter(mongoDbField, queryItem, comparator);
                        if (filter1 != null) {
                            bsonList.add(filter1);
                        }
                    }

                    if (operator.equals(LogicalOperator.OR)) {
                        filter = Filters.or(bsonList);
                    } else {
                        filter = Filters.and(bsonList);
                    }
                }

            }
        }

        return filter;
    }

    /**
     * Generates a date filter.
     *
     * @param mongoDbField Mongo field.
     * @param dateValues List of 1 or 2 strings (dates). Only one will be expected when something like the following is passed:
     *                   =20171210, 20171210, >=20171210, >20171210, <20171210, <=20171210
     *                   When 2 strings are passed, we will expect it to be a range such as: 20171201-20171210
     * @param comparator Comparator value.
     * @param type Type of parameter. Expecting one of {@link QueryParam.Type#DATE} or {@link QueryParam.Type#TIMESTAMP}
     * @return the Bson query.
     */
    protected static Bson createDateFilter(String mongoDbField, List<String> dateValues, ComparisonOperator comparator,
                                           QueryParam.Type type) {
        Bson filter = null;

        Object date = null;
        if (QueryParam.Type.DATE.equals(type)) {
            date = convertStringToDate(dateValues.get(0));
        } else if (QueryParam.Type.TIMESTAMP.equals(type)) {
            date = convertStringToDate(dateValues.get(0)).getTime();
        }

        if (date != null) {
//            if (QueryParam.Type.DATE.equals(type) && comparator == ComparisonOperator.EQUALS) {
//                Date tmpDate = (Date) date;
//                if (dateValues.get(0).length() == 4) {
//                    // Only year provided
//                    tmpDate.
//                } else if (dateValues.get(0).length() == 6) {
//                    // Year and month provided
//                } else if (dateValues.get(0).length() == 8) {
//                    // Year, month and day provided
//                }
//            }
            switch (comparator) {
                case BETWEEN:
                    if (dateValues.size() == 2) {
                        Date to = convertStringToDate(dateValues.get(1));

                        if (QueryParam.Type.DATE.equals(type)) {
                            filter = new Document(mongoDbField, new Document()
                                    .append("$gte", date)
                                    .append("$lt", to));
                        } else if (QueryParam.Type.TIMESTAMP.equals(type)) {
                            filter = new Document(mongoDbField, new Document()
                                    .append("$gte", date)
                                    .append("$lt", to.getTime()));
                        }
                    }
                    break;
                case EQUALS:
                    if (dateValues.get(0).length() <= 8 && QueryParam.Type.DATE.equals(type)) {
                        List<Date> dates = getDateRange(dateValues.get(0));
                        // We will apply a range of dates. Example: 2020 -> It will query for the whole 2020 year
                        // 202002 -> It will query for everything in February 2020
                        // 20200201 -> It will query for everything of the 1st of February 2020
                        filter = new Document(mongoDbField, new Document()
                                .append("$gte", dates.get(0))
                                .append("$lt", dates.get(1)));
                    } else {
                        filter = Filters.eq(mongoDbField, date);
                    }
                    break;
                case GREATER_THAN:
                    filter = Filters.gt(mongoDbField, date);
                    break;
                case GREATER_THAN_EQUAL:
                    filter = Filters.gte(mongoDbField, date);
                    break;
                case LESS_THAN:
                    filter = Filters.lt(mongoDbField, date);
                    break;
                case LESS_THAN_EQUAL:
                    filter = Filters.lte(mongoDbField, date);
                    break;
                default:
                    break;
            }
        }

        return filter;
    }

    /**
     * Checks that the filter value list contains only one type of operations.
     *
     * @param value List of values to check
     * @return  The used operator. Null if no operator is used.
     * @throws IllegalArgumentException if the list contains different operators.
     */
    public static LogicalOperator checkOperator(String value) throws IllegalArgumentException {
        boolean containsOr = value.contains(OR);
        boolean containsAnd = value.contains(AND);
        if (containsAnd && containsOr) {
            throw new IllegalArgumentException("Cannot merge AND and OR operators in the same query filter.");
        } else if (containsAnd && !containsOr) {
            return LogicalOperator.AND;
        } else if (containsOr && !containsAnd) {
            return LogicalOperator.OR;
        } else {    // !containsOr && !containsAnd
            return null;
        }
    }

    public static List<Bson> createGroupBy(Bson query, String groupByField, String idField, boolean count) {
        if (groupByField == null || groupByField.isEmpty()) {
            return new ArrayList<>();
        }

        if (groupByField.contains(",")) {
            // call to multiple createGroupBy if commas are present
            return createGroupBy(query, Arrays.asList(groupByField.split(",")), idField, count);
        } else {
            Bson match = Aggregates.match(query);
            Bson project = Aggregates.project(Projections.include(groupByField, idField));
            Bson group;
            if (count) {
                group = Aggregates.group("$" + groupByField, Accumulators.sum("count", 1));
            } else {
                group = Aggregates.group("$" + groupByField, Accumulators.addToSet("features", "$" + idField));
            }
            return Arrays.asList(match, project, group);
        }
    }

    public static List<Bson> createGroupBy(Bson query, List<String> groupByField, String idField, boolean count) {
        if (groupByField == null || groupByField.isEmpty()) {
            return new ArrayList<>();
        }

        if (groupByField.size() == 1) {
            // if only one field then we call to simple createGroupBy
            return createGroupBy(query, groupByField.get(0), idField, count);
        } else {
            Bson match = Aggregates.match(query);

            // add all group-by fields to the projection together with the aggregation field name
            List<String> groupByFields = new ArrayList<>(groupByField);
            groupByFields.add(idField);
            Bson project = Aggregates.project(Projections.include(groupByFields));

            // _id document creation to have the multiple id
            Document id = new Document();
            for (String s : groupByField) {
                id.append(s, "$" + s);
            }
            Bson group;
            if (count) {
                group = Aggregates.group(id, Accumulators.sum("count", 1));
            } else {
                group = Aggregates.group(id, Accumulators.addToSet("features", "$" + idField));
            }
            return Arrays.asList(match, project, group);
        }
    }

    public static List<Bson> createFacet(Bson query, String facetField) {
        // Sanity check
        if (facetField == null || StringUtils.isEmpty(facetField.trim())) {
            return new ArrayList<>();
        }
        String cleanFacetField = facetField.replace(" ", "");

        // Multiple facets separated by ;
        ArrayList<String> facetFields = new ArrayList<>(Arrays.asList(cleanFacetField.split(";")));
        return createFacet(query, facetFields);
    }

    private static List<Bson> createFacet(Bson query, List<String> facetFields) {
        Set<String> includeFields = new HashSet<>();
        List<Double> boundaries = new ArrayList<>();
        List<Facet> facets = new ArrayList<>();

        for (String facetField : facetFields) {
            Facet facet;

            if (facetField.contains(",")) {
                // Facet combining fields (i.e., AND logical)
                Document id = new Document();
                for (String field : facetField.split(",")) {
                    id.append(field, "$" + field);
                    includeFields.add(field);
                }
                facet = new Facet(facetField.replace(",", AND_SEPARATOR) + COUNTS_SUFFIX,
                        Arrays.asList(Aggregates.group(id, Accumulators.sum(count.name(), 1))));
            } else {
                // Facet with accumulators (count, avg, min, max,...) or range (bucket)
                Accumulator accumulator;
                String field;
                Matcher matcher = FUNC_ACCUMULATOR_PATTERN.matcher(facetField);
                if (matcher.matches()) {
                    try {
                        accumulator = Accumulator.valueOf(matcher.group(1));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid accumulator function '" + matcher.group(1) + "'. Valid accumulator"
                                + " functions: " + StringUtils.join(Arrays.asList(count, sum, max, min, avg, stdDevPop, stdDevSamp), ", "));
                    }
                    field = matcher.group(2);
                } else {
                    if (facetField.contains(RANGE_MARK) || facetField.contains(RANGE_MARK1) || facetField.contains(RANGE_MARK2)) {
                        String[] split = facetField.split(RANGE_SPLIT_MARK);
                        if (split.length == 2) {
                            Matcher matcher1 = RANGE_START_PATTERN.matcher(split[0]);
                            Matcher matcher2 = RANGE_END_PATTERN.matcher(split[1]);
                            if (matcher1.matches() && matcher2.matches()) {
                                accumulator = bucket;
                                field = matcher1.group(1);
                                double start = Double.parseDouble(matcher1.group(2));
                                double end = Double.parseDouble(matcher2.group(1));
                                double step = Double.parseDouble(matcher2.group(2));
                                for (double i = start; i <= end; i += step) {
                                    boundaries.add(i);
                                }
                            } else {
                                throw new IllegalArgumentException(INVALID_FORMAT_MSG + facetField + RANGE_FORMAT_MSG);
                            }
                        } else {
                            throw new IllegalArgumentException(INVALID_FORMAT_MSG + facetField + RANGE_FORMAT_MSG);
                        }
                    } else {
                        accumulator = count;
                        field = facetField;
                    }
                }
                includeFields.add(field);

                // Get MongoDB facet
                facet = getMongoDBFacet(field, accumulator, boundaries);
            }
            if (facet != null) {
                facets.add(facet);
            }
        }

        // Build MongoDB pipeline for facets
        Bson match = Aggregates.match(query);
        Bson project = Aggregates.project(Projections.include(new ArrayList<>(includeFields)));
        // Dot notation management for facets
        Document aggregates = GenericDocumentComplexConverter.replaceDots(Document.parse(Aggregates.facet(facets).toBsonDocument()
                .toJson()));

        return Arrays.asList(match, project, aggregates);
    }

    private static Facet getMongoDBFacet(String field, Accumulator accumulator, List<Double> boundaries) {
        String id = "$" + field;

        Facet facet;
        switch (accumulator) {
            case count: {
                facet = new Facet(field + COUNTS_SUFFIX, Arrays.asList(Aggregates.group(id, Accumulators.sum(count.name(), 1))));
                break;
            }
            case sum: {
                facet = new Facet(field + SUM_SUFFIX, Arrays.asList(Aggregates.group(field, Accumulators.sum(sum.name(), id))));
                break;
            }
            case avg: {
                facet = new Facet(field + AVG_SUFFIX, Arrays.asList(Aggregates.group(field, Accumulators.avg(avg.name(), id))));
                break;
            }
            case min: {
                facet = new Facet(field + MIN_SUFFIX, Arrays.asList(Aggregates.group(field, Accumulators.min(min.name(), id))));
                break;
            }
            case max: {
                facet = new Facet(field + MAX_SUFFIX, Arrays.asList(Aggregates.group(field, Accumulators.max(max.name(), id))));
                break;
            }
            case stdDevPop: {
                facet = new Facet(field + STD_DEV_POP_SUFFIX, Arrays.asList(Aggregates.group(field,
                        Accumulators.stdDevPop(stdDevPop.name(), id))));
                break;
            }
            case stdDevSamp: {
                facet = new Facet(field + STD_DEV_SAMP_SUFFIX, Arrays.asList(Aggregates.group(field,
                        Accumulators.stdDevSamp(stdDevSamp.name(), id))));
                break;
            }
            case bucket: {
                facet = new Facet(field + RANGES_SUFFIX, Aggregates.bucket(id, boundaries,
                        new BucketOptions()
                                .defaultBucket(OTHER)
                                .output(new BsonField(count.name(), new BsonDocument("$sum", new BsonInt32(1))))));
                break;
            }
            default: {
                facet = null;
                break;
            }
        }
        return facet;
    }

    public static void parseQueryOptions(List<Bson> operations, QueryOptions options) {
        if (options != null) {
            Bson projection = getProjection(options);
            if (projection != null) {
                operations.add(projection);
            }
            Bson skip = getSkip(options);
            if (skip != null) {
                operations.add(skip);
            }
            Bson limit = getLimit(options);
            if (limit != null) {
                operations.add(limit);
            }
            Bson sort = getSort(options);
            if (sort != null) {
                operations.add(sort);
            }
        }
    }

    public static Bson getSort(QueryOptions options) {
        Object sortObject = options.get(QueryOptions.SORT);
        if (sortObject != null) {
            if (sortObject instanceof Bson) {
                return Aggregates.sort((Bson) sortObject);
            } else if (sortObject instanceof String) {
                String order = options.getString(QueryOptions.ORDER, "DESC");
                if (order.equalsIgnoreCase(QueryOptions.ASCENDING) || order.equalsIgnoreCase("ASC") || order.equals("1")) {
                    return Aggregates.sort(Sorts.ascending((String) sortObject));
                } else {
                    return Aggregates.sort(Sorts.descending((String) sortObject));
                }
            }
        }
        return null;
    }

    public static Bson getLimit(QueryOptions options) {
        if (options.getInt(QueryOptions.LIMIT) > 0) {
            return Aggregates.limit(options.getInt(QueryOptions.LIMIT));
        }
        return null;
    }

    public static Bson getSkip(QueryOptions options) {
        if (options.getInt(QueryOptions.SKIP) > 0) {
            return Aggregates.skip(options.getInt(QueryOptions.SKIP));
        }
        return null;
    }

    public static Bson getProjection(QueryOptions options) {
        Bson projection = getProjection(null, options);
        return projection != null ? Aggregates.project(projection) : null;
    }

    protected static Bson getProjection(Bson projection, QueryOptions options) {
        Bson projectionResult = null;
        List<Bson> projections = new ArrayList<>();

        // It is too risky to merge projections, if projection already exists we return it as it is, otherwise we create a new one.
        if (projection != null) {
//            projections.add(projection);
            return projection;
        }

        if (options != null) {
            // Select which fields are excluded and included in the query
            // Read and process 'include'/'exclude'/'elemMatch' field from 'options' object

            Bson include = null;
            if (options.containsKey(QueryOptions.INCLUDE)) {
                Object includeObject = options.get(QueryOptions.INCLUDE);
                if (includeObject != null) {
                    if (includeObject instanceof Bson) {
                        include = (Bson) includeObject;
                    } else {
                        List<String> includeStringList = options.getAsStringList(QueryOptions.INCLUDE, ",");
                        if (includeStringList != null && includeStringList.size() > 0) {
                            include = Projections.include(includeStringList);
                        }
                    }
                }
            }

            Bson exclude = null;
            boolean excludeId = false;
            if (options.containsKey(QueryOptions.EXCLUDE)) {
                Object excludeObject = options.get(QueryOptions.EXCLUDE);
                if (excludeObject != null) {
                    if (excludeObject instanceof Bson) {
                        exclude = (Bson) excludeObject;
                    } else {
                        List<String> excludeStringList = options.getAsStringList(QueryOptions.EXCLUDE, ",");
                        if (excludeStringList != null && excludeStringList.size() > 0) {
                            exclude = Projections.exclude(excludeStringList);
                            excludeId = excludeStringList.contains("_id");
                        }
                    }
                }
            }

            // If both include and exclude exist we only add include
            if (include != null) {
                projections.add(include);
                // MongoDB allows to exclude _id when include is present
                if (excludeId) {
                    projections.add(Projections.excludeId());
                }
            } else {
                if (exclude != null) {
                    projections.add(exclude);
                }
            }


            if (options.containsKey(MongoDBCollection.ELEM_MATCH)) {
                Object elemMatch = options.get(MongoDBCollection.ELEM_MATCH);
                if (elemMatch != null && elemMatch instanceof Bson) {
                    projections.add((Bson) elemMatch);
                }
            }

//            List<String> includeStringList = options.getAsStringList(MongoDBCollection.INCLUDE, ",");
//            if (includeStringList != null && includeStringList.size() > 0) {
//                projections.add(Projections.include(includeStringList));
////                for (Object field : includeStringList) {
////                    projection.put(field.toString(), 1);
////                }
//            } else {
//                List<String> excludeStringList = options.getAsStringList(MongoDBCollection.EXCLUDE, ",");
//                if (excludeStringList != null && excludeStringList.size() > 0) {
//                    projections.add(Projections.exclude(excludeStringList));
////                    for (Object field : excludeStringList) {
////                        projection.put(field.toString(), 0);
////                    }
//                }
//            }
        }

        if (projections.size() > 0) {
            projectionResult = Projections.fields(projections);
        }

        return projectionResult;
    }

    public static ComparisonOperator getComparisonOperator(String op, String op2, QueryParam.Type type) {
        ComparisonOperator comparator = null;
        if (op != null && op.isEmpty()) {
            comparator = ComparisonOperator.EQUALS;
        } else {
            switch (type) {
                case STRING:
                case TEXT:
                case TEXT_ARRAY:
                    switch(op) {
                        case "=":
                        case "==":
                            comparator = ComparisonOperator.EQUALS;
                            break;
                        case "!":
                        case "!=":
                            comparator = ComparisonOperator.NOT_EQUALS;
                            break;
                        case "~":
                        case "=~":
                            comparator = ComparisonOperator.REGEX;
                            break;
                        case "=~/":
                        case "~/":
                            if (StringUtils.isEmpty(op2)) {
                                throw new IllegalStateException("Unknown regex query operation " + op + ". Missing "
                                        + "trailing '/'");
                            }
                            if ("/".equals(op2)) {
                                comparator = ComparisonOperator.REGEX;
                            } else if ("/i".equals(op2)) {
                                comparator = ComparisonOperator.CASE_INSENSITIVE_REGEX;
                            } else {
                                throw new IllegalStateException("Unknown regex query operation " + op + ". Unexpected "
                                        + "trailing '" + op2 + "'");
                            }
                            break;
//                        case "^":
////                        case "=^":
//                            comparator = ComparisonOperator.STARTS_WITH;
//                            break;
//                        case "$":
////                        case "=$":
//                            comparator = ComparisonOperator.ENDS_WITH;
//                            break;
                        default:
                            throw new IllegalStateException("Unknown string query operation " + op);
                    }
                    break;
                case LONG:
                case LONG_ARRAY:
                case INTEGER:
                case INTEGER_ARRAY:
                case DOUBLE:
                case DECIMAL:
                case DECIMAL_ARRAY:
                case DATE:
                case TIMESTAMP:
                    switch(op) {
                        case "=":
                        case "==":
                            comparator = ComparisonOperator.EQUALS;
                            break;
                        case ">":
                            comparator = ComparisonOperator.GREATER_THAN;
                            break;
                        case ">=":
                            comparator = ComparisonOperator.GREATER_THAN_EQUAL;
                            break;
                        case "<":
                            comparator = ComparisonOperator.LESS_THAN;
                            break;
                        case "<=":
                            comparator = ComparisonOperator.LESS_THAN_EQUAL;
                            break;
                        case "!=":
                            comparator = ComparisonOperator.NOT_EQUALS;
                            break;
                        default:
                            throw new IllegalStateException("Unknown numerical query operation " + op);
                    }
                    break;
                case BOOLEAN:
                case BOOLEAN_ARRAY:
                    switch(op) {
                        case "=":
                        case "==":
                            comparator = ComparisonOperator.EQUALS;
                            break;
                        case "!=":
                            comparator = ComparisonOperator.NOT_EQUALS;
                            break;
                        default:
                            throw new IllegalStateException("Unknown boolean query operation " + op);
                    }
                    break;
                default:
                    break;
            }
        }
        return comparator;
    }

    protected static Pattern getPattern(QueryParam.Type type) {
        Pattern pattern = null;
        switch (type) {
            case STRING:
            case TEXT:
            case TEXT_ARRAY:
                pattern = OPERATION_STRING_PATTERN;
                break;
            case LONG:
            case LONG_ARRAY:
            case INTEGER:
            case INTEGER_ARRAY:
            case DOUBLE:
            case DECIMAL:
            case DECIMAL_ARRAY:
                pattern = OPERATION_NUMERIC_PATTERN;
                break;
            case BOOLEAN:
            case BOOLEAN_ARRAY:
                pattern = OPERATION_BOOLEAN_PATTERN;
                break;
            case DATE:
            case TIMESTAMP:
                pattern = OPERATION_DATE_PATTERN;
                break;
            default:
                break;
        }
        return pattern;
    }

    public static Date convertStringToDate(String stringDate) {
        if (stringDate.length() == 4) {
            stringDate = stringDate + "0101";
        } else if (stringDate.length() == 6) {
            stringDate = stringDate + "01";
        }
        String myDate = String.format("%-14s", stringDate).replace(" ", "0");
        LocalDateTime localDateTime = LocalDateTime.parse(myDate, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // We convert it to date because it is the type used by mongo
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    protected static List<Date> getDateRange(String stringDate) {
        LocalDateTime from;
        LocalDateTime to;
        if (stringDate.length() == 4) {
            // Only year provided
            int year = Integer.parseInt(stringDate);

            LocalDateTime time = LocalDateTime.of(year, 1, 1, 0, 0);
            from = time.minusSeconds(1);
            to = time.plusYears(1);
        } else if (stringDate.length() == 6) {
            // Year and month provided
            int year = Integer.parseInt(stringDate.substring(0, 4));
            int month = Integer.parseInt(stringDate.substring(4, 6));

            LocalDateTime time = LocalDateTime.of(year, month, 1, 0, 0);
            from = time.minusSeconds(1);
            to = time.plusMonths(1);
        } else if (stringDate.length() == 8) {
            // Year, month and day provided
            int year = Integer.parseInt(stringDate.substring(0, 4));
            int month = Integer.parseInt(stringDate.substring(4, 6));
            int day =  Integer.parseInt(stringDate.substring(6, 8));

            LocalDateTime time = LocalDateTime.of(year, month, day, 0, 0);
            from = time.minusSeconds(1);
            to = time.plusDays(1);
        } else {
            return null;
        }
        return Arrays.asList(Date.from(from.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(to.atZone(ZoneId.systemDefault()).toInstant()));
    }

}
