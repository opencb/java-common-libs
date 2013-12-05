package org.opencb.commons.bioformats.variant.vcf4.filters;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SortedList extends ArrayList<VcfFilter> {

    public SortedList() {
        super();
    }

    public SortedList(int i) {
        super(i);
    }

    @Override
    public boolean add(VcfFilter vcfFilter) {
        return this.addList(vcfFilter);
    }

    private boolean addList(VcfFilter... vcfFilter) {
        boolean res = true;
        if (vcfFilter.length == 1) {
            res = super.add(vcfFilter[0]);
        } else {
            for (VcfFilter v : vcfFilter) {
                res &= super.add(v);
            }
        }

        if (res) {
            Collections.sort(this);
        }
        return res;
    }
}
