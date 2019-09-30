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
    private List<DataResult<T>> response;

    public QueryResponse() {
        this("", -1, "", "", null, null);
    }

    public QueryResponse(String apiVersion, int time, QueryOptions queryOptions, List<DataResult<T>> response) {
        this(apiVersion, time, "", "", queryOptions, response);
    }

    public QueryResponse(QueryOptions queryOptions, List<DataResult<T>> response) {
        this("", -1, "", "", queryOptions, response);
    }

    @Deprecated
    public QueryResponse(QueryOptions queryOptions, List<DataResult<T>> response, String version, String species, int time) {
        this.apiVersion = "";
        this.warning = "";
        this.error = "";
        this.queryOptions = queryOptions;
        this.response = response;
        this.time = time;
    }

    public QueryResponse(String apiVersion, int time, String warning, String error, QueryOptions queryOptions,
                         List<DataResult<T>> response) {
        this.apiVersion = apiVersion;
        this.time = time;
        this.warning = warning;
        this.error = error;
        this.queryOptions = queryOptions;
        this.response = response;
    }


    /**
     * This method just returns the first DataResult of response, or null if response is null or empty.
     * @return the first DataResult in the response
     */
    public DataResult<T> first() {
        if (response != null && response.size() > 0) {
            return response.get(0);
        }
        return null;
    }

    /**
     * This method returns the first result from the first DataResult of response, this is equivalent to response.get(0).getResult.get(0).
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
            for (DataResult<T> queryResult : response) {
                totalSize += queryResult.getResults().size();
            }
        }
        return totalSize;
    }

    /**
     * This method flats the two levels (QueryResponse and DataResult) into a single list of T.
     * @return a single list with all the results, or null if no response exists
     */
    public List<T> allResults() {
        List<T> results = null;
        if (response != null && response.size() > 0) {
            // We first calculate the total size needed
            int totalSize = allResultsSize();

            // We init the list and copy data
            results = new ArrayList<>(totalSize);
            for (DataResult<T> queryResult : response) {
                results.addAll(queryResult.getResults());
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

    public String getApiVersion() {
        return apiVersion;
    }

    public QueryResponse setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public int getTime() {
        return time;
    }

    public QueryResponse setTime(int time) {
        this.time = time;
        return this;
    }

    public String getWarning() {
        return warning;
    }

    public QueryResponse setWarning(String warning) {
        this.warning = warning;
        return this;
    }

    public String getError() {
        return error;
    }

    public QueryResponse setError(String error) {
        this.error = error;
        return this;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public QueryResponse setQueryOptions(QueryOptions queryOptions) {
        this.queryOptions = queryOptions;
        return this;
    }

    public List<DataResult<T>> getResponse() {
        return response;
    }

    public QueryResponse setResponse(List<DataResult<T>> response) {
        this.response = response;
        return this;
    }
}
