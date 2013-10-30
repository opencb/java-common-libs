package org.opencb.commons.bioformats.variant.vcf4.annotators;

import com.google.common.base.Joiner;
import org.opencb.commons.bioformats.variant.vcf4.VariantEffect;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.variant.vcf4.effect.EffectCalculator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: javi
 * Date: 26/09/13
 * Time: 18:23
 * To change this template use File | Settings | File Templates.
 */
public class VcfGeneNameAnnotator implements VcfAnnotator {

    @Override
    public void annot(List<VcfRecord> batch) {

        List<VariantEffect> batchEffect = EffectCalculator.getEffects(batch);

        for (VcfRecord variant : batch) {

            annotVariantEffect(variant, batchEffect);
        }

    }

    private void annotVariantEffect(VcfRecord variant, List<VariantEffect> batchEffect) {

        Set<String> geneNames = new HashSet<>();
        for (VariantEffect effect : batchEffect) {

            if (variant.getChromosome().equals(effect.getChromosome()) &&
                    variant.getPosition() == effect.getPosition() &&
                    variant.getReference().equals(effect.getReferenceAllele()) &&
                    variant.getAlternate().equals(effect.getAlternativeAllele())) {

                geneNames.add(effect.getGeneName());
            }

        }

        String geneNamesAll = Joiner.on(",").join(geneNames);

        if (geneNames.size() > 0) {
            variant.addInfoField("GeneNames=" + geneNamesAll);
        }

    }

    @Override
    public void annot(VcfRecord elem) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
