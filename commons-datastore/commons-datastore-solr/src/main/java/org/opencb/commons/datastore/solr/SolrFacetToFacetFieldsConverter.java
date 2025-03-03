package org.opencb.commons.datastore.solr;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.opencb.commons.datastore.core.FacetField;

import java.util.*;

import static org.opencb.commons.datastore.solr.FacetQueryParser.LABEL_SEPARATOR;
import static org.opencb.commons.datastore.solr.FacetQueryParser.parseNumber;

public class SolrFacetToFacetFieldsConverter {

    public static List<FacetField> convert(QueryResponse solrResponse) {
        return convert(solrResponse, new HashMap<>());
    }

    /**
     * Convert a generic solrResponse into a list of facet fields.
     *
     * @param solrResponse SolrResponse.
     * @param alias        Map containing the solr keys present in the solrResponse and the key we actually want to be visible for the user.
     *                     This is mainly to be able to hide private solr keys to the user and use better user-friendly keys.
     * @return a list of facet field.
     */
    public static List<FacetField> convert(QueryResponse solrResponse, Map<String, String> alias) {
        // Sanity check
        if (solrResponse == null || solrResponse.getResponse() == null || solrResponse.getResponse().get("facets") == null) {
            return Collections.emptyList();
        }

        if (alias == null) {
            alias = new HashMap<>();
        }

        SimpleOrderedMap<Object> solrFacets = (SimpleOrderedMap<Object>) solrResponse.getResponse().get("facets");
        List<FacetField> fields = new ArrayList<>();
        long count = 0;
        if (solrFacets.get("count") != null) {
            count = ((Number) solrFacets.get("count")).longValue();
        }
        for (int i = 0; i < solrFacets.size(); i++) {
            String name = solrFacets.getName(i);
            if (!"count".equals(name)) {
                if (solrFacets.get(name) instanceof SimpleOrderedMap) {
                    String[] split = name.split("___");
                    FacetField facetField = new FacetField(getName(split[0], alias),
                            getBucketCount((SimpleOrderedMap<Object>) solrFacets.get(name), count), new ArrayList<>());
                    if (split.length > 3) {
                        facetField.setStart(FacetQueryParser.parseNumber(split[1]));
                        facetField.setEnd(FacetQueryParser.parseNumber(split[2]));
                        facetField.setStep(FacetQueryParser.parseNumber(split[3]));
                    }

                    parseBuckets((SimpleOrderedMap<Object>) solrFacets.get(name), facetField, alias);
                    fields.add(facetField);
                } else {
                    fields.add(parseAggregation(name, solrFacets.get(name), alias));
                }
            }
        }

        return fields;
    }

    /**
     * In order to process type=query facets with a nested type=range.
     *
     * @param solrFacets    Solr facet
     * @param defaultCount  Default count
     * @return  Actual count
     */
    private static long getBucketCount(SimpleOrderedMap<Object> solrFacets, long defaultCount) {
        List<SimpleOrderedMap<Object>> solrBuckets = (List<SimpleOrderedMap<Object>>) solrFacets.get("buckets");
        if (solrBuckets == null) {
            for (int i = 0; i < solrFacets.size(); i++) {
                if (solrFacets.getName(i).equals("count") && solrFacets.getVal(i) != null) {
                    return ((Number) solrFacets.getVal(i)).longValue();
                }
            }
        }
        return defaultCount;
    }

    private static void parseBuckets(SimpleOrderedMap<Object> solrFacets, FacetField facetField,
                                     Map<String, String> alias) {
        List<SimpleOrderedMap<Object>> solrBuckets = (List<SimpleOrderedMap<Object>>) solrFacets.get("buckets");
        if (solrBuckets == null) {
            // If it does not have buckets, it means that we are processing a type=query facet with a nested type=range
            for (int i = 0; i < solrFacets.size(); i++) {
                if (!solrFacets.getName(i).equals("count")
                        && ((SimpleOrderedMap<Object>) solrFacets.getVal(i)).get("buckets") != null) {
                    solrBuckets = (List<SimpleOrderedMap<Object>>)
                            ((SimpleOrderedMap<Object>) solrFacets.getVal(i)).get("buckets");
                    break;
                }
            }
        }
        if (solrBuckets == null) {
            // No buckets, just return!
            return;
        }

        List<FacetField.Bucket> buckets = new ArrayList<>();
        for (SimpleOrderedMap<Object> solrBucket: solrBuckets) {
            int count = 0;
            String value = "";
            FacetField subfield;
            List<FacetField> subfields = new ArrayList<>();
            for (int i = 0; i < solrBucket.size(); i++) {
                String fullname = solrBucket.getName(i);
                if ("count".equals(fullname)) {
                    count = (int) solrBucket.getVal(i);
                } else if ("val".equals(fullname)) {
                    value = solrBucket.getVal(i).toString();
                } else {
                    if (solrBucket.getVal(i) instanceof SimpleOrderedMap) {
                        String[] split = fullname.split(LABEL_SEPARATOR);
                        subfield = new FacetField(getName(split[0], alias),
                                getBucketCount((SimpleOrderedMap<Object>) solrBucket.getVal(i), count),
                                new ArrayList<>());
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
            FacetField.Bucket bucket = new FacetField.Bucket(value, count, subfields);
            bucket.setFacetFields(subfields);
            buckets.add(bucket);
        }
        facetField.setBuckets(buckets);
    }

    private static FacetField parseAggregation(String fullname, Object value, Map<String, String> alias) {
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
            values = Collections.singletonList(((Number) value).doubleValue());
        }
        return new FacetField(getName(fieldName, alias), aggregationName, values);
    }

    private static String getName(String name, Map<String, String> alias) {
        String[] split = name.split(LABEL_SEPARATOR);
        return alias.getOrDefault(split[0], name);
    }

}
