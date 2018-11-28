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

package org.opencb.commons.datastore.core.result;

import java.util.Collections;
import java.util.List;

/**
 * Created by jtarraga on 09/03/17.
 */
public class FacetQueryResult extends AbstractResult {

    private String query;
    private List<Field> results;

    public FacetQueryResult() {
    }

    @Deprecated
    public FacetQueryResult(String id, int dbTime, long numTotalResults, String warningMsg, String errorMsg,
                            List<Field> results, String query) {
        this(id, dbTime, numTotalResults, Collections.singletonList(new Error(-1, "", warningMsg)), new Error(-1, "", errorMsg),
                results, query);
    }

    public FacetQueryResult(String id, int dbTime, long numMatches, List<Error> warning, Error error, List<Field> results, String query) {
        super(id, dbTime, numMatches, warning, error);
        this.results = results;
        this.query = query;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FacetQueryResult{");
        sb.append("id='").append(id).append('\'');
        sb.append(", dbTime=").append(dbTime);
        sb.append(", numMatches=").append(numMatches);
        sb.append(", warning='").append(warning).append('\'');
        sb.append(", error='").append(error).append('\'');
        sb.append(", query=").append(query);
        sb.append(", results=").append(results);
        sb.append('}');
        return sb.toString();
    }

    public String getQuery() {
        return query;
    }

    public FacetQueryResult setQuery(String query) {
        this.query = query;
        return this;
    }

    public List<Field> getResults() {
        return results;
    }

    public FacetQueryResult setResults(List<Field> results) {
        this.results = results;
        return this;
    }

    public static class Field {
        private String name;
        private long count;
        private List<Bucket> buckets;
        private String aggregationName;
        private List<Double> aggregationValues;
        private Number start;
        private Number end;
        private Number step;

        public Field(String name, long count, List<Bucket> buckets) {
            this.name = name;
            this.count = count;
            this.buckets = buckets;
        }

        public Field(String name, String aggregationName, List<Double> aggregationValues) {
            this.name = name;
            this.aggregationName = aggregationName;
            this.aggregationValues = aggregationValues;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("FacetField{");
            sb.append("name='").append(name).append('\'');
            sb.append(", count=").append(count);
            sb.append(", buckets=").append(buckets);
            sb.append(", aggregationName='").append(aggregationName).append('\'');
            sb.append(", aggregationValues=").append(aggregationValues);
            sb.append(", start=").append(start);
            sb.append(", end=").append(end);
            sb.append(", step=").append(step);
            sb.append('}');
            return sb.toString();
        }

        public String getName() {
            return name;
        }

        public Field setName(String name) {
            this.name = name;
            return this;
        }

        public long getCount() {
            return count;
        }

        public Field setCount(long count) {
            this.count = count;
            return this;
        }

        public List<Bucket> getBuckets() {
            return buckets;
        }

        public Field setBuckets(List<Bucket> buckets) {
            this.buckets = buckets;
            return this;
        }

        public String getAggregationName() {
            return aggregationName;
        }

        public Field setAggregationName(String aggregationName) {
            this.aggregationName = aggregationName;
            return this;
        }

        public List<Double> getAggregationValues() {
            return aggregationValues;
        }

        public Field setAggregationValues(List<Double> aggregationValues) {
            this.aggregationValues = aggregationValues;
            return this;
        }

        public Number getStart() {
            return start;
        }

        public Field setStart(Number start) {
            this.start = start;
            return this;
        }

        public Number getEnd() {
            return end;
        }

        public Field setEnd(Number end) {
            this.end = end;
            return this;
        }

        public Number getStep() {
            return step;
        }

        public Field setStep(Number step) {
            this.step = step;
            return this;
        }
    }

    public static class Bucket {
        private String value;
        private long count;
        private List<Field> fields;

        public Bucket(String value, long count, List<Field> fields) {
            this.value = value;
            this.count = count;
            this.fields = fields;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Bucket{");
            sb.append("value='").append(value).append('\'');
            sb.append(", count=").append(count);
            sb.append(", fields=").append(fields);
            sb.append('}');
            return sb.toString();
        }

        public String getValue() {
            return value;
        }

        public Bucket setValue(String value) {
            this.value = value;
            return this;
        }

        public long getCount() {
            return count;
        }

        public Bucket setCount(long count) {
            this.count = count;
            return this;
        }

        public List<Field> getFields() {
            return fields;
        }

        public Bucket setFields(List<Field> fields) {
            this.fields = fields;
            return this;
        }
    }
}
