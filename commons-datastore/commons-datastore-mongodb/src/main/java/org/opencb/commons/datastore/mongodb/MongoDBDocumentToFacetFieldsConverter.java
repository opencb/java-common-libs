package org.opencb.commons.datastore.mongodb;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.opencb.commons.datastore.core.ComplexTypeConverter;
import org.opencb.commons.datastore.core.FacetField;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter.TO_REPLACE_DOTS;
import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.Accumulator.*;
import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.*;

public class MongoDBDocumentToFacetFieldsConverter implements ComplexTypeConverter<List<FacetField>, Document> {

    private static final Map<String, String> MONTH_MAP = new HashMap<>();

    static {
        MONTH_MAP.put("01", "Jan");
        MONTH_MAP.put("02", "Feb");
        MONTH_MAP.put("03", "Mar");
        MONTH_MAP.put("04", "Apr");
        MONTH_MAP.put("05", "May");
        MONTH_MAP.put("06", "Jun");
        MONTH_MAP.put("07", "Jul");
        MONTH_MAP.put("08", "Aug");
        MONTH_MAP.put("09", "Sep");
        MONTH_MAP.put("10", "Oct");
        MONTH_MAP.put("11", "Nov");
        MONTH_MAP.put("12", "Dec");
    }

    @Override
    public List<FacetField> convertToDataModelType(Document document) {
        if (document == null || document.entrySet().size() == 0) {
            return Collections.emptyList();
        }

        String facetFieldName;
        List<FacetField> facets = new ArrayList<>();
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            String key = entry.getKey();
            List<Document> documentValues = (List<Document>) entry.getValue();
            if (key.endsWith(COUNTS_SUFFIX) || key.endsWith(FACET_ACC_SUFFIX) || key.endsWith(YEAR_SUFFIX) || key.endsWith(MONTH_SUFFIX)
                    || key.endsWith(DAY_SUFFIX)) {
                facetFieldName = key.split(SEPARATOR)[0].replace(TO_REPLACE_DOTS, ".");

                List<FacetField.Bucket> buckets = new ArrayList<>(documentValues.size());
                long total = 0;
                for (Document documentValue : documentValues) {

                    long counter = documentValue.getInteger(count.name());
                    String bucketValue = "";
                    Object internalIdValue = documentValue.get(INTERNAL_ID);
                    if (internalIdValue instanceof String) {
                        bucketValue = (String) internalIdValue;
                    } else if (internalIdValue instanceof Boolean
                            || internalIdValue instanceof Integer
                            || internalIdValue instanceof Long
                            || internalIdValue instanceof Double) {
                        bucketValue = internalIdValue.toString();
                    } else if (internalIdValue instanceof Document) {
                        bucketValue = StringUtils.join(((Document) internalIdValue).values(), SEPARATOR);
                        if (key.endsWith(COUNTS_SUFFIX)) {
                            facetFieldName = key.substring(0, key.indexOf(COUNTS_SUFFIX));
                        }
                    }

                    List<FacetField> bucketFacetFields = null;
                    if (key.endsWith(FACET_ACC_SUFFIX)) {
                        String[] split = key.split(SEPARATOR);
                        String name = split[2];
                        String aggregationName = split[1];
                        Double value;
                        if (documentValue.get(aggregationName) instanceof Integer) {
                            value = 1.0d * documentValue.getInteger(aggregationName);
                        } else if (documentValue.get(aggregationName) instanceof Long) {
                            value = 1.0d * documentValue.getLong(aggregationName);
                        } else {
                            value = documentValue.getDouble(aggregationName);
                        }
                        List<Double> aggregationValues = Collections.singletonList(value);
                        FacetField facetField = new FacetField(name.replace(TO_REPLACE_DOTS, "."), aggregationName, aggregationValues);
                        // Perhaps it’s redundant, as it is also set in the bucket
                        facetField.setCount(counter);
                        bucketFacetFields = Collections.singletonList(facetField);
                    }

                    buckets.add(new FacetField.Bucket(bucketValue, counter, bucketFacetFields));
                    total += counter;
                }
                FacetField facetField = new FacetField(facetFieldName, total, buckets);
                facetField.setAggregationName(count.name());
                if (key.endsWith(YEAR_SUFFIX) || key.endsWith(MONTH_SUFFIX) || key.endsWith(DAY_SUFFIX)) {
                    Collections.sort(buckets, Comparator.comparing(FacetField.Bucket::getValue));
                    if (key.endsWith(MONTH_SUFFIX)) {
                        for (FacetField.Bucket b : buckets) {
                            String[] split = b.getValue().split(SEPARATOR);
                            b.setValue(MONTH_MAP.get(split[1]) + " " + split[0]);
                        }
                    } else if (key.endsWith(DAY_SUFFIX)) {
                        for (FacetField.Bucket b : buckets) {
                            String[] split = b.getValue().split(SEPARATOR);
                            b.setValue(split[2] + " " + MONTH_MAP.get(split[1]) + " " + split[0]);
                        }
                    }
                    // Remove the data field and keep year, month and day
                    List<String> labels = new ArrayList<>(Arrays.asList(key.split(SEPARATOR)));
                    labels.remove(0);
                    facetField.setAggregationName(StringUtils.join(labels, SEPARATOR).toLowerCase(Locale.ROOT));
                }
                facets.add(facetField);
            } else if (key.endsWith(RANGES_SUFFIX)) {
                List<FacetField.Bucket> buckets = new ArrayList<>(documentValues.size());
                int total = 0;

                String[] split = key.split(SEPARATOR);
                double start = Double.parseDouble(split[1].replace(TO_REPLACE_DOTS, "."));
                double end = Double.parseDouble(split[2].replace(TO_REPLACE_DOTS, "."));
                double step = Double.parseDouble(split[3].replace(TO_REPLACE_DOTS, "."));

                int other = 0;
                for (double i = start; i <= end; i += step) {
                    int bucketCount = getBucketCountFromRanges(i, documentValues);
                    FacetField.Bucket bucket = new FacetField.Bucket(String.valueOf(roundToTwoSignificantDecimals(i)), bucketCount, null);
                    buckets.add(bucket);
                    total += bucketCount;
                }

                for (Document value : documentValues) {
                    if (value.get(INTERNAL_ID) instanceof String && OTHER.equals(value.getString(INTERNAL_ID))) {
                        other = value.getInteger(count.name());
                    }
                }
                facetFieldName = key.split(SEPARATOR)[0].replace(TO_REPLACE_DOTS, ".");
                if (other > 0) {
                    FacetField.Bucket bucket = new FacetField.Bucket("Other", other, null);
                    buckets.add(bucket);
                    total += bucket.getCount();
                }
                FacetField facetField = new FacetField(facetFieldName, total, buckets)
                        .setStart(start)
                        .setEnd(end)
                        .setStep(step);
                facets.add(facetField);
            } else {
                Document documentValue = ((List<Document>) entry.getValue()).get(0);
                MongoDBQueryUtils.Accumulator accumulator = getAccumulator(documentValue);
                switch (accumulator) {
                    case sum:
                    case avg:
                    case max:
                    case min:
                    case stdDevPop:
                    case stdDevSamp: {
                        List<Double> fieldValues = new ArrayList<>();
                        if (documentValue.get(accumulator.name()) instanceof Integer) {
                            fieldValues.add(1.0d * documentValue.getInteger(accumulator.name()));
                        } else if (documentValue.get(accumulator.name()) instanceof Long) {
                            fieldValues.add(1.0d * documentValue.getLong(accumulator.name()));
                        } else if (documentValue.get(accumulator.name()) instanceof List) {
                            List<Number> list = (List<Number>) documentValue.get(accumulator.name());
                            for (Number number : list) {
                                fieldValues.add(number.doubleValue());
                            }
                        } else {
                            fieldValues.add(documentValue.getDouble(accumulator.name()));
                        }
                        long count = 0;
                        if (documentValue.containsKey("count")) {
                            count = Long.valueOf(documentValue.getInteger("count"));
                        }
                        facetFieldName = documentValue.getString(INTERNAL_ID).replace(TO_REPLACE_DOTS, ".");
                        facets.add(new FacetField(facetFieldName, count, accumulator.name(), fieldValues));
                        break;
                    }
                    default: {
                        // Do nothing, exception is raised
                    }
                }
            }
        }
        return facets;
    }

    private MongoDBQueryUtils.Accumulator getAccumulator(Document document) {
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            try {
                MongoDBQueryUtils.Accumulator accumulator = MongoDBQueryUtils.Accumulator.valueOf(entry.getKey());
                return accumulator;
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
        }
        throw new IllegalArgumentException("No accumulators found in facet document: " + StringUtils.join(document.keySet(), ", ")
                + "Valid accumulator functions: " + StringUtils.join(Arrays.asList(count, sum, max, min, avg, stdDevPop, stdDevSamp), ","));
    }

    @Override
    public Document convertToStorageType(List<FacetField> facetFields) {
        throw new RuntimeException("Not yet implemented");
    }

    private static double roundToTwoSignificantDecimals(double value) {
        if (value == 0) {
            return 0;
        }

        BigDecimal bd = new BigDecimal(value);
        int integerDigits = bd.precision() - bd.scale();
        int scale = Math.max(0, 2 + integerDigits);
        return bd.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }


    private int getBucketCountFromRanges(double inputRange, List<Document> documentValues) {
        for (Document document : documentValues) {
            if (!OTHER.equals(document.get(INTERNAL_ID))) {
                if (inputRange == document.getDouble(INTERNAL_ID)) {
                    return document.getInteger(count.name());
                }
            }
        }
        return 0;
    }
}
