package org.opencb.commons.datastore.core;

/**
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public interface QueryParam {

    enum Type {
        STRING,
        TEXT,
        TEXT_ARRAY,
        INTEGER,
        INTEGER_ARRAY,
        DOUBLE,
        DECIMAL,
        DECIMAL_ARRAY,
        BOOLEAN,
        DATE
    }

    String key();

    String description();

    Type type();

}
