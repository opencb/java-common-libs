package org.opencb.commons.datastore.mongodb;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.opencb.commons.datastore.core.ComplexTypeConverter;
import org.opencb.commons.datastore.core.FacetField;

import java.util.*;

import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.*;
import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.Accumulator.*;

public class MongoDBDocumentToFacetFieldsConverter implements ComplexTypeConverter<List<FacetField>, Document> {

    @Override
    public List<FacetField> convertToDataModelType(Document document) {
        if (document == null || document.entrySet().size() == 0) {
            return Collections.emptyList();
        }

        List<FacetField> facets = new ArrayList<>();
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            String key = entry.getKey();
            List<Document> documentValues = (List<Document>) entry.getValue();
            if (key.endsWith(COUNTS_SUFFIX) || key.endsWith(FACET_ACC_SUFFIX)) {
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
                            || internalIdValue instanceof Double) {
                        bucketValue = internalIdValue.toString();
                    } else if (internalIdValue instanceof Document) {
                        bucketValue = StringUtils.join(((Document) internalIdValue).values(), AND_SEPARATOR);
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
                        FacetField facetField = new FacetField(name, aggregationName, aggregationValues);
                        // Perhaps it’s redundant, as it is also set in the bucket
                        facetField.setCount(counter);
                        bucketFacetFields = Collections.singletonList(facetField);
                    }

                    buckets.add(new FacetField.Bucket(bucketValue, counter, bucketFacetFields));
                    total += counter;
                }
                key = key.split(SEPARATOR)[0];
                facets.add(new FacetField(key, total, buckets));
            } else if (key.endsWith(RANGES_SUFFIX)) {
                List<Double> facetFieldValues = new ArrayList<>();
                Number start = null;
                Number end = null;
                Number step = null;
                Double other = null;
                for (Document value : documentValues) {
                    if (value.get(INTERNAL_ID) instanceof String && OTHER.equals(value.getString(INTERNAL_ID))) {
                        other = 1.0d * value.getInteger(count.name());
                    } else {
                        Double range = value.getDouble(INTERNAL_ID);
                        Integer counter = value.getInteger(count.name());
                        facetFieldValues.add(1.0d * counter);
                        if (start == null) {
                            start = range;
                        }
                        end = range;
                        if (step == null && start != end) {
                            step = end.doubleValue() - start.doubleValue();
                        }
                    }
                }
                key = key.split(SEPARATOR)[0].replace(GenericDocumentComplexConverter.TO_REPLACE_DOTS, ".");
                if (other != null) {
                    key += " (counts out of range: " + other + ")";
                }
                FacetField facetField = new FacetField(key, "range", facetFieldValues)
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
                        facets.add(new FacetField(documentValue.getString(INTERNAL_ID), accumulator.name(), fieldValues));
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
}
