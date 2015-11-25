package org.opencb.commons.datastore.core;

/**
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public interface QueryParam {

    enum Type {
        TEXT, TEXT_ARRAY, INTEGER, INTEGER_ARRAY, DECIMAL, DECIMAL_ARRAY, BOOLEAN
    }

    String key();

    String description();

    Type type();

}
