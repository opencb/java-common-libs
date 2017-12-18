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

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryParam;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by imedina on 17/01/16.
 */
public class MongoDBQueryUtils {

    @Deprecated
    private static final String REGEX_SEPARATOR = "(\\w+|\\^)";
    private static final Pattern OPERATION_STRING_PATTERN = Pattern.compile("^(!=?|!?=?~|==?|=?\\^|=?\\$)([^=<>~!]+.*)$");
    private static final Pattern OPERATION_NUMERIC_PATTERN = Pattern.compile("^(<=?|>=?|!=|!?=?~|==?)([^=<>~!]+.*)$");
    private static final Pattern OPERATION_BOOLEAN_PATTERN = Pattern.compile("^(!=|!?=?~|==?)([^=<>~!]+.*)$");
    private static final Pattern OPERATION_DATE_PATTERN = Pattern.compile("^(<=?|>=?|!=|!?=?~|=?=?)([0-9]+)(-?)([0-9]*)");

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
        EQUAL_IGNORE_CASE,
        STARTS_WITH,         // The regular expression will look for "=^" or "^" at the beginning.
        ENDS_WITH,            // The regular expression will look for "=$" or "$" at the beginning.
        REGEX,               // The regular expression will look for "=~" or "~" at the beginning.
        TEXT,

        // Numeric and Date comparators
        GREATER_THAN,
        GREATER_THAN_EQUAL,
        LESS_THAN,
        LESS_THAN_EQUAL,
        RANGES
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
                    filter = createFilter(mongoDbField, query.getAsLongList(queryParam, getLogicalSeparator(operator)), comparator,
                            operator);
                    break;
                case DECIMAL:
                case DECIMAL_ARRAY:
                    filter = createFilter(mongoDbField, query.getAsDoubleList(queryParam, getLogicalSeparator(operator)), comparator,
                            operator);
                    break;
                case BOOLEAN:
                    filter = createFilter(mongoDbField, query.getBoolean(queryParam), comparator);
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

    public static Bson createAutoFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type, LogicalOperator operator)
            throws NumberFormatException {

        List<String> queryParamList = query.getAsStringList(queryParam, getLogicalSeparator(operator));

        List<Bson> bsonList = new ArrayList<>(queryParamList.size());
        for (String queryItem : queryParamList) {
            Matcher matcher = getPattern(type).matcher(queryItem);
            String op = "";
            String queryValueString = queryItem;
            if (matcher.find()) {
                op = matcher.group(1);
                queryValueString = matcher.group(2);
            }
            ComparisonOperator comparator = getComparisonOperator(op, type);
            switch (type) {
                case STRING:
                case TEXT:
                case TEXT_ARRAY:
                    bsonList.add(createFilter(mongoDbField, queryValueString, comparator));
                    break;
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
                    bsonList.add(createFilter(mongoDbField, Boolean.parseBoolean(queryValueString), comparator));
                    break;
                case DATE:
                    List<String> dateList = new ArrayList<>();
                    dateList.add(queryValueString);
                    if (!matcher.group(3).isEmpty()) {
                        dateList.add(matcher.group(4));
                        comparator = ComparisonOperator.RANGES;
                    }
                    bsonList.add(createDateFilter(mongoDbField, dateList, comparator));
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
                    case EQUAL_IGNORE_CASE:
                        filter = Filters.regex(mongoDbField, queryValue.toString(), "i");
                        break;
                    case STARTS_WITH:
                        filter = Filters.regex(mongoDbField, "^" + queryValue + "*");
                        break;
                    case ENDS_WITH:
                        filter = Filters.regex(mongoDbField, "*" + queryValue + "$");
                        break;
                    case REGEX:
                        filter = Filters.regex(mongoDbField, queryValue.toString());
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
     * @return the Bson query.
     */
    public static Bson createDateFilter(String mongoDbField, List<String> dateValues, ComparisonOperator comparator) {
        Bson filter = null;

        if (ComparisonOperator.RANGES.equals(comparator)) {
            if (dateValues.size() == 2) {
                Date from = converStringToDate(dateValues.get(0));
                Date to = converStringToDate(dateValues.get(1));

                filter = new Document(mongoDbField, new Document()
                        .append("$gte", from)
                        .append("$lt", to)
                );
            }
        } else {
            Date date = converStringToDate(dateValues.get(0));
            switch (comparator) {
                case EQUALS:
                    filter = Filters.eq(mongoDbField, date);
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


    public static ComparisonOperator getComparisonOperator(String op, QueryParam.Type type) {
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
                        case "^":
//                        case "=^":
                            comparator = ComparisonOperator.STARTS_WITH;
                            break;
                        case "$":
//                        case "=$":
                            comparator = ComparisonOperator.ENDS_WITH;
                            break;
                        default:
                            throw new IllegalStateException("Unknown string query operation " + op);
                    }
                    break;
                case INTEGER:
                case INTEGER_ARRAY:
                case DOUBLE:
                case DECIMAL:
                case DECIMAL_ARRAY:
                case DATE:
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
            case INTEGER:
            case INTEGER_ARRAY:
            case DOUBLE:
            case DECIMAL:
            case DECIMAL_ARRAY:
                pattern = OPERATION_NUMERIC_PATTERN;
                break;
            case BOOLEAN:
                pattern = OPERATION_BOOLEAN_PATTERN;
                break;
            case DATE:
                pattern = OPERATION_DATE_PATTERN;
                break;
            default:
                break;
        }
        return pattern;
    }

    private static Date converStringToDate(String stringDate) {
        Calendar calendar = Calendar.getInstance();
        if (!stringDate.isEmpty()) {
            if (stringDate.length() >= 4) {
                calendar.set(Calendar.YEAR, Integer.parseInt(stringDate.substring(0, 4)));
            }
            if (stringDate.length() >= 6) {
                calendar.set(Calendar.MONTH, Integer.parseInt(stringDate.substring(4, 6)) - 1);
            }
            if (stringDate.length() >= 8) {
                calendar.set(Calendar.DATE, Integer.parseInt(stringDate.substring(6, 8)));
            }
            if (stringDate.length() >= 10) {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(stringDate.substring(8, 10)));
            }
            if (stringDate.length() >= 12) {
                calendar.set(Calendar.MINUTE, Integer.parseInt(stringDate.substring(10, 12)));
            }
            if (stringDate.length() >= 14) {
                calendar.set(Calendar.SECOND, Integer.parseInt(stringDate.substring(12, 14)));
            }
        }
        return calendar.getTime();
    }

}
