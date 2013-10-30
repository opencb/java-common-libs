package org.opencb.commons.bioformats.variant.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    HashMap<String, Integer> consequenceTypes;
    @JsonProperty
    HashMap<String, Integer> biotypes;
    @JsonProperty
    HashMap<String, Double> globalStats;
    @JsonProperty
    HashMap<String, SampleStat> sampleStats;
    @JsonProperty
    HashMap<String, Integer> chromosomes;

    public VariantAnalysisInfo() {
        samples = new ArrayList<>(5);
        consequenceTypes = new LinkedHashMap<>(50);
        biotypes = new LinkedHashMap<>(50);
        globalStats = new LinkedHashMap<>(20);
        sampleStats = new LinkedHashMap<>(5);
        chromosomes = new LinkedHashMap<>(25);

    }

    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }

    public HashMap<String, Integer> getConsequenceTypes() {
        return consequenceTypes;
    }

    public void setConsequenceTypes(HashMap<String, Integer> consequenceTypes) {
        this.consequenceTypes = consequenceTypes;
    }

    public HashMap<String, Integer> getBiotypes() {
        return biotypes;
    }

    public void setBiotypes(HashMap<String, Integer> biotypes) {
        this.biotypes = biotypes;
    }

    public void addSample(String sample) {
        this.samples.add(sample);
    }

    public void addConsequenceType(String ct) {
        int count = 0;
        if (consequenceTypes.containsKey(ct)) {
            count = consequenceTypes.get(ct);
        }

        count++;
        consequenceTypes.put(ct, count);
    }

    public void addBiotype(String bt) {
        if (bt.equals("")) {
            bt = ".";
        }
        int count = 0;
        if (biotypes.containsKey(bt)) {
            count = biotypes.get(bt);
        }

        count++;
        biotypes.put(bt, count);
    }

    public void addGlobalStats(String key, double value) {
        globalStats.put(key, value);

    }

    public void addSampleStats(String sample, int mendelianErrors, int missingGenotypes, int homozygotesNumber) {
        sampleStats.put(sample, new SampleStat(mendelianErrors, missingGenotypes, homozygotesNumber));

    }

    @Override
    public String toString() {
        return "VariantAnalysisInfo{" +
                "samples=" + samples +
                ", consequenceTypes=" + consequenceTypes +
                ", biotypes=" + biotypes +
                '}';
    }

    public void addBiotype(String bt, int count) {
        if (bt.equals("")) {
            bt = ".";
        }

        biotypes.put(bt, count);
    }

    public void addChromosome(String chromosome, int count) {
        chromosomes.put(chromosome, count);
    }

    public void addConsequenceType(String ct, int count) {
        consequenceTypes.put(ct, count);
    }

    private class SampleStat {
        @JsonProperty
        int mendelianErrors;
        @JsonProperty
        int missingGenotypes;
        @JsonProperty
        int homozygotesNumber;

        private SampleStat(int mendelianErrors, int missingGenotypes, int homozygotesNumber) {
            this.mendelianErrors = mendelianErrors;
            this.missingGenotypes = missingGenotypes;
            this.homozygotesNumber = homozygotesNumber;
        }
    }

}
