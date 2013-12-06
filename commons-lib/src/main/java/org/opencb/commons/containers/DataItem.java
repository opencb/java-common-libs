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
