package org.opencb.commons.bioformats.variant.vcf4.io.writers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.opencb.commons.bioformats.feature.Genotype;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantJsonDataWriter implements VariantWriter {

    private PrintWriter writer;
    private String filename;
    private JsonFactory f;
    private JsonGenerator g;
    private boolean gzip;

    private boolean includeSamples = false;
    private boolean includeStats = false;
    private boolean includeEffect = false;

    public VariantJsonDataWriter(String filename) {
        this(filename, false);
    }

    public VariantJsonDataWriter(String filename, boolean gzip) {
        this.filename = filename;
        this.f = new JsonFactory();
        this.gzip = gzip;
    }

    @Override
    public void includeStats(boolean stats) {
        this.includeStats = stats;

    }

    @Override
    public void includeSamples(boolean samples) {
        this.includeSamples = samples;
    }

    @Override
    public void includeEffect(boolean effect) {
        this.includeEffect = effect;

    }

    @Override
    public boolean open() {

        boolean res = true;

        try {
            if (this.gzip) {
                writer = new PrintWriter(new GZIPOutputStream(new FileOutputStream(this.filename)));
            } else {
                writer = new PrintWriter(filename);
            }

            g = f.createGenerator(writer);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            res = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean close() {
        boolean res = true;
        try {
            g.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }

    @Override
    public boolean pre() {
        boolean res = true;
        g.useDefaultPrettyPrinter();
        try {
            g.writeStartArray();
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }

    @Override
    public boolean post() {
        boolean res = true;
        try {
            g.writeEndArray();
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }

    @Override
    public boolean write(Variant elem) {

        boolean res = true;

//        VariantJson v = new VariantJson(elem);


        try {
            g.writeStartObject();
            g.writeStringField("chr", elem.getChromosome());
            g.writeNumberField("pos", elem.getPosition());
            g.writeStringField("ref", elem.getReference());
            g.writeStringField("alt", elem.getAlternate());
            g.writeStringField("snpId", elem.getId());
            g.writeObjectFieldStart("attributes");
            for (Map.Entry<String, String> entry : elem.getAttributes().entrySet()) {
                g.writeStringField(entry.getKey(), entry.getValue());
            }
            g.writeEndObject();

            if (this.includeSamples) {
                g.writeObjectFieldStart("samples");

                for (Map.Entry<String, Map<String, String>> entry : elem.getSamplesData().entrySet()) {
                    g.writeObjectFieldStart(entry.getKey());
                    for (Map.Entry<String, String> sampleEntry : entry.getValue().entrySet()) {
                        g.writeStringField(sampleEntry.getKey(), sampleEntry.getValue());
                    }
                    g.writeEndObject();
                }

                g.writeEndObject();
            }

            if (this.includeEffect && elem.getEffect() != null) {
                Set<String> cts = new HashSet<>();
                Set<String> genes = new HashSet<>();
                for (VariantEffect ve : elem.getEffect()) {
                    if (ve.getConsequenceTypeObo().length() > 0) {
                        cts.add(ve.getConsequenceTypeObo());
                    }
                    if (ve.getGeneName().length() > 0) {
                        genes.add(ve.getGeneName());
                    }
                }
                g.writeArrayFieldStart("genes");
                for (String gene : genes) {
                    g.writeString(gene);
                }
                g.writeEndArray();
                g.writeArrayFieldStart("effects");
                for (String effect : cts) {
                    g.writeString(effect);
                }
                g.writeEndArray();
            }

            if (this.includeStats) {
                g.writeObjectFieldStart("stats");
                g.writeNumberField("maf", elem.getStats().getMaf());
                g.writeNumberField("mgf", elem.getStats().getMgf());

                g.writeStringField("alleleMaf", elem.getStats().getMafAllele());
                g.writeStringField("genotypeMaf", elem.getStats().getMgfAllele());
                g.writeNumberField("missAllele", elem.getStats().getMissingAlleles());
                g.writeNumberField("missGenotypes", elem.getStats().getMissingGenotypes());
                g.writeNumberField("mendelErr", elem.getStats().getMendelinanErrors());
                g.writeNumberField("casesPercentDominant", elem.getStats().getCasesPercentDominant());
                g.writeNumberField("controlsPercentDominant", elem.getStats().getControlsPercentDominant());
                g.writeNumberField("casesPercentRecessive", elem.getStats().getCasesPercentRecessive());
                g.writeNumberField("controlsPercentRecessive", elem.getStats().getControlsPercentRecessive());
                g.writeObjectFieldStart("genotypeCount");
                for (Genotype genotype : elem.getStats().getGenotypes()) {
                    g.writeNumberField(genotype.getGenotype(), genotype.getCount());
                }
                g.writeEndObject();

                g.writeEndObject();
            }


            g.writeEndObject();
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }

        return res;
    }

    @Override
    public boolean write(List<Variant> batch) {

        for (Variant v : batch) {
            this.write(v);
        }
        return true;
    }


}
