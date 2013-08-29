package org.opencb.commons.bioformats.commons.core.vcffilter;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.guavaFilters.VcfFilter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 10:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfRecordFilters {

    public static List<VcfRecord> filter(List<VcfRecord> list_records, List<VcfFilter> filters){

        Predicate<VcfRecord> and_filters = Predicates.and(filters);

        return Lists.newArrayList(Iterables.filter(list_records, and_filters));

    }

    public static boolean filter(VcfRecord vcf_record, List<VcfFilter> filters){

           Predicate<VcfRecord> and_filters = Predicates.and(filters);

        return and_filters.apply(vcf_record);

    }
}
