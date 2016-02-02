package org.opencb.commons.datastore.mongodb;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 17/01/16.
 */
public class MongoDBQueryUtils {

    private static final String REGEX_SEPARATOR = "(\\w+|\\^)";
    
    public enum LogicalOperator {
        AND,
        OR;
    }

    public enum ComparisonOperator {
        EQUAL,
        NOT_EQUAL,
        IN,
        NOT_IN,
        ALL,

        // String comparators
        EQUAL_IGNORE_CASE,
        START_WITH,
        END_WITH,
        REGEX,
        TEXT,

        // Numeric comparators
        GREATER_THAN,
        GREATER_THAN_EQUAL,
        LESS_THAN,
        LESS_THAN_EQUAL;
    }


    public static Bson createFilter(String mongoDbField, String queryParam, Query query) {
        return createFilter(mongoDbField, queryParam, query, QueryParam.Type.TEXT, ComparisonOperator.EQUAL, LogicalOperator.OR);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type) {
        return createFilter(mongoDbField, queryParam, query, type, ComparisonOperator.EQUAL, LogicalOperator.OR);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type, ComparisonOperator
            comparator) {
        return createFilter(mongoDbField, queryParam, query, type, comparator, LogicalOperator.OR);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type, ComparisonOperator
            comparator, LogicalOperator operator) {
        Bson filter = null;
        if (query != null && query.containsKey(queryParam)) {
            switch (type) {
                case TEXT:
                case TEXT_ARRAY:
                    filter = createFilter(mongoDbField, query.getAsStringList(queryParam), comparator, operator);
                    break;
                case INTEGER:
                case INTEGER_ARRAY:
                    filter = createFilter(mongoDbField, query.getAsIntegerList(queryParam), comparator, operator);
                    break;
                case DECIMAL:
                case DECIMAL_ARRAY:
                    filter = createFilter(mongoDbField, query.getAsDoubleList(queryParam), comparator, operator);
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

    public static Bson createAutoFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type) throws
            NumberFormatException {
        return createAutoFilter(mongoDbField, queryParam, query, type, LogicalOperator.OR);
    }

    public static Bson createAutoFilter(String mongoDbField, String queryParam, Query query, QueryParam.Type type, LogicalOperator operator)
    throws NumberFormatException {
        Bson filter = null;

        if (query != null && query.containsKey(queryParam)) {
            List<String> queryParamList = query.getAsStringList(queryParam);

            List<Bson> bsonList = new ArrayList<>(queryParamList.size());
            for (String queryItem : queryParamList) {
                String op = queryItem.substring(0, 2).replaceFirst(REGEX_SEPARATOR, "");
                ComparisonOperator comparator = getComparisonOperator(op);

                String queryValueString = queryItem.replaceFirst(op, "");
                switch (type) {
                    case TEXT:
                    case TEXT_ARRAY:
                        bsonList.add(createFilter(mongoDbField, queryValueString, comparator));
                        break;
                    case INTEGER:
                    case INTEGER_ARRAY:
                        bsonList.add(createFilter(mongoDbField, Integer.parseInt(queryValueString), comparator));
                        break;
                    case DECIMAL:
                    case DECIMAL_ARRAY:
                        bsonList.add(createFilter(mongoDbField, Double.parseDouble(queryValueString), comparator));
                        break;
                    case BOOLEAN:
                        bsonList.add(createFilter(mongoDbField, Boolean.parseBoolean(queryValueString), comparator));
                        break;
                    default:
                        break;
                }
            }

            if (bsonList.size() == 1) {
                filter = bsonList.get(0);
            } else {
                if (operator.equals(LogicalOperator.OR)) {
                    filter = Filters.or(bsonList);
                } else {
                    filter = Filters.and(bsonList);
                }
            }
        }
        return filter;
    }

    public static <T> Bson createFilter(String mongoDbField, T queryValue) {
        return createFilter(mongoDbField, queryValue, ComparisonOperator.EQUAL);
    }

    public static <T> Bson createFilter(String mongoDbField, T queryValue, ComparisonOperator comparator) {
        Bson filter = null;

        if (queryValue != null) {
            if (queryValue instanceof String) {
                switch (comparator) {
                    case EQUAL:
                        filter = Filters.eq(mongoDbField, queryValue);
                        break;
                    case NOT_EQUAL:
                        filter = Filters.ne(mongoDbField, queryValue);
                        break;
                    case EQUAL_IGNORE_CASE:
                        filter = Filters.regex(mongoDbField, queryValue.toString(), "i");
                        break;
                    case START_WITH:
                        filter = Filters.regex(mongoDbField, "^" + queryValue + "*");
                        break;
                    case END_WITH:
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
                    case EQUAL:
                        filter = Filters.eq(mongoDbField, queryValue);
                        break;
                    case NOT_EQUAL:
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
        return createFilter(mongoDbField, queryValues, ComparisonOperator.EQUAL, LogicalOperator.OR);
    }

    public static <T> Bson createFilter(String mongoDbField, List<T> queryValues, LogicalOperator operator) {
        return createFilter(mongoDbField, queryValues, ComparisonOperator.EQUAL, operator);
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


    public static ComparisonOperator getComparisonOperator(String op) {
        ComparisonOperator comparator;
        op = op.replaceFirst(REGEX_SEPARATOR, "");
        if (op.isEmpty()) {
            comparator = ComparisonOperator.EQUAL;
        } else {
            switch(op) {
                case "=":
                case "==":
                    comparator = ComparisonOperator.EQUAL;
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
                    comparator = ComparisonOperator.NOT_EQUAL;
                    break;
                case "~":
                case "~=":
                    comparator = ComparisonOperator.REGEX;
                    break;
                default:
                    comparator = ComparisonOperator.EQUAL;
                    break;
            }
        }
        return comparator;
    }

}
