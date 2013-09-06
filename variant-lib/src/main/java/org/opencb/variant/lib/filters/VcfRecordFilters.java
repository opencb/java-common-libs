package org.opencb.variant.lib.filters;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.opencb.variant.lib.filters.customfilters.VcfFilter;
import org.opencb.variant.lib.core.formats.VcfRecord;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 10:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfRecordFilters {

    public static List<VcfRecord> filter(List<VcfRecord> recordList, List<VcfFilter> filters){

        Predicate<VcfRecord> andFilter = Predicates.and(filters);

        return Lists.newArrayList(Iterables.filter(recordList, andFilter));

    }

    public static boolean filter(VcfRecord vcfRecord, List<VcfFilter> filters){

           Predicate<VcfRecord> andFilter = Predicates.and(filters);

        return andFilter.apply(vcfRecord);

    }
}
