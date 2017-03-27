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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jtarraga on 09/03/17.
 */
public class FacetedQueryResultItem {

    private List<Field> fields;
    private List<Range> ranges;

    public FacetedQueryResultItem() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public FacetedQueryResultItem(List<Field> fields, List<Range> ranges) {
        this.fields = fields;
        this.ranges = ranges;
    }

    public long size() {
        long size = 0;
        if (fields != null) {
            size += fields.size();
        }
        if (ranges != null) {
            size += ranges.size();
        }
        return size;
    }

    public class Field {

        private String name;
        private long total;
        private List<Count> counts;

        public Field() {
            this("", 0, new ArrayList<>());
        }

        public Field(String name, long total, List<Count> counts) {
            this.name = name;
            this.total = total;
            this.counts = counts;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Field{");
            sb.append("name='").append(name).append('\'');
            sb.append(", total=").append(total);
            sb.append(", counts=").append(counts);
            sb.append('}');
            return sb.toString();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public List<Count> getCounts() {
            return counts;
        }

        public void setCounts(List<Count> counts) {
            this.counts = counts;
        }
    }

    public class Count {

        private String value;
        private long count;
        private Field field;

        public Count(String value, long count, Field field) {
            this.value = value;
            this.count = count;
            this.field  = field;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Count{");
            sb.append("value='").append(value).append('\'');
            sb.append(", count=").append(count);
            sb.append(", field=").append(field);
            sb.append('}');
            return sb.toString();
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }
    }

    public class Range {

        private String name;
        private Number start;
        private Number end;
        private Number gap;
        private long total;
        private List<Long> counts;

        public Range() {
            this("", 0, 0, 0, 0, new ArrayList<>());
        }

        public Range(String name, Number start, Number end, Number gap, long total, List<Long> counts) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.gap = gap;
            this.total = total;
            this.counts = counts;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Range{");
            sb.append("name='").append(name).append('\'');
            sb.append(", start=").append(start);
            sb.append(", end=").append(end);
            sb.append(", gap=").append(gap);
            sb.append(", total=").append(total);
            sb.append(", counts=").append(counts);
            sb.append('}');
            return sb.toString();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Number getStart() {
            return start;
        }

        public void setStart(Number start) {
            this.start = start;
        }

        public Number getEnd() {
            return end;
        }

        public void setEnd(Number end) {
            this.end = end;
        }

        public Number getGap() {
            return gap;
        }

        public void setGap(Number gap) {
            this.gap = gap;
        }

        public long getTotal() {
            return total;
        }

        public void setGap(long totalCount) {
            this.total = totalCount;
        }

        public List<Long> getCounts() {
            return counts;
        }

        public void setCounts(List<Long> counts) {
            this.counts = counts;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FacetedQueryResultItem{");
        sb.append("fields=").append(fields);
        sb.append(", ranges=").append(ranges);
        sb.append('}');
        return sb.toString();
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public void setRanges(List<Range> ranges) {
        this.ranges = ranges;
    }

}
