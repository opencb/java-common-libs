package org.opencb.commons.run;

import java.io.IOException;
import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public abstract class Task<T> implements Comparable<Task<T>> {

    private int priority;

    public Task() {
        this(0);
    }

    public Task(int priority) {
        this.priority = priority;
    }

    public abstract boolean apply(List<T> batch) throws IOException;

    public boolean pre() {
        return true;
    }

    public boolean post() {
        return true;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(Task<T> elem) {
        return elem.getPriority() - this.priority;
    }

}
