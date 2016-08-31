package org.opencb.commons.datastore.mongodb;

import org.bson.Document;
import org.junit.Test;
import org.opencb.commons.datastore.core.ObjectMap;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter.TO_REPLACE_DOTS;

/**
 * Created on 28/04/16
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class GenericDocumentComplexConverterTest {

    @Test
    public void testReplaceDots() throws Exception {
        Document documentWithDots = new Document("My.key",
                new Document("myOtherKey", Arrays.asList(
                        "value.with.dots",
                        "valueWithoutDots",
                        5,
                        5.3,
                        true,
                        'c',
                        new Document("MyKey", 3).append("my.other.key", "true.true"))));
        Document documentWithoutDots = new Document("My" + TO_REPLACE_DOTS + "key",
                new Document("myOtherKey", Arrays.asList(
                        "value.with.dots",
                        "valueWithoutDots",
                        5,
                        5.3,
                        true,
                        'c',
                        new Document("MyKey", 3).append("my" + TO_REPLACE_DOTS + "other" + TO_REPLACE_DOTS + "key", "true.true"))));

        Document documentReplacedDots = GenericDocumentComplexConverter.replaceDots(documentWithDots);
        Document documentRestoredDots = GenericDocumentComplexConverter.restoreDots(documentWithDots);

        assertEquals(documentWithDots, documentRestoredDots);
        assertEquals(documentWithoutDots, documentReplacedDots);
    }

}