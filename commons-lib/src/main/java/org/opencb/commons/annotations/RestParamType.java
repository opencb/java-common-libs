package org.opencb.commons.annotations;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RestParamType {
    PATH,
    QUERY,
    BODY;

    // Use this to serialize in lowercase
    @JsonValue
    protected String lowercase() {
        return name().toLowerCase();
    }
}
