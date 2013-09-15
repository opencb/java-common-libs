package org.opencb.variant.lib.annot;

import org.opencb.variant.lib.core.formats.VcfRecord;
import org.opencb.variant.lib.io.variant.annotators.VcfAnnotator;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/13/13
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class Annot {


    public static void applyAnnotations(List<VcfRecord> batch, List<VcfAnnotator> annotations){

        for(VcfAnnotator annot: annotations){
            annot.annot(batch);
        }

    }
}
