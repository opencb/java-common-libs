package org.opencb.commons.containers;

import java.util.HashMap;
import java.util.Map;
import org.opencb.commons.containers.map.QueryOptions;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cgonzalez@cipf.es>
 */
@Deprecated
public class QueryResponse {//extends ObjectMap {

    private static final long serialVersionUID = -2978952531219554024L;

    private String dbVersion;
    private String apiVersion;
    private String version;
    private String species;
    
    private Long time;
    
    private String warningMsg;
    private String errorMsg;
    
    private Map other;

    private QueryOptions options;
    private Object response;
    
    public QueryResponse() {
        this(null, null);
    }
    
    public QueryResponse(QueryOptions options, Object response) {
        this(options, response, null, null, null);
    }

    public QueryResponse(QueryOptions options, Object response, String version, String species, Long time) {
        this.dbVersion = "v3";
        this.apiVersion = "v2";
        this.warningMsg = "";
        this.errorMsg = "";
        this.options = options;
        this.response = response;
        this.version = version;
        this.species = species;
        this.time = time;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public QueryOptions getOptions() {
        return options;
    }

    public void setOptions(QueryOptions options) {
        this.options = options;
    }

    public Map getOther() {
        return other;
    }

    public void setOther(Map other) {
        this.other = other;
    }

    public void addOther(String key, Object value) {
        if (this.other == null) {
            this.other = new HashMap<>();
        }
        this.other.put(key, value);
    }
    
    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }
    
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWarningMsg() {
        return warningMsg;
    }

    public void setWarningMsg(String warningMsg) {
        this.warningMsg = warningMsg;
    }

    
}
