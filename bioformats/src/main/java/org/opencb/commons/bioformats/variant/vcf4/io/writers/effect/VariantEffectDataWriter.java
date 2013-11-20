package org.opencb.commons.bioformats.variant.vcf4.io.writers.effect;

import org.opencb.commons.bioformats.commons.DataWriter;
import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 10/24/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VariantEffectDataWriter extends DataWriter {
    boolean writeVariantEffect(List<VariantEffect> batchEffect);

}
