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
public class FilterApplicator {

    public static <E> boolean filter(List<E> recordList, List<? extends Filter<E>> filters) {

        Predicate<E> andFilter = Predicates.and(filters);

        List<E> aux = Lists.newArrayList(Iterables.filter(recordList, andFilter));
        return Iterables.retainAll(recordList, aux);

//        return Lists.newArrayList(Iterables.filter(recordList, andFilter));

    }
//
//    public static <E> boolean filter(E elem, List<Filter<E>> filters) {
//
//        Predicate<E> andFilter = Predicates.and(filters);
//
//        return andFilter.apply(elem);
//
//    }
}
