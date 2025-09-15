package org.opencb.commons.datastore.mongodb;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class SmartSplitTest {

    @Test
    public void testSmartSplitWithQuotedCommaValues() {
        String input = "\"a \",\" b\",\" c \"";
        List<String> result = MongoDBQueryUtils.smartSplit(input, ",");
        
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    public void testSmartSplitWithQuotedSemicolonValues() {
        String input = "\"a \";\" b\";\" c \"";
        List<String> result = MongoDBQueryUtils.smartSplit(input, ";");
        
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    public void testSmartSplitWithNonQuotedValues() {
        String input = "a,b,c";
        List<String> result = MongoDBQueryUtils.smartSplit(input, ",");
        
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    public void testSmartSplitWithNonQuotedValuesAndSpaces() {
        String input = "a, b,c ";
        List<String> result = MongoDBQueryUtils.smartSplit(input, ",");

        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    public void testSmartSplitWithMixedValues() {
        String input = "\"a, with comma\",b,\"c\"";
        List<String> result = MongoDBQueryUtils.smartSplit(input, ",");
        
        assertEquals(3, result.size());
        assertEquals("a, with comma", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }
}
