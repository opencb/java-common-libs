package org.opencb.commons.datastore.solr;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.opencb.commons.datastore.core.result.FacetQueryResultItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SolrFacetToFacetQueryResultItemConverter {

    public FacetQueryResultItem convert(QueryResponse solrResponse) {
        // Sanity check
        if (solrResponse == null || solrResponse.getResponse() == null || solrResponse.getResponse().get("facets") == null) {
            return null;
        }

        SimpleOrderedMap<Object> solrFacets = (SimpleOrderedMap<Object>) solrResponse.getResponse().get("facets");
        List<FacetQueryResultItem.FacetField> facetFields = new ArrayList<>();
        int count = (int) solrFacets.get("count");
        for (int i = 0; i < solrFacets.size(); i++) {
            String name = solrFacets.getName(i);
            if (!"count".equals(name)) {
                if (solrFacets.get(name) instanceof SimpleOrderedMap) {
                    FacetQueryResultItem.FacetField facetField = new FacetQueryResultItem.FacetField(name, count, new ArrayList<>());
                    parseBuckets((SimpleOrderedMap<Object>) solrFacets.get(name), facetField);
                    facetFields.add(facetField);
                } else {
                    facetFields.add(parseAggregation(name, solrFacets.get(name)));
                }
            }
        }

        return new FacetQueryResultItem(facetFields);
    }

    private void parseBuckets(SimpleOrderedMap<Object> solrFacets, FacetQueryResultItem.FacetField facetField) {
        List<SimpleOrderedMap<Object>> solrBuckets = (List<SimpleOrderedMap<Object>>) solrFacets.get("buckets");
        List<FacetQueryResultItem.Bucket> buckets = new ArrayList<>();
        for (SimpleOrderedMap<Object> solrBucket: solrBuckets) {
            int count = 0;
            String value = "";
            FacetQueryResultItem.FacetField subfield;
            List<FacetQueryResultItem.FacetField> subfields = new ArrayList<>();
            for (int i = 0; i < solrBucket.size(); i++) {
                String fullname = solrBucket.getName(i);
                if ("count".equals(fullname)) {
                    count = (int) solrBucket.getVal(i);
                } else if ("val".equals(fullname)) {
                    value = solrBucket.getVal(i).toString();
                } else {
                    if (solrBucket.getVal(i) instanceof SimpleOrderedMap) {
                        String[] split = fullname.split("__");
                        subfield = new FacetQueryResultItem.FacetField(split[0], count, new ArrayList<>());
                        if (split.length > 3) {
                            subfield.setStart(Integer.parseInt(split[1]));
                            subfield.setEnd(Integer.parseInt(split[2]));
                            subfield.setStep(Integer.parseInt(split[3]));
                        }

                        parseBuckets((SimpleOrderedMap<Object>) solrBucket.getVal(i), subfield);
                    } else {
                        subfield = parseAggregation(fullname, solrBucket.getVal(i));
                    }
                    subfields.add(subfield);
                }
            }
            FacetQueryResultItem.Bucket bucket = new FacetQueryResultItem.Bucket(value, count, subfields);
            bucket.setFacetFields(subfields);
            buckets.add(bucket);
        }
        facetField.setBuckets(buckets);
    }

    private FacetQueryResultItem.FacetField parseAggregation(String fullname, Object value) {
        String[] split = fullname.split("__");
        String fieldName = split[0];
        String aggregationName = split[1];
        if (split.length > 3) {
            aggregationName += ("(" + split[2]);
            for (int i = 3; i < split.length - 1; i++) {
                aggregationName += ("," + split[i]);
            }
            aggregationName += ")";
        }
        List<Double> values;
        if (value instanceof Double[]) {
            values = Arrays.asList((Double[]) value);
        } else {
            values = Collections.singletonList((Double) value);
        }
        return new FacetQueryResultItem.FacetField(fieldName, aggregationName, values);
    }
}
