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

package org.opencb.commons.run;

import java.io.IOException;
import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
@Deprecated
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
