package org.opencb.commons.bioformats.variant.vcf4.filters;

import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.filters.Filter;

/**
 * Created by aaleman on 12/5/13.
 */
public abstract class VcfFilter extends Filter<VcfRecord> {

    public VcfFilter(){
        super(0);
    }
    public VcfFilter(int priority) {
        super(priority);
    }
}
