package org.opencb.commons.bioformats.variant.annotators;

import org.opencb.biodata.models.variant.Variant;

import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantTestAnnotator implements VariantAnnotator {

    private String text;

    public VariantTestAnnotator(String text) {
        this.text = text;
    }

    @Override
    public void annot(List<Variant> batch) {
        for (Variant vr : batch) {
            annot(vr);
        }
    }

    @Override
    public void annot(Variant elem) {
//        elem.addInfoField("TEXT=" + text);
        elem.addAttribute("TEXT", text);

    }
}
