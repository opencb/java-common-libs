package org.opencb.commons.bioformats.variant.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;
import org.opencb.commons.bioformats.variant.effect.EffectCalculator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantConsequenceTypeFilter extends VariantFilter {

    private String consequenceType;

    public VariantConsequenceTypeFilter(String consequenceType) {
        this.consequenceType = consequenceType;

    }

    public VariantConsequenceTypeFilter(String consequenceType, int priority) {
        super(priority);
        this.consequenceType = consequenceType;
    }

    @Override
    public boolean apply(Variant variant) {

        ObjectMapper mapper = new ObjectMapper();
        boolean res = false;

        List<Variant> batch = new ArrayList<>();
        batch.add(variant);

        List<VariantEffect> batchEffect = EffectCalculator.getEffects(batch);

        Iterator<VariantEffect> it = batchEffect.iterator();

        VariantEffect effect;
        while (it.hasNext() && !res) {
            effect = it.next();

            if (effect.getConsequenceTypeObo().equalsIgnoreCase(this.consequenceType)) {
                return true;
            }

        }

        return res;
    }
}
