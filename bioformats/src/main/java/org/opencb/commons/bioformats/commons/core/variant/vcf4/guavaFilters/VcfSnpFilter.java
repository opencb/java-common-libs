package org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters;

import com.google.common.base.Predicate;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 10:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfSnpFilter implements Predicate<VcfRecord> {
    @Override
    public boolean apply(VcfRecord vcfRecord) {
        return (!vcfRecord.getId().equalsIgnoreCase(".") && !vcfRecord.getId().equalsIgnoreCase(""));
    }
}
