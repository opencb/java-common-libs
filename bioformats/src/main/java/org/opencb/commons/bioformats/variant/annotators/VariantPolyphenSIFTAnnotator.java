package org.opencb.commons.bioformats.variant.annotators;

import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.effect.EffectCalculator;
import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;

import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantPolyphenSIFTAnnotator implements VariantAnnotator {

    private String polyphenScoreTag;
    private String polyphenEffectTag;
    private String siftScoreTag;
    private String siftEffectTag;


    public VariantPolyphenSIFTAnnotator() {
        this("PolyphenScore","PolyphenEffect", "SIFTScore", "SIFTEffect");
    }

    public VariantPolyphenSIFTAnnotator(String polyphenScoreTag, String polyphenEffectTag, String siftScoreTag, String siftEffectTag) {
        this.polyphenScoreTag = polyphenScoreTag;
        this.polyphenEffectTag = polyphenEffectTag;
        this.siftScoreTag = siftScoreTag;
        this.siftEffectTag = siftEffectTag;
    }

    @Override
    public void annot(List<Variant> batch) {

        EffectCalculator.setEffects(batch, true, true);

        for (Variant variant : batch) {
            annotPolyphenSIFT(variant);
        }

    }

    private void annotPolyphenSIFT(Variant variant) {

        if (!variant.containsAttribute(this.polyphenScoreTag)) {
            annotPolyphen(variant);
        }

        if (!variant.containsAttribute(this.siftScoreTag)) {
            annotSIFT(variant);
        }

    }

    private void annotPolyphen(Variant variant) {

        double poly = -1;
        int effect = 0;

        for (VariantEffect ve : variant.getEffect()) {
            if (ve.getPolyphenScore() != -1 && ve.getPolyphenScore() > poly) {
                poly = ve.getPolyphenScore();
                effect = ve.getPolyphenEffect();
            }
        }
        if (poly >= 0) {
            variant.addAttribute(this.polyphenScoreTag, String.valueOf(poly));
            variant.addAttribute(this.polyphenEffectTag, String.valueOf(effect));

        }
    }

    private void annotSIFT(Variant variant) {

        double sift = 2;
        int effect = 0;

        for (VariantEffect ve : variant.getEffect()) {
            if (ve.getSiftScore() != -1 && ve.getSiftScore() < sift) {
                sift = ve.getSiftScore();
            }
        }
        if (sift <= 1) {
            variant.addAttribute(this.siftScoreTag, String.valueOf(sift));
            variant.addAttribute(this.siftEffectTag, String.valueOf(effect));
        }
    }


    @Override
    public void annot(Variant elem) {

    }
}
