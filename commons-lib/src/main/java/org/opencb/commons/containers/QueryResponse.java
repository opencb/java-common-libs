package org.opencb.commons.containers;

import org.opencb.commons.containers.map.ObjectMap;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cgonzalez@cipf.es>
 */
public class QueryResponse extends ObjectMap {

    private static final long serialVersionUID = -2978952531219554024L;

    public QueryResponse() {
        initialize();
    }

    public QueryResponse(int size) {
        super(size);
        initialize();
    }

    public QueryResponse(String key, Object value) {
        super(key, value);
        initialize();
    }

    private void initialize() {
        this.put("dbVersion", "v3");
        this.put("apiVersion", "v2");
        this.put("time", "");
        this.put("warningMsg", "");
        this.put("errorMsg", "");
    }

}
