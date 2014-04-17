package org.opencb.commons.bioformats.variant.annotators;

import com.google.common.base.Joiner;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;
import org.opencb.commons.bioformats.variant.effect.EffectCalculator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantConsequenceTypeAnnotator implements VariantAnnotator {


    private String ctTag;

    public VariantConsequenceTypeAnnotator() {
        this("ConsType");
    }

    public VariantConsequenceTypeAnnotator(String ctTag) {
        this.ctTag = ctTag;
    }

    @Override
    public void annot(List<Variant> batch) {

        EffectCalculator.setEffects(batch);

        for (Variant variant : batch) {
            annotVariantEffect(variant);
        }

    }

    private void annotVariantEffect(Variant variant) {

        Set<String> ct = new HashSet<>();
        for (VariantEffect effect : variant.getEffect()) {
            if (effect.getConsequenceTypeObo().length() > 0)
                ct.add(effect.getConsequenceTypeObo());
        }

        String ct_all = Joiner.on(",").join(ct);

        if (ct.size() > 0) {
            variant.addAttribute(this.ctTag, ct_all);
        }

    }

    @Override
    public void annot(Variant elem) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
