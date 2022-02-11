package org.opencb.commons.datastore.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataResult<T> {

    protected int time;

    protected List<Event> events;

    protected int numResults;
    protected List<T> results;
    protected String resultType;

    protected long numMatches;
    protected long numInserted;
    protected long numUpdated;
    protected long numDeleted;
    protected long numErrors;

    protected ObjectMap attributes;

    public DataResult() {
    }

    public DataResult(int time, List<Event> events, int numResults, List<T> results, long numMatches) {
        this(time, events, numResults, results, numMatches, 0, 0, 0, new ObjectMap());
    }

    public DataResult(int time, List<Event> events, int numResults, List<T> results, long numMatches, ObjectMap attributes) {
        this(time, events, numResults, results, numMatches, 0, 0, 0, attributes);
    }

    public DataResult(int time, List<Event> events, long numMatches, long numInserted, long numUpdated, long numDeleted) {
        this(time, events, 0, Collections.emptyList(), numMatches, numInserted, numUpdated, numDeleted, new ObjectMap());
    }

    public DataResult(int time, List<Event> events, long numMatches, long numInserted, long numUpdated, long numDeleted, long numErrors) {
        this(time, events, 0, Collections.emptyList(), numMatches, numInserted, numUpdated, numDeleted, numErrors, new ObjectMap());
    }

    public DataResult(int time, List<Event> events, long numMatches, long numInserted, long numUpdated, long numDeleted,
                      ObjectMap attributes) {
        this(time, events, 0, Collections.emptyList(), numMatches, numInserted, numUpdated, numDeleted, attributes);
    }

    public DataResult(int time, List<Event> events, long numMatches, long numInserted, long numUpdated, long numDeleted, long numErrors,
                      ObjectMap attributes) {
        this(time, events, 0, Collections.emptyList(), numMatches, numInserted, numUpdated, numDeleted, numErrors, attributes);
    }

    public DataResult(int time, List<Event> events, int numResults, List<T> results, long numMatches, long numInserted, long numUpdated,
                      long numDeleted, ObjectMap attributes) {
        this(time, events, numResults, results, numMatches, numInserted, numUpdated, numDeleted,
                events == null || events.isEmpty()
                        ? 0
                        : events.stream().filter(e -> Event.Type.ERROR.equals(e.getType())).count(),
                attributes);
    }

    public DataResult(int time, List<Event> events, int numResults, List<T> results, long numMatches, long numInserted, long numUpdated,
                      long numDeleted, long numErrors, ObjectMap attributes) {
        this.time = time;
        this.events = events;
        this.numResults = numResults;
        this.results = results;
        this.numMatches = numMatches;
        this.numInserted = numInserted;
        this.numUpdated = numUpdated;
        this.numDeleted = numDeleted;
        this.numErrors = numErrors;
        this.resultType = results != null && !results.isEmpty() && results.get(0) != null
                ? results.get(0).getClass().getCanonicalName() : "";
        this.attributes = attributes;
    }

    public static DataResult empty() {
        return new DataResult(0, new ArrayList<>(), 0, new ArrayList(), 0, 0, 0, 0, 0, new ObjectMap());
    }

    public T first() {
        if (results != null && results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    public void append(DataResult dataResult) {
        this.numResults += dataResult.numResults;
        this.numMatches += dataResult.numMatches;
        this.numInserted += dataResult.numInserted;
        this.numUpdated += dataResult.numUpdated;
        this.numDeleted += dataResult.numDeleted;
        this.numErrors += dataResult.numErrors;
        this.time += dataResult.time;

        if (this.events != null && dataResult.getEvents() != null) {
            this.events.addAll(dataResult.getEvents());
        }
        if (this.results != null && dataResult.getResults() != null) {
            this.results.addAll(dataResult.getResults());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataResult{");
        sb.append("time=").append(time);
        sb.append(", events=").append(events);
        sb.append(", numResults=").append(numResults);
        sb.append(", results=").append(results);
        sb.append(", resultType='").append(resultType).append('\'');
        sb.append(", numMatches=").append(numMatches);
        sb.append(", numInserted=").append(numInserted);
        sb.append(", numUpdated=").append(numUpdated);
        sb.append(", numDeleted=").append(numDeleted);
        sb.append(", numErrors=").append(numErrors);
        sb.append(", attributes=").append(attributes);
        sb.append('}');
        return sb.toString();
    }

    public int getTime() {
        return time;
    }

    public DataResult<T> setTime(int time) {
        this.time = time;
        return this;
    }

    public List<Event> getEvents() {
        return events;
    }

    public DataResult<T> setEvents(List<Event> events) {
        this.events = events;
        return this;
    }

    public int getNumResults() {
        return numResults;
    }

    public DataResult<T> setNumResults(int numResults) {
        this.numResults = numResults;
        return this;
    }

    public List<T> getResults() {
        return results;
    }

    public DataResult<T> setResults(List<T> results) {
        this.results = results;
        return this;
    }

    public long getNumMatches() {
        return numMatches;
    }

    public DataResult<T> setNumMatches(long numMatches) {
        this.numMatches = numMatches;
        return this;
    }

    public long getNumInserted() {
        return numInserted;
    }

    public DataResult<T> setNumInserted(long numInserted) {
        this.numInserted = numInserted;
        return this;
    }

    public long getNumUpdated() {
        return numUpdated;
    }

    public DataResult<T> setNumUpdated(long numUpdated) {
        this.numUpdated = numUpdated;
        return this;
    }

    public long getNumDeleted() {
        return numDeleted;
    }

    public DataResult<T> setNumDeleted(long numDeleted) {
        this.numDeleted = numDeleted;
        return this;
    }

    public long getNumErrors() {
        return numErrors;
    }

    public DataResult<T> setNumErrors(long numErrors) {
        this.numErrors = numErrors;
        return this;
    }

    public String getResultType() {
        return resultType;
    }

    public DataResult<T> setResultType(String resultType) {
        this.resultType = resultType;
        return this;
    }

    public ObjectMap getAttributes() {
        return attributes;
    }

    public DataResult<T> setAttributes(ObjectMap attributes) {
        this.attributes = attributes;
        return this;
    }
}
