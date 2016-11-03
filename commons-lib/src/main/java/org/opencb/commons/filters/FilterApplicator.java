package org.opencb.commons.filters;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/26/13
 * Time: 10:27 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class FilterApplicator {

    public static <E> boolean filter(List<E> recordList, List<? extends Filter<E>> filters) {

        Predicate<E> andFilter = Predicates.and(filters);

        List<E> aux = Lists.newArrayList(Iterables.filter(recordList, andFilter));
        return Iterables.retainAll(recordList, aux);

    }

    public static <E> boolean filter(List<E> recordList, Filter<E> filter) {

        List<E> aux = Lists.newArrayList(Iterables.filter(recordList, filter));
        return Iterables.retainAll(recordList, aux);

    }

}
