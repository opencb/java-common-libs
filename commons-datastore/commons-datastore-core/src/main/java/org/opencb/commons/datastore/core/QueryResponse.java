/*
 * Copyright 2015 OpenCB
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imedina on 20/03/14.
 */
public class QueryResponse<T> {

    private String apiVersion;
    private int time;
    private String warning;
    private String error;

    private QueryOptions queryOptions;
    private List<QueryResult<T>> response;

    public QueryResponse() {
        this("", -1, "", "", null, null);
    }

    public QueryResponse(QueryOptions queryOptions, List<QueryResult<T>> response) {
        this("", -1, "", "", queryOptions, response);
    }

    @Deprecated
    public QueryResponse(QueryOptions queryOptions, List<QueryResult<T>> response, String version, String species, int time) {
        this.apiVersion = "";
        this.warning = "";
        this.error = "";
        this.queryOptions = queryOptions;
        this.response = response;
        this.time = time;
    }

    public QueryResponse(String apiVersion, int time, String warning, String error, QueryOptions queryOptions,
                         List<QueryResult<T>> response) {
        this.apiVersion = apiVersion;
        this.time = time;
        this.warning = warning;
        this.error = error;
        this.queryOptions = queryOptions;
        this.response = response;
    }


    /**
     * This method just returns the first QueryResult of response, or null if response is null or empty.
     * @return the first QueryResult in the response
     */
    public QueryResult<T> first() {
        if (response != null && response.size() > 0) {
            return response.get(0);
        }
        return null;
    }

    /**
     * This method returns the first result from the first QueryResult of response, this is equivalent to response.get(0).getResult.get(0).
     * @return T value if exists, null otherwise
     */
    public T firstResult() {
        if (response != null && response.size() > 0) {
            return response.get(0).first();
        }
        return null;
    }

    public int allResultsSize() {
        int totalSize = 0;
        if (response != null && response.size() > 0) {
            for (QueryResult<T> queryResult : response) {
                totalSize += queryResult.getResult().size();
            }
        }
        return totalSize;
    }

    /**
     * This method flats the two levels (QueryResponse and QueryResult) into a single list of T.
     * @return a single list with all the results, or null if no response exists
     */
    public List<T> allResults() {
        List<T> results = null;
        if (response != null && response.size() > 0) {
            // We first calculate the total size needed
            int totalSize = allResultsSize();

            // We init the list and copy data
            results = new ArrayList<>(totalSize);
            for (QueryResult<T> queryResult : response) {
                results.addAll(queryResult.getResult());
            }
        }
        return results;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryResponse{");
        sb.append("apiVersion='").append(apiVersion).append('\'');
        sb.append(", time=").append(time);
        sb.append(", warning='").append(warning).append('\'');
        sb.append(", error='").append(error).append('\'');
        sb.append(", queryOptions=").append(queryOptions);
        sb.append(", response=").append(response);
        sb.append('}');
        return sb.toString();
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getWarning() {
        return warning;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public void setQueryOptions(QueryOptions queryOptions) {
        this.queryOptions = queryOptions;
    }

    public List<QueryResult<T>> getResponse() {
        return response;
    }

    public void setResponse(List<QueryResult<T>> response) {
        this.response = response;
    }
}
