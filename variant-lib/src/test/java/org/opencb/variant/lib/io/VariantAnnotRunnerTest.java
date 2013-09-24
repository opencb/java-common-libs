package org.opencb.variant.lib.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.variant.lib.io.variant.annotators.VcfAnnotator;
import org.opencb.variant.lib.io.variant.annotators.VcfControlAnnotator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/23/13
 * Time: 4:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantAnnotRunnerTest {

    private String fileIn = "/home/aaleman/tmp/controls/annot.vcf";
    private String fileOut = "/home/aaleman/tmp/controls/annotBier.vcf";
    private String controlFile = "/media/data/controls/bier/bier.gz";


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void annot() {

        VariantAnnotRunner vr = new VariantAnnotRunner(fileIn, fileOut);

        List<VcfAnnotator> list = new ArrayList<>();

        try {
            list.add(new VcfControlAnnotator("BIER", controlFile));

            vr.annotations(list);
            vr.run();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
