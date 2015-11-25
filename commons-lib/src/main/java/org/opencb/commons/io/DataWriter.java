package org.opencb.commons.io;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
@FunctionalInterface
public interface DataWriter<T> {

    default boolean open() {
        return true;
    }

    default boolean close() {
        return true;
    }

    default boolean pre() {
        return true;
    }

    default boolean post() {
        return true;
    }

    default boolean write(T elem) {
        return write(Collections.singletonList(elem));
    }

    boolean write(List<T> batch);

}
