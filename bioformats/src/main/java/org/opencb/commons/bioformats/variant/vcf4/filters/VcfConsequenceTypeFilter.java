package org.opencb.commons.bioformats.variant.vcf4.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.commons.bioformats.variant.vcf4.effect.VariantEffect;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.variant.vcf4.effect.EffectCalculator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: javi
 * Date: 26/09/13
 * Time: 18:33
 * To change this template use File | Settings | File Templates.
 */
public class VcfConsequenceTypeFilter extends VcfFilter {

    private String consequenceType;

    public VcfConsequenceTypeFilter(String consequenceType) {
        this.consequenceType = consequenceType;

    }

    public VcfConsequenceTypeFilter(String consequenceType, int priority) {
        super(priority);
        this.consequenceType = consequenceType;
    }

    @Override
    public boolean apply(VcfRecord vcfRecord) {

        ObjectMapper mapper = new ObjectMapper();
        boolean res = false;

        List<VcfRecord> batch = new ArrayList<>();
        batch.add(vcfRecord);

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
