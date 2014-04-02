package org.opencb.commons.bioformats.variant;

import java.util.*;

import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;
import org.opencb.commons.bioformats.variant.utils.stats.VariantStats;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class Variant {
    private String chromosome;
    private int position;
    private String reference;
    private String alternate;
    private String id;
    private String format;
    private Map<String, Map<String, String>> samplesData;
    private VariantStats stats;
    private List<VariantEffect> effect;

    /**
     * Optional attributes that probably depend on the format of the file the
     * variant was initially read.
     */
    private Map<String, String> attributes;

    public Variant(String chromosome, int position, String reference, String alternate) {
        this.setChromosome(chromosome);
        this.setPosition(position);
        this.setReference(reference);
        this.setAlternate(alternate);

        this.samplesData = new LinkedHashMap<>();
//        this.effect = new ArrayList<>();
        this.attributes = new LinkedHashMap<>();
    }

    public String getChromosome() {
        return chromosome;
    }

    public final void setChromosome(String chromosome) {
        this.chromosome = chromosome.replaceAll("chrom | chrm | chr | ch", "");
    }

    public int getPosition() {
        return position;
    }

    public final void setPosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Position must be positive");
        }

        this.position = position;
    }

    public String getReference() {
        return reference;
    }

    public final void setReference(String reference) {
        this.reference = reference;
    }

    public String getAlternate() {
        return alternate;
    }

    public final void setAlternate(String alternate) {
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

    public Map<String, Map<String, String>> getSamplesData() {
        return samplesData;
    }

    public Map<String, String> getSampleData(String sampleName) {
        return samplesData.get(sampleName);
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

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public boolean addEffect(VariantEffect e) {
        if (this.effect == null) {
            this.effect = new ArrayList<>();
        }
        return this.effect.add(e);
    }



    public void addId(String newId) {
        if (!this.id.contains(newId)) {
            if (this.id.equals(".")) {
                this.id = newId;
            } else {
                this.id += ";" + newId;
            }
        }
    }

    public void addAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return this.attributes.get(key);
    }

    public boolean containsAttribute(String key) {
        return this.attributes.containsKey(key);
    }

    public void addSampleData(String sampleName, Map<String, String> sampleData) {
        this.samplesData.put(sampleName, sampleData);
    }

    public String getSampleData(String sampleName, String field) {
        return this.samplesData.get(sampleName).get(field.toUpperCase());
    }

    public Iterable<String> getSampleNames() {
        return this.samplesData.keySet();
    }

    @Override
    public String toString() {
        return "Variant{" +
                "chromosome='" + chromosome + '\'' +
                ", position=" + position +
                ", reference='" + reference + '\'' +
                ", alternate='" + alternate + '\'' +
                ", id='" + id + '\'' +
                ", format='" + format + '\'' +
                ", samplesData=" + samplesData +
                ", stats=" + stats +
                ", effect=" + effect +
                ", attributes=" + attributes +
                '}';
    }

    public String[] getAltAlleles() {
        return this.getAlternate().split(",");
    }

    public boolean isIndel() {
        return (this.reference.length() > 1 || this.alternate.length() > 1) && (this.reference.length() != this.alternate.length());
    }

}
