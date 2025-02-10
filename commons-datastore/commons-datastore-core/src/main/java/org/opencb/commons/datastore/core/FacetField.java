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

package org.opencb.commons.datastore.core;

import java.util.List;

/**
 * Created by jtarraga on 09/03/17.
 */
public class FacetField {
    private String name;
    private long count;
    private List<Bucket> buckets;
    private String aggregationName;
    private List<Double> aggregationValues;
    private Number start;
    private Number end;
    private Number step;

    public FacetField(String name, long count, List<Bucket> buckets) {
        this.name = name;
        this.count = count;
        this.buckets = buckets;
    }

    public FacetField(String name, long count, String aggregationName, List<Double> aggregationValues) {
        this.name = name;
        this.count = count;
        this.aggregationName = aggregationName;
        this.aggregationValues = aggregationValues;
    }

    public FacetField(String name, String aggregationName, List<Double> aggregationValues) {
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

    public FacetField setName(String name) {
        this.name = name;
        return this;
    }

    public long getCount() {
        return count;
    }

    public FacetField setCount(long count) {
        this.count = count;
        return this;
    }

    public FacetField addCount(long delta) {
        this.count = this.count + delta;
        return this;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public FacetField setBuckets(List<Bucket> buckets) {
        this.buckets = buckets;
        return this;
    }

    public String getAggregationName() {
        return aggregationName;
    }

    public FacetField setAggregationName(String aggregationName) {
        this.aggregationName = aggregationName;
        return this;
    }

    public List<Double> getAggregationValues() {
        return aggregationValues;
    }

    public FacetField setAggregationValues(List<Double> aggregationValues) {
        this.aggregationValues = aggregationValues;
        return this;
    }

    public Number getStart() {
        return start;
    }

    public FacetField setStart(Number start) {
        this.start = start;
        return this;
    }

    public Number getEnd() {
        return end;
    }

    public FacetField setEnd(Number end) {
        this.end = end;
        return this;
    }

    public Number getStep() {
        return step;
    }

    public FacetField setStep(Number step) {
        this.step = step;
        return this;
    }

    public static class Bucket {
        private String value;
        private long count;
        private List<FacetField> facetFields;

        public Bucket(String value, long count, List<FacetField> facetFields) {
            this.value = value;
            this.count = count;
            this.facetFields = facetFields;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Bucket{");
            sb.append("value='").append(value).append('\'');
            sb.append(", count=").append(count);
            sb.append(", facetFields=").append(facetFields);
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

        public Bucket addCount(long delta) {
            this.count += delta;
            return this;
        }

        public List<FacetField> getFacetFields() {
            return facetFields;
        }

        public Bucket setFacetFields(List<FacetField> facetFields) {
            this.facetFields = facetFields;
            return this;
        }
    }
}
