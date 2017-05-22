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

/**
 * Created by jtarraga on 09/03/17.
 */
public class FacetedQueryResult extends AbstractResult {

    private FacetedQueryResultItem result;

    public FacetedQueryResult() {
    }

    public FacetedQueryResult(String id, int dbTime, int numResults, long numTotalResults, String warningMsg, String errorMsg,
                              FacetedQueryResultItem result) {
        super(id, dbTime, numResults, numTotalResults, warningMsg, errorMsg);
        this.result = result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FacetedQueryResult{");
        sb.append("id='").append(id).append('\'');
        sb.append(", dbTime=").append(dbTime);
        sb.append(", numResults=").append(numResults);
        sb.append(", numTotalResults=").append(numTotalResults);
        sb.append(", warningMsg='").append(warningMsg).append('\'');
        sb.append(", errorMsg='").append(errorMsg).append('\'');
        sb.append(", result=").append(result);
        sb.append('}');
        return sb.toString();
    }

    public FacetedQueryResultItem getResult() {
        return result;
    }

    public FacetedQueryResult setResult(FacetedQueryResultItem result) {
        this.result = result;
        return this;
    }
}
