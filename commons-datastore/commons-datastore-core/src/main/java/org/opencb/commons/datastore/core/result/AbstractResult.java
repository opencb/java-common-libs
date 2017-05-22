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

/**
 * Created by imedina on 23/03/17.
 */
public class AbstractResult {

    protected String id;
    protected int dbTime;
    protected int numResults;
    protected long numTotalResults;
    protected String warningMsg;
    protected String errorMsg;

    public AbstractResult() {
    }

    public AbstractResult(String id, int dbTime, int numResults, long numTotalResults, String warningMsg, String errorMsg) {
        this.id = id;
        this.dbTime = dbTime;
        this.numResults = numResults;
        this.numTotalResults = numTotalResults;
        this.warningMsg = warningMsg;
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractResult{");
        sb.append("id='").append(id).append('\'');
        sb.append(", dbTime=").append(dbTime);
        sb.append(", numResults=").append(numResults);
        sb.append(", numTotalResults=").append(numTotalResults);
        sb.append(", warningMsg='").append(warningMsg).append('\'');
        sb.append(", errorMsg='").append(errorMsg).append('\'');
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

    public int getNumResults() {
        return numResults;
    }

    public AbstractResult setNumResults(int numResults) {
        this.numResults = numResults;
        return this;
    }

    public long getNumTotalResults() {
        return numTotalResults;
    }

    public AbstractResult setNumTotalResults(long numTotalResults) {
        this.numTotalResults = numTotalResults;
        return this;
    }

    public String getWarningMsg() {
        return warningMsg;
    }

    public AbstractResult setWarningMsg(String warningMsg) {
        this.warningMsg = warningMsg;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public AbstractResult setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }
}
