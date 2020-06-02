/*
 * Copyright 2015-2017 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.commons.datastore.core.result;

import java.util.List;

/**
 * Created by imedina on 23/03/17.
 */
public class AbstractResult {

    @Deprecated
    protected String id;
    protected int dbTime;
    @Deprecated
    protected long numMatches;
    protected List<String> warnings;
    @Deprecated
    protected Error error;

    public AbstractResult() {
    }

    @Deprecated
    public AbstractResult(String id, int dbTime, long numMatches, List<String> warnings, Error error) {
        this.id = id;
        this.dbTime = dbTime;
        this.numMatches = numMatches;
        this.warnings = warnings;
        this.error = error;
    }

    public AbstractResult(int dbTime, List<String> warnings) {
        this.dbTime = dbTime;
        this.warnings = warnings;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractResult{");
        sb.append("dbTime=").append(dbTime);
        sb.append(", warnings=").append(warnings);
        sb.append('}');
        return sb.toString();
    }

    @Deprecated
    public String getId() {
        return id;
    }

    @Deprecated
    public AbstractResult setId(String id) {
        this.id = id;
        return this;
    }

    public int getDbTime() {
        return dbTime;
    }

    public AbstractResult setDbTime(int dbTime) {
        this.dbTime = dbTime;
        return this;
    }

    @Deprecated
    public long getNumMatches() {
        return numMatches;
    }

    @Deprecated
    public AbstractResult setNumMatches(long numMatches) {
        this.numMatches = numMatches;
        return this;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public AbstractResult setWarnings(List<String> warnings) {
        this.warnings = warnings;
        return this;
    }

    @Deprecated
    public Error getError() {
        return error;
    }

    @Deprecated
    public AbstractResult setError(Error error) {
        this.error = error;
        return this;
    }
}
