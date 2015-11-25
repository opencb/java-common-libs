package org.opencb.commons.filters;

import com.google.common.base.Predicate;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Filter)) {
            return false;
        }
        Filter<?> filter = (Filter<?>) o;
        return priority == filter.priority;
    }

    @Override
    public int hashCode() {
        return priority;
    }

}
