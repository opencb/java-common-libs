package org.opencb.commons.datastore.solr;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.opencb.commons.datastore.core.result.FacetQueryResultItem;

import java.util.*;

import static org.opencb.commons.datastore.solr.FacetQueryParser.LABEL_SEPARATOR;
import static org.opencb.commons.datastore.solr.FacetQueryParser.parseNumber;

public class SolrFacetToFacetQueryResultItemConverter {

    public static FacetQueryResultItem convert(QueryResponse solrResponse) {
        return convert(solrResponse, new HashMap<>());
    }

    /**
     * Convert a generic solrResponse into our FacetQueryResultItem.
     *
     * @param solrResponse SolrResponse.
     * @param alias        Map containing the solr keys present in the solrResponse and the key we actually want to be visible for the user.
     *                     This is mainly to be able to hide private solr keys to the user and use better user-friendly keys.
     * @return a FacetQueryResultItem.
     */
    public static FacetQueryResultItem convert(QueryResponse solrResponse, Map<String, String> alias) {
        // Sanity check
        if (solrResponse == null || solrResponse.getResponse() == null || solrResponse.getResponse().get("facets") == null) {
            return null;
        }

        if (alias == null) {
            alias = new HashMap<>();
        }

        SimpleOrderedMap<Object> solrFacets = (SimpleOrderedMap<Object>) solrResponse.getResponse().get("facets");
        List<FacetQueryResultItem.FacetField> facetFields = new ArrayList<>();
        int count = (int) solrFacets.get("count");
        for (int i = 0; i < solrFacets.size(); i++) {
            String name = solrFacets.getName(i);
            if (!"count".equals(name)) {
                if (solrFacets.get(name) instanceof SimpleOrderedMap) {

                    String[] split = name.split("___");
                    FacetQueryResultItem.FacetField facetField = new FacetQueryResultItem.FacetField(getName(split[0], alias), count,
                            new ArrayList<>());
                    if (split.length > 3) {
                        facetField.setStart(FacetQueryParser.parseNumber(split[1]));
                        facetField.setEnd(FacetQueryParser.parseNumber(split[2]));
                        facetField.setStep(FacetQueryParser.parseNumber(split[3]));
                    }

                    parseBuckets((SimpleOrderedMap<Object>) solrFacets.get(name), facetField, alias);
                    facetFields.add(facetField);
                } else {
                    facetFields.add(parseAggregation(name, solrFacets.get(name), alias));
                }
            }
        }

        return new FacetQueryResultItem(facetFields);
    }

    private static void parseBuckets(SimpleOrderedMap<Object> solrFacets, FacetQueryResultItem.FacetField facetField,
                                     Map<String, String> alias) {
        List<SimpleOrderedMap<Object>> solrBuckets = (List<SimpleOrderedMap<Object>>) solrFacets.get("buckets");
        List<FacetQueryResultItem.Bucket> buckets = new ArrayList<>();
        for (SimpleOrderedMap<Object> solrBucket : solrBuckets) {
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
                        String[] split = fullname.split(LABEL_SEPARATOR);
                        subfield = new FacetQueryResultItem.FacetField(getName(split[0], alias), count, new ArrayList<>());
                        if (split.length > 3) {
                            subfield.setStart(parseNumber(split[1]));
                            subfield.setEnd(parseNumber(split[2]));
                            subfield.setStep(parseNumber(split[3]));
                        }
                        parseBuckets((SimpleOrderedMap<Object>) solrBucket.getVal(i), subfield, alias);
                    } else {
                        subfield = parseAggregation(fullname, solrBucket.getVal(i), alias);
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

    private static FacetQueryResultItem.FacetField parseAggregation(String fullname, Object value, Map<String, String> alias) {
        String[] split = fullname.split(LABEL_SEPARATOR);
        String fieldName = split[0];
        String aggregationName = split[1];
        if (split.length > 3) {
            aggregationName += ("(" + split[2]);
            for (int i = 3; i < split.length - 1; i++) {
                aggregationName += ("," + split[i]);
            }
        }
        List<Double> values;
        if (value instanceof ArrayList) {
            values = (List<Double>) value;
        } else {
            values = Collections.singletonList((Double) value);
        }
        return new FacetQueryResultItem.FacetField(getName(fieldName, alias), aggregationName, values);
    }

    private static String getName(String name, Map<String, String> alias) {
        String[] split = name.split(LABEL_SEPARATOR);
        return alias.getOrDefault(split[0], name);
    }

}
