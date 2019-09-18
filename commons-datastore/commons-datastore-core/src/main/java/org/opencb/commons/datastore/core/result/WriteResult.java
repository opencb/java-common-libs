package org.opencb.commons.datastore.core.result;

import java.util.ArrayList;
import java.util.List;

public class WriteResult extends AbstractResult {

    private long numMatched;
    private long numInserted;
    private long numUpdated;
    private long numDeleted;
    private List<Fail> failed;

    public WriteResult() {
        this(0, 0, 0, 0, 0, new ArrayList<>(), new ArrayList<>());
    }

    public WriteResult(int dbTime, long numMatched, long numInserted, long numUpdated, long numDeleted, List<String> warnings,
                       List<Fail> failed) {
        super(dbTime, warnings);
        this.numMatched = numMatched;
        this.numInserted = numInserted;
        this.numUpdated = numUpdated;
        this.numDeleted = numDeleted;
        this.failed = failed;
    }

    public static WriteResult empty() {
        return new WriteResult();
    }

    public void append(WriteResult writeResult) {
        this.numMatched += writeResult.numMatched;
        this.numInserted += writeResult.numInserted;
        this.numUpdated += writeResult.numUpdated;
        this.numDeleted += writeResult.numDeleted;
        this.dbTime += writeResult.dbTime;

        if (failed != null && writeResult.getFailed() != null) {
            failed.addAll(writeResult.getFailed());
        }
        if (warnings != null && writeResult.getWarnings() != null) {
            warnings.addAll(writeResult.getWarnings());
        }
    }

    public static class Fail {

        private String id;
        private String message;

        public Fail() {
        }

        public Fail(String id, String message) {
            this.id = id;
            this.message = message;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Fail{");
            sb.append("id='").append(id).append('\'');
            sb.append(", message='").append(message).append('\'');
            sb.append('}');
            return sb.toString();
        }

        public String getId() {
            return id;
        }

        public Fail setId(String id) {
            this.id = id;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public Fail setMessage(String message) {
            this.message = message;
            return this;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WriteResult{");
        sb.append("numMatched=").append(numMatched);
        sb.append(", numInserted=").append(numInserted);
        sb.append(", numUpdated=").append(numUpdated);
        sb.append(", numDeleted=").append(numDeleted);
        sb.append(", failed=").append(failed);
        sb.append(", dbTime=").append(dbTime);
        sb.append(", warnings=").append(warnings);
        sb.append('}');
        return sb.toString();
    }

    public long getNumMatched() {
        return numMatched;
    }

    public WriteResult setNumMatched(long numMatched) {
        this.numMatched = numMatched;
        return this;
    }

    public long getNumInserted() {
        return numInserted;
    }

    public WriteResult setNumInserted(long numInserted) {
        this.numInserted = numInserted;
        return this;
    }

    public long getNumUpdated() {
        return numUpdated;
    }

    public WriteResult setNumUpdated(long numUpdated) {
        this.numUpdated = numUpdated;
        return this;
    }

    public long getNumDeleted() {
        return numDeleted;
    }

    public WriteResult setNumDeleted(long numDeleted) {
        this.numDeleted = numDeleted;
        return this;
    }

    public List<Fail> getFailed() {
        return failed;
    }

    public WriteResult setFailed(List<Fail> failed) {
        this.failed = failed;
        return this;
    }

    @Override
    public WriteResult setDbTime(int dbTime) {
        super.setDbTime(dbTime);
        return this;
    }

    @Override
    public WriteResult setWarnings(List<String> warnings) {
        super.setWarnings(warnings);
        return this;
    }

}
