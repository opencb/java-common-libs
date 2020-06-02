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

package org.opencb.commons.datastore.solr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FacetQueryParser {

    public static final String FACET_SEPARATOR = ";";
    public static final String LABEL_SEPARATOR = "___";
    public static final String NESTED_FACET_SEPARATOR = ">>";
    public static final String NESTED_SUBFACET_SEPARATOR = ",";
    public static final String INCLUDE_SEPARATOR = ",";
    private static final String RANGE_IDENTIFIER = "..";
    private static final String AGGREGATION_IDENTIFIER = "(";
    private static final String[] AGGREGATION_FUNCTIONS = {"sum", "avg", "max", "min", "unique", "percentile", "sumsq", "variance",
            "stddev", };
    public static final Pattern CATEGORICAL_PATTERN = Pattern.compile("^([a-zA-Z][a-zA-Z0-9_.]+)(\\[[a-zA-Z0-9,*]+])?(:\\*|:\\d+)?$");

    private static final int DEFAULT_FACET_LIMIT = 50;

    private int count;

    public FacetQueryParser() {
        count = 0;
    }

    /**
     * This method accepts a simple facet declaration format and converts it into a rich JSON query.
     *
     * @param query A string with the format: chrom>>type,biotype,avg>>gerp,avg;type;biotype>>sadasd
     * @return A JSON string facet query
     * @throws Exception Any exception related with JSON conversion
     */
    public String parse(String query) throws Exception {
        if (StringUtils.isEmpty(query)) {
            return "";
        }

        Map<String, Object> jsonFacetMap = new LinkedHashMap<>();
        String[] split = query.split(FACET_SEPARATOR);
        for (String facet : split) {
            if (facet.contains(NESTED_FACET_SEPARATOR)) {
                parseNestedFacet(facet, jsonFacetMap);
            } else {
                List<String> simpleFacets = getSubFacets(facet);
                for (String simpleFacet : simpleFacets) {
                    parseSimpleFacet(simpleFacet, jsonFacetMap);
                }
            }
        }

        return parseJson(new ObjectMapper().writeValueAsString(jsonFacetMap));
    }

    public Map<String, Set<String>> getIncludingValuesMap(String facet) throws Exception {
        Map<String, Set<String>> includeMap = new HashMap<>();
        String[] subfacets = facet.split(FACET_SEPARATOR);
        for (String subfacet : subfacets) {
            if (subfacet.contains(NESTED_FACET_SEPARATOR)) {
                String[] nestedFacets = subfacet.split(NESTED_FACET_SEPARATOR);
                for (String nestedFacet : nestedFacets) {
                    updateIncludeMap(nestedFacet, includeMap);
                }
            } else {
                updateIncludeMap(subfacet, includeMap);
            }
        }
        return includeMap;
    }

    private void updateIncludeMap(String facet, Map<String, Set<String>> includeMap) throws Exception {
        // Only for categorical field names
        if (!facet.contains(RANGE_IDENTIFIER) && !facet.contains(AGGREGATION_IDENTIFIER)) {
            Matcher matcher = FacetQueryParser.CATEGORICAL_PATTERN.matcher(facet);
            if (matcher.find()) {
                if (matcher.group(1) != null && matcher.group(2) != null) {
                    if (includeMap.containsKey(matcher.group(1))) {
                        throw new Exception("Duplicated categorical field name: '" + matcher.group(1) + "'");
                    }
                    String include = matcher.group(2).replace("[", "").replace("]", "");
                    includeMap.put(matcher.group(1), new HashSet<>(Arrays.asList(include.split(FacetQueryParser.INCLUDE_SEPARATOR))));
                }
            }
        }
    }

    private void parseSimpleFacet(String facet, Map<String, Object> jsonFacetMap) throws Exception {
        Map<String, Object> facetMap = parseFacet(facet);
        String label;
        if (facetMap == null) {
            // Aggregation
            label = getLabelFromAggregation(facet);
            if (facet.startsWith("percentile")) {
                jsonFacetMap.put(label, facet.replace(")", ",1,10,25,50,75,90,99)"));
            } else {
                jsonFacetMap.put(label, facet);
            }
        } else {
            // Categorical or range
            label = getLabel(facetMap);
            jsonFacetMap.put(label, facetMap);
        }
    }

    private String getLabel(Map<String, Object> facetMap) {
        String label = (facetMap.containsKey("field") ? facetMap.get("field").toString() : "noname");
        // Range
        if (facetMap.containsKey("step")) {
            label += LABEL_SEPARATOR
                    + facetMap.get("start") + LABEL_SEPARATOR
                    + facetMap.get("end") + LABEL_SEPARATOR
                    + facetMap.get("step");
        }
        return label + LABEL_SEPARATOR + (count++);
    }

    private String getLabelFromAggregation(String facet) {
        String aggregationName = facet.substring(0, facet.indexOf("("));
        String fieldName = facet.substring(facet.indexOf("(") + 1, facet.indexOf(")"));
        String label = fieldName + LABEL_SEPARATOR + aggregationName;
        return label + LABEL_SEPARATOR + (count++);
    }

    /**
     * Parse a facet and return the map containing the facet fields.
     *
     * @param facet facet string
     * @return the map containing the facet fields.
     */
    private Map<String, Object> parseFacet(String facet) throws Exception {
        Map<String, Object> outputMap = new HashMap<>();
        if (facet.contains(AGGREGATION_IDENTIFIER)) {
            // Validate function
            for (String function : AGGREGATION_FUNCTIONS) {
                if (facet.startsWith(function)) {
                    return null;
                }
            }
            throw new Exception("Invalid aggregation function: " + facet);
        } else if (facet.contains(RANGE_IDENTIFIER)) {
            // Deal with ranges...
            int numMatches = StringUtils.countMatches(facet, ':');
            if (numMatches > 1) {
                // Special case: facet with type "query" and with a nested "range"
                String[] split = facet.split(":");
                Map<String, Object> rangeMap = new HashMap<>();
                StringBuilder filter = new StringBuilder(split[3]);
                for (int i = 4; i < split.length; i++) {
                    filter.append(":").append(split[i]);
                }

                String[] range = split[0].replace("[", ":").replace("..", ":").replace("]", "").split(":");
                rangeMap.put("field", range[0]);
                rangeMap.put("type", "range");
                rangeMap.put("start", parseNumber(range[1]));
                rangeMap.put("end", parseNumber(range[2]));
                rangeMap.put("step", parseNumber(split[1]));

                String[] nested = split[2].split(LABEL_SEPARATOR);
                outputMap.put("field", nested[0]);
                outputMap.put("type", "query");
                outputMap.put("q", filter.toString());
                if (nested.length > 1) {
                    Map<String, Object> nestedMap = new HashMap<>();
                    nestedMap.put("field", nested[1]);
                    nestedMap.put("type", "terms");
                    Map<String, Object> auxMap1 = new HashMap<>();
                    auxMap1.put(range[0], rangeMap);
                    nestedMap.put("facet", auxMap1);

                    Map<String, Object> auxMap2 = new HashMap<>();
                    auxMap2.put(nested[1], nestedMap);
                    outputMap.put("facet", auxMap2);
                } else {
                    Map<String, Object> auxMap1 = new HashMap<>();
                    auxMap1.put(range[0], rangeMap);
                    outputMap.put("facet", auxMap1);
                }
            } else {
                String[] split = facet.replace("[", ":").replace("..", ":").replace("]", "").split(":");
                outputMap.put("field", split[0]);
                outputMap.put("start", parseNumber(split[1]));
                outputMap.put("end", parseNumber(split[2]));
                outputMap.put("step", parseNumber(split[3]));
            }
        } else {
            // Categorical...
            Matcher matcher = CATEGORICAL_PATTERN.matcher(facet);
            if (matcher.find()) {
                boolean isQuery = false;
                outputMap.put("field", matcher.group(1));
                String include = matcher.group(2);
                if (StringUtils.isNotEmpty(include)) {
                    include = include.replace("]", "").replace("[", "");
                    if (include.endsWith("*")) {
                        outputMap.put("prefix", include.substring(0, include.indexOf("*")));
                    } else {
                        isQuery = true;
                        List<String> filters = new ArrayList<>();
                        for (String value: include.split(INCLUDE_SEPARATOR)) {
                            filters.add(matcher.group(1) + ":" + value);
                        }
                        outputMap.put("q", StringUtils.join(filters, " OR "));
                    }
                }

                if (isQuery) {
                    outputMap.put("type", "query");

                    Map<String, Object> auxMap = new HashMap<>();
                    auxMap.put("field", matcher.group(1));
                    auxMap.put("type", "terms");
                    setTermLimit(matcher.group(3), auxMap);

                    Map<String, Object> tmpMap = new HashMap<>();
                    tmpMap.put(matcher.group(1), auxMap);

                    outputMap.put("facet", tmpMap);
                } else {
                    outputMap.put("type", "terms");
                    setTermLimit(matcher.group(3), outputMap);
                }
            } else {
                throw new Exception("Invalid categorical facet: " + facet);
            }
        }

        return outputMap;
    }

    private void setTermLimit(String limit, Map<String, Object> map) {
        if (StringUtils.isNotEmpty(limit)) {
            if (limit.contains("*")) {
                map.put("allBuckets", true);
            } else {
                map.put("limit", Integer.parseInt(limit.substring(1)));
            }
        } else {
            map.put("limit", DEFAULT_FACET_LIMIT);
        }
    }

    private void parseNestedFacet(String nestedFacet, Map<String, Object> jsonFacet) throws Exception {
        String[] split = nestedFacet.split(NESTED_FACET_SEPARATOR);

        Map<String, Object> rootFacetMap = new HashMap<>();
        Map<String, Object> childFacetMap = new HashMap<>();
        for (int i = split.length - 1; i >= 0; i--) {
            String facet = split[i];
            List<String> subfacets = getSubFacets(facet);
            for (String subfacet: subfacets) {
                parseSimpleFacet(subfacet, rootFacetMap);
            }

            if (!childFacetMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : rootFacetMap.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> value = (Map<String, Object>) entry.getValue();
                        // We have to check if there is already a fieldname facet, it means we are processing
                        // a facet of type "query"
                        if (value.containsKey("facet")) {
                            Map<String, Object> innerMap = (Map<String, Object>) value.get("facet");
                            for (Map.Entry<String, Object> innerEntry: innerMap.entrySet()) {
                                if (innerEntry.getValue() instanceof Map) {
                                    ((Map<String, Object>) innerEntry.getValue()).put("facet", childFacetMap);
                                }
                            }
                        } else {
                            value.put("facet", childFacetMap);
                        }
                    }
                }
            }

            childFacetMap = rootFacetMap;
            rootFacetMap = new HashMap<>();
        }

        jsonFacet.putAll(childFacetMap);
    }

    public String parseJson(String facetJson) throws IOException {
        Queue<Map<String, Object>> myQueue = new LinkedList<>();
        Map jsonMap = new ObjectMapper().readValue(facetJson, Map.class);
        myQueue.add(jsonMap);

        while (!myQueue.isEmpty()) {
            Map<String, Object> map = myQueue.remove();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> innerMap = (Map<String, Object>) entry.getValue();

                    // Analyse map to fill in content
                    if (innerMap.containsKey("start")) {
                        // Ranges
                        innerMap.put("type", "range");
                        innerMap.put("gap", innerMap.get("step"));
                        innerMap.remove("step");
                    } else if (innerMap.containsKey("q")) {
                        // Query
                        innerMap.put("type", "query");
                    } else if (innerMap.containsKey("field")) {
                        // Categorical
                        innerMap.put("type", "terms");
                    }

                    // Check if there is a 'facet' field and insert all the items in the queue
                    Object facet = innerMap.get("facet");
                    if (facet != null) {
                        myQueue.add((Map<String, Object>) facet);
                    }
                }
            }
        }

        return new ObjectMapper().writeValueAsString(jsonMap);
    }

    static Number parseNumber(String number) {
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException e) {
            return Double.parseDouble(number);
        }
    }

    static List<String> getSubFacets(String query) {
        String facet = "";
        List<String> facets = new ArrayList<>();
        String[] split = query.split(NESTED_SUBFACET_SEPARATOR);
        for (String s: split) {
            if (s.contains("[")) {
                if (s.contains("]")) {
                    facets.add(s);
                    facet = null;
                } else {
                    if (StringUtils.isEmpty(facet)) {
                        facet = s;
                    } else {
                        facets.add(facet);
                        facet = s;
                    }
                }
            } else if (s.contains("]")) {
                facets.add(facet + "," + s);
                facet = null;
            } else {
                if (StringUtils.isEmpty(facet)) {
                    facets.add(s);
                    facet = null;
                } else {
                    facet += ("," + s);
                }
            }
        }

        return facets;
    }
}
