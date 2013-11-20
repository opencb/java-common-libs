package org.opencb.commons.bioformats.variant;

import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;
import org.opencb.commons.bioformats.variant.utils.stats.VariantStats;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 11/20/13
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Variant {
    private String chromosome;
    private long position;
    private String reference;
    private String alternate;
    private String id;
    private String format;
    private String sampleRawData;
    private Map<String, Map<String, String>> sampleData;
    private VariantStats stats;
    private List<VariantEffect> effect;

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAlternate() {
        return alternate;
    }

    public void setAlternate(String alternate) {
        this.alternate = alternate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSampleRawData() {
        return sampleRawData;
    }

    public void setSampleRawData(String sampleRawData) {
        this.sampleRawData = sampleRawData;
    }

    public Map<String, Map<String, String>> getSampleData() {
        return sampleData;
    }

    public void setSampleData(Map<String, Map<String, String>> sampleData) {
        this.sampleData = sampleData;
    }

    public VariantStats getStats() {
        return stats;
    }

    public void setStats(VariantStats stats) {
        this.stats = stats;
    }

    public List<VariantEffect> getEffect() {
        return effect;
    }

    public void setEffect(List<VariantEffect> effect) {
        this.effect = effect;
    }
}
