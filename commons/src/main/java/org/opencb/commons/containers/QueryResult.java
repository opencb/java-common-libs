package org.opencb.commons.containers;


public class QueryResult<T> {

    private String id;
    private long dbTime;
    private long numResults;
    private String warningMsg;
    private String errorMsg;
    private String featureType;
    private String resultType;
    private T result;


    public QueryResult() {
        this.id = "";
        this.dbTime = -1;
        this.numResults = -1;
        this.warningMsg = "";
        this.errorMsg = "";
        this.featureType = "";
        this.resultType = "";
        this.result = null;
    }

    public QueryResult(String id, long dbTime, long numResults, String warningMsg, String errorMsg, String featureType, String resultType, T result) {
        this.id = id;
        this.dbTime = dbTime;
        this.numResults = numResults;
        this.warningMsg = warningMsg;
        this.errorMsg = errorMsg;
        this.featureType = featureType;
        this.resultType = resultType;
        this.result = result;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDbTime() {
        return dbTime;
    }

    public void setDbTime(long dbTime) {
        this.dbTime = dbTime;
    }

    public long getNumResults() {
        return numResults;
    }

    public void setNumResults(long numResults) {
        this.numResults = numResults;
    }

    public String getWarningMsg() {
        return warningMsg;
    }

    public void setWarningMsg(String warningMsg) {
        this.warningMsg = warningMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
