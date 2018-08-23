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

    protected String id;
    protected int dbTime;
    protected long numMatches;
    protected List<Error> warning;
    protected Error error;

    public AbstractResult() {
    }

    public AbstractResult(String id, int dbTime, long numMatches, List<Error> warning, Error error) {
        this.id = id;
        this.dbTime = dbTime;
        this.numMatches = numMatches;
        this.warning = warning;
        this.error = error;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractResult{");
        sb.append("id='").append(id).append('\'');
        sb.append(", dbTime=").append(dbTime);
        sb.append(", numMatches=").append(numMatches);
        sb.append(", warning=").append(warning);
        sb.append(", error=").append(error);
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

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

    public long getNumMatches() {
        return numMatches;
    }

    public AbstractResult setNumMatches(long numMatches) {
        this.numMatches = numMatches;
        return this;
    }

    public List<Error> getWarning() {
        return warning;
    }

    public AbstractResult setWarning(List<Error> warning) {
        this.warning = warning;
        return this;
    }

    public Error getError() {
        return error;
    }

    public AbstractResult setError(Error error) {
        this.error = error;
        return this;
    }
}
