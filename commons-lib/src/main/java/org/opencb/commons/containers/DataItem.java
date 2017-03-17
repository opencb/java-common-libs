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

package org.opencb.commons.containers;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 10/7/13
 * Time: 10:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataItem<T> implements Comparable<DataItem<T>> {
    /**
     * Identifier that allows to sort DataItems stored in a queue.
     */
    private int tokenId;
    private T data;

    public DataItem(int tokenId, T data) {
        this.tokenId = tokenId;
        this.data = data;
    }

    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public int compareTo(DataItem<T> o) {
        return (this.getTokenId() - o.getTokenId());
    }
}
