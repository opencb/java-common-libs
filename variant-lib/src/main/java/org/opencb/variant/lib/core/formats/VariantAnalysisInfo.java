package org.opencb.variant.lib.core.formats;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/28/13
 * Time: 9:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class VariantAnalysisInfo {

    @JsonProperty
    List<String> samples;
    @JsonProperty
    List<String> consequenceTypes;

    public VariantAnalysisInfo() {

    }

    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }

    public List<String> getConsequenceTypes() {
        return consequenceTypes;
    }

    public void setConsequenceTypes(List<String> consequenceTypes) {
        this.consequenceTypes = consequenceTypes;
    }
}
