package org.opencb.variant.lib.core.formats;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/10/13
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantEffect {

    private String chromosome;
    private int position;
    private String referenceAllele;
    private String alternativeAllele;
    private String featureId;
    private String featureName;
    private String featureType;
    private String featureBiotype;
    private String featureChromosome;
    private int featureStart;
    private int featureEnd;
    private String featureStrand;
    private String snpId;
    private String ancestral;
    private String alternative;
    private String geneId;
    private String transcriptId;
    private String geneName;
    private String consequenceType;
    private String consequenceTypeObo;
    private String consequenceTypeDesc;
    private String consequenceTypeType;
    private int aaPosition;
    private String aminoacidChange;
    private String codonChange;

    @JsonCreator
    public VariantEffect(@JsonProperty("chromosome") String chromosome,
                         @JsonProperty("position") int position,
                         @JsonProperty("referenceAllele") String referenceAllele,
                         @JsonProperty("alternativeAllele") String alternativeAllele,
                         @JsonProperty("featureId") String featureId,
                         @JsonProperty("featureName") String featureName,
                         @JsonProperty("featureType") String featureType,
                         @JsonProperty("featureBiotype") String featureBiotype,
                         @JsonProperty("featureChromosome") String featureChromosome,
                         @JsonProperty("featureStart") int featureStart,
                         @JsonProperty("featureEnd") int featureEnd,
                         @JsonProperty("featureStrand") String featureStrand,
                         @JsonProperty("snpId") String snpId,
                         @JsonProperty("ancestral") String ancestral,
                         @JsonProperty("alternative") String alternative,
                         @JsonProperty("geneId") String geneId,
                         @JsonProperty("transcriptId") String transcriptId,
                         @JsonProperty("geneName") String geneName,
                         @JsonProperty("consequenceType") String consequenceType,
                         @JsonProperty("consequenceTypeObo") String consequenceTypeObo,
                         @JsonProperty("consequenceTypeDesc") String consequenceTypeDesc,
                         @JsonProperty("consequenceTypeType") String consequenceTypeType,
                         @JsonProperty("aaPosition") int aaPosition,
                         @JsonProperty("aminoacidChange") String aminoacidChange,
                         @JsonProperty("codonChange") String codonChange
                             ) {
        this.chromosome = chromosome;
        this.position = position;
        this.referenceAllele = referenceAllele;
        this.alternativeAllele = alternativeAllele;
        this.featureId = featureId;
        this.featureName = featureName;
        this.featureType = featureType;
        this.featureBiotype = featureBiotype;
        this.featureChromosome = featureChromosome;
        this.featureStart = featureStart;
        this.featureEnd = featureEnd;
        this.featureStrand = featureStrand;
        this.snpId = snpId;
        this.ancestral = ancestral;
        this.alternative = alternative;
        this.geneId = geneId;
        this.transcriptId = transcriptId;
        this.geneName = geneName;
        this.consequenceType = consequenceType;
        this.consequenceTypeObo = consequenceTypeObo;
        this.consequenceTypeDesc = consequenceTypeDesc;
        this.consequenceTypeType = consequenceTypeType;
        this.aaPosition = aaPosition;
        this.aminoacidChange = aminoacidChange;
        this.codonChange = codonChange;
    }




    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getReferenceAllele() {
        return referenceAllele;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
    }

    public String getAlternativeAllele() {
        return alternativeAllele;
    }

    public void setAlternativeAllele(String alternativeAllele) {
        this.alternativeAllele = alternativeAllele;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public String getFeatureBiotype() {
        return featureBiotype;
    }

    public void setFeatureBiotype(String featureBiotype) {
        this.featureBiotype = featureBiotype;
    }

    public String getFeatureChromosome() {
        return featureChromosome;
    }

    public void setFeatureChromosome(String featureChromosome) {
        this.featureChromosome = featureChromosome;
    }

    public int getFeatureStart() {
        return featureStart;
    }

    public void setFeatureStart(int featureStart) {
        this.featureStart = featureStart;
    }

    public int getFeatureEnd() {
        return featureEnd;
    }

    public void setFeatureEnd(int featureEnd) {
        this.featureEnd = featureEnd;
    }

    public String getFeatureStrand() {
        return featureStrand;
    }

    public void setFeatureStrand(String featureStrand) {
        this.featureStrand = featureStrand;
    }

    public String getSnpId() {
        return snpId;
    }

    public void setSnpId(String snpId) {
        this.snpId = snpId;
    }

    public String getAncestral() {
        return ancestral;
    }

    public void setAncestral(String ancestral) {
        this.ancestral = ancestral;
    }

    public String getAlternative() {
        return alternative;
    }

    public void setAlternative(String alternative) {
        this.alternative = alternative;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getConsequenceType() {
        return consequenceType;
    }

    public void setConsequenceType(String consequenceType) {
        this.consequenceType = consequenceType;
    }

    public String getConsequenceTypeObo() {
        return consequenceTypeObo;
    }

    public void setConsequenceTypeObo(String consequenceTypeObo) {
        this.consequenceTypeObo = consequenceTypeObo;
    }

    public String getConsequenceTypeDesc() {
        return consequenceTypeDesc;
    }

    public void setConsequenceTypeDesc(String consequenceTypeDesc) {
        this.consequenceTypeDesc = consequenceTypeDesc;
    }

    public String getConsequenceTypeType() {
        return consequenceTypeType;
    }

    public void setConsequenceTypeType(String consequenceTypeType) {
        this.consequenceTypeType = consequenceTypeType;
    }

    public int getAaPosition() {
        return aaPosition;
    }

    public void setAaPosition(int aaPosition) {
        this.aaPosition = aaPosition;
    }

    public String getAminoacidChange() {
        return aminoacidChange;
    }

    public void setAminoacidChange(String aminoacidChange) {
        this.aminoacidChange = aminoacidChange;
    }

    public String getCodonChange() {
        return codonChange;
    }

    public void setCodonChange(String codonChange) {
        this.codonChange = codonChange;
    }

    @Override
    public String toString() {
        return "VariantEffect{" +
                "chromosome='" + chromosome + '\'' +
                ", position=" + position +
                ", referenceAllele='" + referenceAllele + '\'' +
                ", alternativeAllele='" + alternativeAllele + '\'' +
                ", featureId='" + featureId + '\'' +
                ", featureName='" + featureName + '\'' +
                ", featureType='" + featureType + '\'' +
                ", featureBiotype='" + featureBiotype + '\'' +
                ", featureChromosome='" + featureChromosome + '\'' +
                ", featureStart=" + featureStart +
                ", featureEnd=" + featureEnd +
                ", featureStrand='" + featureStrand + '\'' +
                ", snpId='" + snpId + '\'' +
                ", ancestral='" + ancestral + '\'' +
                ", alternative='" + alternative + '\'' +
                ", geneId='" + geneId + '\'' +
                ", transcriptId='" + transcriptId + '\'' +
                ", geneName='" + geneName + '\'' +
                ", consequenceType='" + consequenceType + '\'' +
                ", consequenceTypeObo='" + consequenceTypeObo + '\'' +
                ", consequenceTypeDesc='" + consequenceTypeDesc + '\'' +
                ", consequenceTypeType='" + consequenceTypeType + '\'' +
                ", aaPosition=" + aaPosition +
                ", aminoacidChange='" + aminoacidChange + '\'' +
                ", codonChange=" + codonChange +
                '}';
    }
}
