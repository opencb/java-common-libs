package org.opencb.commons.bioformats.commons.core.variant.vcf4;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/27/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class Genotypes {

    public static void addGenotypeToList(List<Genotype> list, Genotype g) {

        Genotype aux_g;
        int index = list.indexOf(g);

        if (index >= 0) {
            aux_g = list.get(index);
            aux_g.setCount(aux_g.getCount() + 1);
        } else {
            g.setCount(g.getCount() + 1);
            list.add(g);
        }

    }
}
