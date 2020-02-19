package org.opencb.commons.utils;

import java.util.Collection;

/**
 * Created on 10/08/17.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class CollectionUtils {

    @Deprecated
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    @Deprecated
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

}
