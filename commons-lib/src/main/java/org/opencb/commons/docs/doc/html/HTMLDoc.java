package org.opencb.commons.docs.doc.html;

import org.opencb.commons.docs.config.DocConfiguration;
import org.opencb.commons.docs.doc.Doc;
import org.opencb.commons.docs.models.DataClassDoc;

public class HTMLDoc extends Doc {

    public HTMLDoc(DocConfiguration config) {
        super(config);
    }

    @Override
    public String getOverview(DataClassDoc doc) {
        return "";
    }

    @Override
    public String getDataModel(DataClassDoc doc) {
        return "";
    }

    @Override
    public String getSummary(DataClassDoc doc) {
        return "";
    }

    @Override
    public String getRelatedTables(DataClassDoc doc) {
        return "";
    }

    @Override
    public String getExample(DataClassDoc doc) {
        return "";
    }

    @Override
    public void writeDoc(DocConfiguration config) {

    }


}
