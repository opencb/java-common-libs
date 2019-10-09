package org.opencb.commons.datastore.core;

import org.opencb.commons.datastore.core.result.Error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataResponse<T> {

    private String apiVersion;
    private int time;

    private List<String> warnings;
    private Error error;

    private ObjectMap params;
    private List<DataResult<T>> responses;

    public DataResponse() {
    }

    public DataResponse(ObjectMap params, List<DataResult<T>> responses) {
        this("", -1, Collections.emptyList(), null, params, responses);
    }

    public DataResponse(String apiVersion, int time, List<String> warnings, Error error, ObjectMap params, List<DataResult<T>> responses) {
        this.apiVersion = apiVersion;
        this.time = time;
        this.warnings = warnings;
        this.error = error;
        this.params = params;
        this.responses = responses;
    }

    /**
     * Fetch the m-result of the first response.
     * @param m Position of the result from the array of results.
     * @return the m-result of the first response.
     */
    public T result(int m) {
        return result(m, 0);
    }

    /**
     * Fetch the m-result of the n-response.
     * @param m Position of the result from the array of results.
     * @param n Position of the response from the array of responses.
     * @return the m-result of the n-response.
     */
    public T result(int m, int n) {
        return this.responses.get(n).getResults().get(m);
    }

    /**
     * Fetch the list of results of the m-response.
     * @param m Position of the response from the array of responses.
     * @return the list of results of the m-response.
     */
    public List<T> results(int m) {
        return this.responses.get(m).getResults();
    }

    /**
     * Fetch the list of responses.
     * @return the list of responses.
     */
    public List<DataResult<T>> responses() {
        return this.responses;
    }

    /**
     * Fetch the DataResult of the m-response.
     * @param m Position of the response from the array of responses.
     * @return the DataResult of the m-response.
     */
    public DataResult<T> response(int m) {
        return this.responses.get(m);
    }

    /**
     * This method just returns the first DataResult of response, or null if response is null or empty.
     * @return the first DataResult in the response
     */
    public DataResult<T> first() {
        if (responses != null && responses.size() > 0) {
            return responses.get(0);
        }
        return null;
    }

    /**
     * This method returns the first result from the first DataResult of response, this is equivalent to response.get(0).getResult.get(0).
     * @return T value if exists, null otherwise
     */
    public T firstResult() {
        if (responses != null && responses.size() > 0) {
            return responses.get(0).first();
        }
        return null;
    }

    public int allResultsSize() {
        int totalSize = 0;
        if (responses != null && responses.size() > 0) {
            for (DataResult<T> dataResult : responses) {
                totalSize += dataResult.getResults().size();
            }
        }
        return totalSize;
    }

    /**
     * This method flats the two levels (DataResponse and DataResult) into a single list of T.
     * @return a single list with all the results, or null if no response exists
     */
    public List<T> allResults() {
        List<T> results = null;
        if (responses != null && responses.size() > 0) {
            // We first calculate the total size needed
            int totalSize = allResultsSize();

            // We init the list and copy data
            results = new ArrayList<>(totalSize);
            for (DataResult<T> dataResult : responses) {
                results.addAll(dataResult.getResults());
            }
        }
        return results;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataResponse{");
        sb.append("apiVersion='").append(apiVersion).append('\'');
        sb.append(", time=").append(time);
        sb.append(", warnings=").append(warnings);
        sb.append(", error=").append(error);
        sb.append(", params=").append(params);
        sb.append(", responses=").append(responses);
        sb.append('}');
        return sb.toString();
    }


    public String getApiVersion() {
        return apiVersion;
    }

    public DataResponse<T> setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public int getTime() {
        return time;
    }

    public DataResponse<T> setTime(int time) {
        this.time = time;
        return this;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public DataResponse<T> setWarnings(List<String> warnings) {
        this.warnings = warnings;
        return this;
    }

    public Error getError() {
        return error;
    }

    public DataResponse<T> setError(Error error) {
        this.error = error;
        return this;
    }

    public ObjectMap getParams() {
        return params;
    }

    public DataResponse<T> setParams(ObjectMap params) {
        this.params = params;
        return this;
    }

    public List<DataResult<T>> getResponses() {
        return responses;
    }

    public DataResponse<T> setResponses(List<DataResult<T>> responses) {
        this.responses = responses;
        return this;
    }
}
