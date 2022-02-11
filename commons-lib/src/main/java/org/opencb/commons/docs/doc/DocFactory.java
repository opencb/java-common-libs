package org.opencb.commons.docs.doc;

import org.opencb.commons.docs.config.DocConfiguration;
import org.opencb.commons.docs.doc.markdown.MarkdownDoc;

public class DocFactory {

    public static Doc getDoc(DocConfiguration config) {
        switch (config.getType()) {
            case MARKDOWN:
            default:
                return new MarkdownDoc(config);
        }
    }


    public enum DocFactoryType {
        MARKDOWN
    }
}
