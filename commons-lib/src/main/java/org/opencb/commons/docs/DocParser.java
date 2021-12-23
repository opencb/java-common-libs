package org.opencb.commons.docs;

import org.opencb.commons.docs.config.DocConfiguration;
import org.opencb.commons.docs.doc.Doc;
import org.opencb.commons.docs.doc.DocFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DocParser {


    private static final Logger LOG = LoggerFactory.getLogger(DocParser.class);

    public void parse(DocConfiguration config) throws IOException {
        Doc doc = DocFactory.getDoc(config);
        doc.writeDoc(config);
    }
}
