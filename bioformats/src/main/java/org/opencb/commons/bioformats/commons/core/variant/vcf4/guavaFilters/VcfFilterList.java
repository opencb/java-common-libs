package org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfFilterList extends ArrayList<VcfFilter> {

    public VcfFilterList(){
        super();
    }
    public VcfFilterList(int i) {
        super(i);
    }

    @Override
    public boolean add(VcfFilter vcfFilter) {
        boolean res = super.add(vcfFilter);
        if(res){
            Collections.sort(this);
        }
        return res;
    }
}
