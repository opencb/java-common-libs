package org.opencb.commons.bioformats.variant.vcf4.io.readers;

import org.opencb.commons.bioformats.feature.Genotype;
import org.opencb.commons.bioformats.feature.Genotypes;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.utils.stats.VariantStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantVcfEVSReader extends VariantVcfReader implements VariantReader {

    public VariantVcfEVSReader(String filename) {
        super(filename);
    }

    @Override
    public Variant read() {
        Variant variant = super.read();

        if (variant != null) {
            VariantStats stats = new VariantStats();
            stats.setChromosome(variant.getChromosome());
            stats.setPosition(variant.getPosition());
            stats.setRefAllele(variant.getReference());
            stats.setAltAlleles(variant.getAltAlleles());
            if (variant.containsAttribute("MAF")) {
                String splitsMaf[] = variant.getAttribute("MAF").split(",");
                if (splitsMaf.length == 3) {
                    float maf = Float.parseFloat(splitsMaf[2]) / 100;
                    stats.setMaf(maf);
                }
            }

            if (variant.containsAttribute("GTS") && variant.containsAttribute("GTC")) {
                String splitsGTS[] = variant.getAttribute("GTS").split(",");
                String splitsGTC[] = variant.getAttribute("GTC").split(",");

                List<Genotype> genotypeList = new ArrayList<>();

                if (splitsGTC.length == splitsGTS.length) {

                    for (int i = 0; i < splitsGTC.length; i++) {
                        String gt = splitsGTS[i];
                        int gtCount = Integer.parseInt(splitsGTC[i]);
                        Genotype g = null;
                        if (gt.length() == 1) {
                            g = new Genotype(gt + "/" + gt, variant.getReference(), variant.getAlternate());
                        } else if (gt.length() == 2) {
                            g = new Genotype(gt.charAt(0) + "/" + gt.charAt(1), variant.getReference(), variant.getAlternate());
                        } else {
                            ;
                        }

                        if (g != null) {
                            g.setCount(gtCount);
                            Genotypes.addGenotypeToList(genotypeList, g);
                        }


                    }

                    stats.setMafAllele("");
                    stats.setMissingAlleles(0);
                    stats.setGenotypes(genotypeList);

                }

            }
            variant.setStats(stats);

        }

        return variant;
    }
}
