package org.opencb.commons.containers.list;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SortedList<T extends Comparable<T>> extends ArrayList<T> {

    public SortedList() {
        super();
    }

    public SortedList(int i) {
        super(i);
    }

    @Override
    public boolean add(T vcfFilter) {
        return this.addList(vcfFilter);
    }

    private boolean addList(T... vcfFilter) {
        boolean res = true;
        if (vcfFilter.length == 1) {
            res = super.add(vcfFilter[0]);
        } else {
            for (T v : vcfFilter) {
                res &= super.add(v);
            }
        }

        if (res) {
            Collections.sort(this);
        }



        return res;
    }
}
