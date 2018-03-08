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
public class FacetedQueryResult extends AbstractResult {

    private int numResults;
    private FacetedQueryResultItem result;

    public FacetedQueryResult() {
    }

    @Deprecated
    public FacetedQueryResult(String id, int dbTime, int numResults, long numTotalResults, String warningMsg, String errorMsg,
                              FacetedQueryResultItem result) {
        this(id, dbTime, numResults, numTotalResults, Collections.singletonList(new Error(-1, "", warningMsg)), new Error(-1, "", errorMsg),
                result);
    }

    public FacetedQueryResult(String id, int dbTime, int numResults, long numMatches, List<Error> warning, Error error,
                              FacetedQueryResultItem result) {
        super(id, dbTime, numMatches, warning, error);
        this.numResults = numResults;
        this.result = result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FacetedQueryResult{");
        sb.append("id='").append(id).append('\'');
        sb.append(", dbTime=").append(dbTime);
        sb.append(", numResults=").append(numResults);
        sb.append(", numMatches=").append(numMatches);
        sb.append(", warning='").append(warning).append('\'');
        sb.append(", error='").append(error).append('\'');
        sb.append(", result=").append(result);
        sb.append('}');
        return sb.toString();
    }

    public int getNumResults() {
        return numResults;
    }

    public FacetedQueryResult setNumResults(int numResults) {
        this.numResults = numResults;
        return this;
    }

    @Deprecated
    public long getNumTotalResults() {
        return numMatches;
    }

    @Deprecated
    public FacetedQueryResult setNumTotalResults(long numTotalResults) {
        this.numMatches = numTotalResults;
        return this;
    }

    @Override
    public long getNumMatches() {
        return numMatches;
    }

    @Override
    public FacetedQueryResult setNumMatches(long numMatches) {
        this.numMatches = numMatches;
        return this;
    }

    public FacetedQueryResultItem getResult() {
        return result;
    }

    public FacetedQueryResult setResult(FacetedQueryResultItem result) {
        this.result = result;
        return this;
    }
}
