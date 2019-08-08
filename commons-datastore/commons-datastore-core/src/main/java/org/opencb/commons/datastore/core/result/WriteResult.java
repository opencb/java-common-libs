package org.opencb.commons.datastore.core.result;

import java.util.ArrayList;
import java.util.List;

public class WriteResult extends AbstractResult {

    private long numModified;
    private List<Fail> failed;

    public WriteResult() {
    }

    public WriteResult(String id) {
        this(id, 0, 0, 0, new ArrayList<>(), new ArrayList<>(), null);
    }

    public WriteResult(String id, int dbTime, long numMatches, long numModified, List<Fail> failed, List<Error> warning, Error error) {
        super(id, dbTime, numMatches, warning, error);

        this.numModified = numModified;
        this.failed = failed;
    }

    public void concat(WriteResult writeResult) {
        this.numMatches += writeResult.numMatches;
        this.numModified += writeResult.numModified;
        this.dbTime += writeResult.dbTime;

        if (failed != null && writeResult.getFailed() != null) {
            failed.addAll(writeResult.getFailed());
        }
        if (warning != null && writeResult.getWarning() != null) {
            warning.addAll(writeResult.getWarning());
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
        sb.append("numModified=").append(numModified);
        sb.append(", failed=").append(failed);
        sb.append(", id='").append(id).append('\'');
        sb.append(", dbTime=").append(dbTime);
        sb.append(", numMatches=").append(numMatches);
        sb.append(", warning=").append(warning);
        sb.append(", error=").append(error);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public WriteResult setId(String id) {
        super.setId(id);
        return this;
    }

    public long getNumModified() {
        return numModified;
    }

    public WriteResult setNumModified(long numModified) {
        this.numModified = numModified;
        return this;
    }

    public List<Fail> getFailed() {
        return failed;
    }

    public WriteResult setFailed(List<Fail> failed) {
        this.failed = failed;
        return this;
    }

}
