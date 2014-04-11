package org.opencb.commons.bioformats.variant.filters;

import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.vcf4.io.readers.VariantReader;
import org.opencb.commons.bioformats.variant.vcf4.io.readers.VariantVcfReader;

import java.util.*;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantRetainFilter extends VariantFilter {

    private List<Variant> variants;

    public VariantRetainFilter(String filename) {
        System.out.println("filename = " + filename);
        variants = loadVariants(filename);
        System.out.println("Variants loaded");

    }

    private List<Variant> loadVariants(String filename) {
        List<Variant> res = new ArrayList<>(1000);
        List<Variant> batch;
        VariantReader vr = new VariantVcfReader(filename);
        vr.open();
        vr.pre();

        batch = vr.read(1000);
        while (!batch.isEmpty()) {
            res.addAll(batch);
            batch = vr.read(1000);
        }
        vr.post();
        vr.close();

        return res;
    }

    @Override
    public boolean apply(Variant variant) {
        return listContainsVariant(variant);
    }

    private boolean listContainsVariant(Variant variant) {
        boolean res = false;
        Iterator<Variant> it = variants.iterator();
        Variant v;
        while (it.hasNext() && !res) {
            v = it.next();
            res = compareVariants(v, variant);
        }

        return res;
    }

    private boolean compareVariants(Variant v1, Variant v2) {
        boolean res = true;
        res &= v1.getChromosome().equals(v2.getChromosome());
        res &= v1.getPosition() == v2.getPosition();
        res &= (Arrays.asList(v1.getAltAlleles()).containsAll(Arrays.asList(v2.getAltAlleles())));
        return res;
    }
}
