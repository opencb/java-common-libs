package org.opencb.commons.containers;

import java.util.ArrayList;
import java.util.List;


public class QueryResult<T> {

    private String id;
    private long dbTime;
    private long time;
    private long numResults;
    private String warningMsg;
    private String errorMsg;
    private String featureType;
    private String resultType;
    private List<T> result;


    public QueryResult() {
        this("", -1, -1, -1, "", "", "", null);
    }

    public QueryResult(String id) {
        this(id, -1, -1, -1, "", "", "", new ArrayList<T>());
    }

    public QueryResult(String id, long dbTime, long time, long numResults, String warningMsg, String errorMsg,
                       String featureType, List<T> result) {
        this.id = id;
        this.dbTime = dbTime;
        this.time = time;
        this.numResults = numResults;
        this.warningMsg = warningMsg;
        this.errorMsg = errorMsg;
        this.featureType = featureType;
        this.resultType = result.size() > 0 ? result.get(0).getClass().getCanonicalName() : "";
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

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public void addResult(T result) {
        this.resultType = result.getClass().getCanonicalName();
        this.result.add(result);
    }

    public void addAllResults(List<T> result) {
        if (result.size() > 0) {
            this.resultType = result.get(0).getClass().getCanonicalName();
        }
        this.result.addAll(result);
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    @Override
    public String toString() {
        return "QueryResult{\n" +
                "id='" + id + '\'' + "\n" +
                ", dbTime=" + dbTime + "\n" +
                ", time=" + time + "\n" +
                ", numResults=" + numResults + "\n" +
                ", warningMsg='" + warningMsg + '\'' + "\n" +
                ", errorMsg='" + errorMsg + '\'' + "\n" +
                ", featureType='" + featureType + '\'' + "\n" +
                ", resultType='" + resultType + '\'' + "\n" +
                ", result=" + result + "\n" +
                '}';
    }
}
