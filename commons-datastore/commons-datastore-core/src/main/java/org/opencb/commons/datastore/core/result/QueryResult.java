package org.opencb.commons.datastore.core.result;

import java.util.List;

public class QueryResult<T> extends AbstractResult {

    private long numResults;
    private long numTotalResults;
    private List<T> results;

    public QueryResult() {
    }

    public QueryResult(int dbTime, List<String> warnings, long numResults, long numTotalResults, List<T> results) {
        super(dbTime, warnings);
        this.numResults = numResults;
        this.numTotalResults = numTotalResults;
        this.results = results;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryResult{");
        sb.append("numResults=").append(numResults);
        sb.append(", numTotalResults=").append(numTotalResults);
        sb.append(", results=").append(results);
        sb.append(", dbTime=").append(dbTime);
        sb.append(", warnings=").append(warnings);
        sb.append('}');
        return sb.toString();
    }

    public long getNumResults() {
        return numResults;
    }

    public QueryResult<T> setNumResults(long numResults) {
        this.numResults = numResults;
        return this;
    }

    public long getNumTotalResults() {
        return numTotalResults;
    }

    public QueryResult<T> setNumTotalResults(long numTotalResults) {
        this.numTotalResults = numTotalResults;
        return this;
    }

    public List<T> getResults() {
        return results;
    }

    public QueryResult<T> setResults(List<T> results) {
        this.results = results;
        return this;
    }
}
