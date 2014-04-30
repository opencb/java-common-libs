package org.opencb.commons.bioformats.variant;

import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.opencb.commons.test.GenericTest;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantFactoryTest extends GenericTest {

    @Test
    public void testCreateVariantFromVcf() throws Exception {

    }

    @Test
    public void testGetVcfInfo() throws Exception {
        Variant v = new Variant("1", 1, "A", "C");
        v.addAttribute("QUAL", "0.1");
        v.addAttribute("FILTER", "PASS");
        v.addAttribute("DP", "5");
        v.addAttribute("AP", "10");
        v.addAttribute("H2", "");

        System.out.println(VariantFactory.getVcfInfo(v));
        assertEquals(VariantFactory.getVcfInfo(v), "DP=5;AP=10;H2");

        Variant v2 = new Variant("1", 1, "A", "C");
        v2.addAttribute("DP", "5");
        v2.addAttribute("FILTER", "PASS");
        v2.addAttribute("AP", "10");
        v2.addAttribute("H2", "");
        v2.addAttribute("QUAL", "0.1");

        System.out.println(VariantFactory.getVcfInfo(v2));
        assertEquals(VariantFactory.getVcfInfo(v2), "DP=5;AP=10;H2");
    }

    @Test
    public void testGetVcfSampleRawData() throws Exception {
        Variant v = new Variant("1", 1, "A", "C");

        Map<String, String> sample1 = new LinkedHashMap<>();
        Map<String, String> sample2 = new LinkedHashMap<>();
        Map<String, String> sample3 = new LinkedHashMap<>();

        sample1.put("DP", "1");
        sample1.put("GT", "0/1");

        sample2.put("GT", "0/2");
        sample2.put("DP", "2");

        sample3.put("DP", "3");
        sample3.put("GT", "0/3");

        v.setFormat("GT:DP");
        v.addSampleData("sample2", sample2);
        v.addSampleData("sample3", sample3);
        v.addSampleData("sample1", sample1);

        System.out.println("sample1 = " + sample1);
        System.out.println("sample2 = " + sample2);
        System.out.println("sample3 = " + sample3);

        assertEquals(VariantFactory.getVcfSampleRawData(v, "sample1"), "0/1:1");
        assertEquals(VariantFactory.getVcfSampleRawData(v, "sample2"), "0/2:2");
        assertEquals(VariantFactory.getVcfSampleRawData(v, "sample3"), "0/3:3");
    }
}
