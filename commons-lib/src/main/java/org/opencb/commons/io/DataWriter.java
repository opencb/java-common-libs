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

package org.opencb.commons.io;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
@FunctionalInterface
public interface DataWriter<T> {

    default boolean open() {
        return true;
    }

    default boolean close() {
        return true;
    }

    default boolean pre() {
        return true;
    }

    default boolean post() {
        return true;
    }

    default boolean write(T elem) {
        return write(Collections.singletonList(elem));
    }

    boolean write(List<T> batch);

}
