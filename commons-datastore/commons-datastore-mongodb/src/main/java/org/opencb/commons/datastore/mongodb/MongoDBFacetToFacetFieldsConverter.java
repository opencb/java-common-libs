package org.opencb.commons.datastore.mongodb;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.opencb.commons.datastore.core.ComplexTypeConverter;
import org.opencb.commons.datastore.core.FacetField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.*;
import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.Accumulator.count;

public class MongoDBFacetToFacetFieldsConverter implements ComplexTypeConverter<List<FacetField>, Document> {

    @Override
    public List<FacetField> convertToDataModelType(Document document) {
        if (document == null || document.entrySet().size() == 0) {
            return Collections.emptyList();
        }

        List<FacetField> facets = new ArrayList<>();
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            String key = entry.getKey();
            List<Document> documentValues = (List<Document>) entry.getValue();
            if (key.endsWith(COUNTS_SUFFIX)) {
                List<FacetField.Bucket> buckets = new ArrayList<>(documentValues.size());
                long total = 0;
                for (Document documentValue : documentValues) {
                    long counter = documentValue.getInteger(count.name());
                    String bucketValue;
                    if (documentValue.get(INTERNAL_ID) instanceof String) {
                        bucketValue = documentValue.getString(INTERNAL_ID);
                    } else if (documentValue.get(INTERNAL_ID) instanceof Boolean) {
                        bucketValue = documentValue.getBoolean(INTERNAL_ID).toString();
                    } else {
                        Document combined = (Document) documentValue.get(INTERNAL_ID);
                        bucketValue = StringUtils.join(combined.values(), AND_SEPARATOR);
                    }
                    buckets.add(new FacetField.Bucket(bucketValue, counter, null));
                    total += counter;
                }
                key = key.substring(0, key.length() - COUNTS_SUFFIX.length());
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
                System.out.println("entry = " + entry);
                key = key.substring(0, key.length() - RANGES_SUFFIX.length()).replace(GenericDocumentComplexConverter.TO_REPLACE_DOTS, ".");
                if (other != null) {
                    key += " (out of range = " + other + ")";
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
                    case max:
                    case min:
                    case avg:
                    case stdDevPop:
                    case stdDevSamp: {
                        Double fieldValue;
                        if (documentValue.get(accumulator.name()) instanceof Integer) {
                            fieldValue = 1.0d * documentValue.getInteger(accumulator.name());
                        } else {
                            fieldValue = documentValue.getDouble(accumulator.name());
                        }
                        facets.add(new FacetField(documentValue.getString(INTERNAL_ID), accumulator.name(),
                                Collections.singletonList(fieldValue)));
                        break;
                    }
                    default: {
                        // Nothing to do
                        break;
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
        throw new IllegalArgumentException("No accumulators found in facet document: " + StringUtils.join(document.keySet(), ","));
    }

    @Override
    public Document convertToStorageType(List<FacetField> facetFields) {
        throw new RuntimeException("Not yet implemented");
    }
}
