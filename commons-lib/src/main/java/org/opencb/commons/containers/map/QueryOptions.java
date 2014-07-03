package org.opencb.commons.containers.map;

import java.util.List;
import java.util.Map;


@Deprecated
public class QueryOptions extends ObjectMap {


    private static final long serialVersionUID = -6331081481906004636L;


    public QueryOptions() {

    }

    public QueryOptions(String key, Object value) {
        super(key, value);
    }

    public QueryOptions(String json) {
        super(json);
    }

    public QueryOptions(Map<String, ?> inputOptions) {
        this.putAll(inputOptions);
    }
    
    public QueryOptions(Map<String, ?> inputOptions, boolean pickFirstValue) {
        if (pickFirstValue) {
            for (Map.Entry<String, ?> option : inputOptions.entrySet()) {
                if (option.getValue() instanceof List) {
                    this.put(option.getKey(), ((List) option.getValue()).get(0));
                }
            }
        } else {
            this.putAll(inputOptions);
        }
    }
    
}
