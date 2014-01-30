package org.opencb.commons.bioformats.variant.vcf4.annotators;

import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 10/24/13
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfTestAnnotator implements VcfAnnotator {

    private String text;

    public VcfTestAnnotator(String text) {
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
