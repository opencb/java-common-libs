package org.opencb.commons.bioformats.variant.effect;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;
import org.opencb.commons.test.GenericTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class EffectCalculatorTest extends GenericTest {

    private static List<Variant> variants;

    @BeforeClass
    public static void init() {
        variants = new ArrayList<>();
        variants.add(new Variant("15", 89758364, "A", "A,C"));
    }

    @Test
    public void testGetEffects() throws Exception {
        List<VariantEffect> effects = EffectCalculator.getEffects(variants);
        for (VariantEffect ve : effects) {
            System.out.println("ve = " + ve);
        }
    }


    @Test
    public void testGetEffectsWithPolyPhenAndSift() throws Exception {

    }

    @Test
    public void testGetEffectPerVariant() throws Exception {

        List<List<VariantEffect>> effects = EffectCalculator.getEffectPerVariant(variants);
        for (List<VariantEffect> ve : effects) {
            System.out.println(ve);
        }

    }
}
