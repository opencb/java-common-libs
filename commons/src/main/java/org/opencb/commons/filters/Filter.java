package org.opencb.commons.filters;

import com.google.common.base.Predicate;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 5:38 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Filter<E> implements Predicate<E>, Comparable<Filter> {
    private int priority;

    protected Filter() {
        this(0);
    }

    protected Filter(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {

        this.priority = priority;
    }

    @Override
    public int compareTo(Filter v) {
        return -(this.priority - v.getPriority());
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
