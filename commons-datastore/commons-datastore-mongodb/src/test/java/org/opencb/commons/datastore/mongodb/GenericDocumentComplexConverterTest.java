package org.opencb.commons.datastore.mongodb;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter.TO_REPLACE_DOTS;

/**
 * Created on 28/04/16
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class GenericDocumentComplexConverterTest {

    @Test
    public void testReplaceDots() throws Exception {
        Document documentWithDots = newDocument(".");
        Document documentWithoutDots = newDocument(TO_REPLACE_DOTS);

        Document documentReplacedDots = GenericDocumentComplexConverter.replaceDots(documentWithDots);
        assertEquals(documentWithoutDots, documentReplacedDots);
    }

    @Test
    public void testRestoreDots() throws Exception {
        Document documentWithDots = newDocument(".");
        Document documentWithoutDots = newDocument(TO_REPLACE_DOTS);

        Document documentRestoredDots = GenericDocumentComplexConverter.restoreDots(documentWithoutDots);
        assertEquals(documentWithDots, documentRestoredDots);
    }

    private Document newDocument(String dot) {
        return newDocument(dot, 3);
    }

    private Document newDocument(String dot, int levels) {
        Document document = new Document("My" + dot + "key",
                new Document("myOtherKey", Arrays.asList(
                        "value.with.dots",
                        "valueWithoutDots",
                        5,
                        5.3,
                        true,
                        'c',
                        new Document("MyKey", 3).append("my" + dot + "other" + dot + "key", "true.true"))));
        if (levels > 0) {
            document.append("recursive", newDocument(dot, levels - 1));
        }
        return document;
    }

}