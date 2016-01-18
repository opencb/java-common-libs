package org.opencb.commons.datastore.mongodb;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 17/01/16.
 */
public class MongoDBQueryUtils {

    public enum LogicalOperator {
        AND,
        OR;
    }

    public enum ParamType {
        STRING,
        INTEGER,
        DOUBLE;
    }

    public enum ComparisonOperator {
        EQUAL,
        NOT_EQUAL,
        IN,
        NOT_IN,
        ALL,
        AUTO,

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
        return createFilter(mongoDbField, queryParam, query, ParamType.STRING, ComparisonOperator.EQUAL, LogicalOperator.OR);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, LogicalOperator operator) {
        return createFilter(mongoDbField, queryParam, query, ParamType.STRING, ComparisonOperator.EQUAL, operator);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, ParamType type) {
        return createFilter(mongoDbField, queryParam, query, type, ComparisonOperator.EQUAL, LogicalOperator.OR);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, ParamType type, ComparisonOperator comparator) {
        return createFilter(mongoDbField, queryParam, query, type, comparator, LogicalOperator.OR);
    }

    public static Bson createFilter(String mongoDbField, String queryParam, Query query, ParamType type, ComparisonOperator comparator,
                                    LogicalOperator operator) {
        Bson filter = null;
        if (query != null && query.containsKey(queryParam)) {
            switch (type) {
                case STRING:
                    filter = createFilter(mongoDbField, query.getAsStringList(queryParam), comparator, operator);
                    break;
                case INTEGER:
                    filter = createFilter(mongoDbField, query.getAsIntegerList(queryParam), comparator, operator);
                    break;
                case DOUBLE:
                    filter = createFilter(mongoDbField, query.getAsDoubleList(queryParam), comparator, operator);
                    break;
                default:
                    break;
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

//            if (comparator.equals(COMPARISON_OPERATOR.AUTO)) {
//                String queryValueString = String.valueOf(queryValue);
//                String op = queryValueString.substring(0, 2);
//                op = op.replaceFirst("[a-zA-Z0-9]", "");
//                if (op.isEmpty()) {
//                    comparator = COMPARISON_OPERATOR.EQUAL;
//                } else {
//                    switch(op) {
//                        case "=":
//                        case "==":
//                            comparator = COMPARISON_OPERATOR.EQUAL;
//                            break;
//                        case ">":
//                            comparator = COMPARISON_OPERATOR.GREATER_THAN;
//                            break;
//                        case ">=":
//                            comparator = COMPARISON_OPERATOR.GREATER_THAN_EQUAL;
//                            break;
//                        case "<":
//                            comparator = COMPARISON_OPERATOR.LESS_THAN;
//                            break;
//                        case "<=":
//                            comparator = COMPARISON_OPERATOR.LESS_THAN_EQUAL;
//                            break;
//                        case "!=":
//                            comparator = COMPARISON_OPERATOR.NOT_EQUAL;
//                            break;
//                        case "~=":
//                            comparator = COMPARISON_OPERATOR.REGEX;
//                            break;
//                    }
//                    queryValue = queryValueString.replaceFirst(op, "");
//                }
//            }

            if (queryValue instanceof String) {
                switch (comparator) {
                    case EQUAL:
                        filter = Filters.eq(mongoDbField, queryValue);
                        break;
                    case NOT_EQUAL:
                        filter = Filters.ne(mongoDbField, queryValue);
                        break;
                    case EQUAL_IGNORE_CASE:
                        filter = Filters.regex(mongoDbField, "/" + queryValue + "/i");
                        break;
                    case START_WITH:
                        filter = Filters.regex(mongoDbField, "/^" + queryValue + "*/");
                        break;
                    case END_WITH:
                        filter = Filters.regex(mongoDbField, "/*" + queryValue + "$/");
                        break;
                    case REGEX:
                        filter = Filters.regex(mongoDbField, "/" + queryValue + "/");
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

    public static <T> Bson createFilter(String mongoDbField, List<T> queryValues, ComparisonOperator comparator,
                                        LogicalOperator operator) {
        Bson filter = null;

        if (queryValues != null && queryValues.size() > 0) {
            List<Bson> bsonList = new ArrayList<>(queryValues.size());

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
//        else {
//
//        }

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
}
