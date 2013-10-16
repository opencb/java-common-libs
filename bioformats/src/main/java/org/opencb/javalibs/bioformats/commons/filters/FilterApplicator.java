package org.opencb.javalibs.bioformats.commons.filters;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.opencb.javalibs.bioformats.variant.vcf4.VcfRecord;
import org.opencb.javalibs.bioformats.variant.vcf4.filters.VcfFilter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 10:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilterApplicator {

    public static List<VcfRecord> filter(List<VcfRecord> recordList, List<VcfFilter> filters) {

        Predicate<VcfRecord> andFilter = Predicates.and(filters);

        return Lists.newArrayList(Iterables.filter(recordList, andFilter));

    }

    public static boolean filter(VcfRecord vcfRecord, List<VcfFilter> filters) {

        Predicate<VcfRecord> andFilter = Predicates.and(filters);

        return andFilter.apply(vcfRecord);

    }
}
