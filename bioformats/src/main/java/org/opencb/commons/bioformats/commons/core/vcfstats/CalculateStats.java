package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.opencb.commons.bioformats.commons.core.connectors.variant.VcfDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.VcfDataWriter;
import org.opencb.commons.bioformats.commons.core.feature.Individual;
import org.opencb.commons.bioformats.commons.core.feature.Pedigree;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/26/13
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalculateStats {

    public static List<VcfRecordStat> variantStats(List<VcfRecord> vcfRecordsList, List<String> sampleNames, Pedigree ped, VcfGlobalStat globalStats) {
        List<VcfRecordStat> statList = new ArrayList<>(vcfRecordsList.size());

        // Temporary variables for file stats updating
        int variantsCount = 0, samplesCount = 0, snpsCount = 0, indelsCount = 0, passCount = 0;
        int transitionsCount = 0, transversionsCount = 0, biallelicsCount = 0, multiallelicsCount = 0;
        float accumQuality = 0;


        int cont = 0;
        for (VcfRecord vcfRecord : vcfRecordsList) {

            int genotypeCurrentPos;
            int totalAllelesCount = 0;
            int totalGenotypesCount = 0;
            String mgfGenotype = "";
            Individual ind;

            float maf = Float.MAX_VALUE;
            float mgf = Float.MAX_VALUE;
            float currentGtFreq;


            float controlsDominant = 0;
            float casesDominant = 0;
            float controlsRecessive = 0;
            float casesRecessive = 0;


            VcfRecordStat vcfStat = new VcfRecordStat();

            vcfStat.setChromosome(vcfRecord.getChromosome());
            vcfStat.setPosition((long) vcfRecord.getPosition());
            vcfStat.setRefAllele(vcfRecord.getReference());

            String[] altAlleles = vcfRecord.getAltAlleles();

            vcfStat.setAltAlleles(altAlleles);
            vcfStat.setNumAlleles(altAlleles.length + 1);

            int[] allelesCount = new int[vcfStat.getNumAlleles()];
            int[] genotypesCount = new int[vcfStat.getNumAlleles() * vcfStat.getNumAlleles()];
            float[] allelesFreq = new float[vcfStat.getNumAlleles()];
            float[] genotypesFreq = new float[vcfStat.getNumAlleles() * vcfStat.getNumAlleles()];

            for (String sampleName : sampleNames) {

                Genotype g = vcfRecord.getSampleGenotype(sampleName);

                Genotypes.addGenotypeToList(vcfStat.getGenotypes(), g);

                // Check missing alleles and genotypes
                switch (g.getCode()) {
                    case ALLELES_OK:
                        // Both alleles set
                        genotypeCurrentPos = g.getAllele1() * (vcfStat.getNumAlleles()) + g.getAllele2();

                        allelesCount[g.getAllele1()]++;
                        allelesCount[g.getAllele2()]++;
                        genotypesCount[genotypeCurrentPos]++;

                        totalAllelesCount += 2;
                        totalGenotypesCount++;
                        break;
                    case HAPLOID:
                        // Haploid (chromosome X/Y)
                        allelesCount[g.getAllele1()]++;
                        totalAllelesCount++;
                        break;
                    default:
                        // Missing genotype (one or both alleles missing)
                        vcfStat.setMissingGenotypes(vcfStat.getMissingGenotypes() + 1);
                        if (g.getAllele1() == null) {
                            vcfStat.setMissingAlleles(vcfStat.getMissingAlleles() + 1);
                        } else {
                            allelesCount[g.getAllele1()]++;
                            totalAllelesCount++;
                        }

                        if (g.getAllele2() == null) {
                            vcfStat.setMissingAlleles(vcfStat.getMissingAlleles() + 1);
                        } else {
                            allelesCount[g.getAllele2()]++;
                            totalAllelesCount++;

                        }
                        break;

                }

               // Include statistics that depend on pedigree information
                if (ped != null) {
                    if (g.getCode() == AllelesCode.ALLELES_OK || g.getCode() == AllelesCode.HAPLOID) {
                        ind = ped.getIndividual(sampleName);
                        if (isMendelianError(ind, g, vcfRecord)) {
                            vcfStat.setMendelinanErrors(vcfStat.getMendelinanErrors() + 1);

                        }
                        if (g.getCode() == AllelesCode.ALLELES_OK) {

                            // Check inheritance models
                            if (ind.getCondition() == Condition.UNAFFECTED) {
                                if (g.isAllele1Ref() && g.isAllele2Ref()) { // 0|0
                                    controlsDominant++;
                                    controlsRecessive++;

                                } else if ((g.isAllele1Ref() && !g.isAllele2Ref()) || (!g.isAllele1Ref() || g.isAllele2Ref())) { // 0|1 or 1|0
                                    controlsRecessive++;

                                }
                            } else if (ind.getCondition() == Condition.AFFECTED) {
                                if (!g.isAllele1Ref() && !g.isAllele2Ref() && g.getAllele1() == g.getAllele2()) {// 1|1, 2|2, and so on
                                    casesRecessive++;
                                    casesDominant++;
                                } else if (!g.isAllele1Ref() || !g.isAllele2Ref()) { // 0|1, 1|0, 1|2, 2|1, 1|3, and so on
                                    casesDominant++;

                                }
                            }

                        }

                    }
                }

            }  // Finish all samples loop


            // MAF
            for (int i = 0; i < vcfStat.getNumAlleles(); i++) {
                allelesFreq[i] = (totalAllelesCount > 0) ? allelesCount[i] / (float) totalAllelesCount : 0;
                if (allelesFreq[i] < maf) {
                    maf = allelesFreq[i];
                    vcfStat.setMafAllele((i == 0) ? vcfStat.getRefAlleles() : vcfStat.getAltAlleles()[i - 1]);
                }
            }

            vcfStat.setMaf(maf);

            for (int i = 0; i < vcfStat.getNumAlleles() * vcfStat.getNumAlleles(); i++) {
                genotypesFreq[i] = (totalGenotypesCount > 0) ? genotypesCount[i] / (float) totalGenotypesCount : 0;


            }


            // MGF
            for (int i = 0; i < vcfStat.getNumAlleles(); i++) {
                for (int j = 0; j < vcfStat.getNumAlleles(); j++) {
                    int idx1 = i * vcfStat.getNumAlleles() + j;
                    if (i == j) {
                        currentGtFreq = genotypesFreq[idx1];
                    } else {
                        int idx2 = j * vcfStat.getNumAlleles() + i;
                        currentGtFreq = genotypesFreq[idx1] + genotypesFreq[idx2];
                    }

                    if (currentGtFreq < mgf) {
                        String firstAllele = (i == 0) ? vcfStat.getRefAlleles() : vcfStat.getAltAlleles()[i - 1];
                        String secondAllele = (j == 0) ? vcfStat.getRefAlleles() : vcfStat.getAltAlleles()[j - 1];
                        mgfGenotype = firstAllele + "|" + secondAllele;
                        mgf = currentGtFreq;

                    }
                }
            }

            vcfStat.setMgf(mgf);
            vcfStat.setMgfAllele(mgfGenotype);

            vcfStat.setAllelesCount(allelesCount);
            vcfStat.setGenotypesCount(genotypesCount);

            vcfStat.setAllelesFreq(allelesFreq);
            vcfStat.setGenotypesFreq(genotypesFreq);

         // INDELS
         /*
         * 3 possibilities for being an INDEL:
         * - The value of the ALT field is <DEL> or <INS>
         * - The REF allele is not . but the ALT is
         * - The REF allele is . but the ALT is not
         * - The REF field length is different than the ALT field length
         */
            if ((!vcfStat.getRefAlleles().equals(".") && vcfRecord.getAlternate().equals(".")) ||
                    (vcfRecord.getAlternate().equals(".") && !vcfStat.getRefAlleles().equals(".")) ||
                    (vcfRecord.getAlternate().equals("<INS>")) ||
                    (vcfRecord.getAlternate().equals("<DEL>")) ||
                    vcfRecord.getReference().length() != vcfRecord.getAlternate().length()) {
                vcfStat.setIndel(true);
                indelsCount++;
            } else {
                vcfStat.setIndel(false);
            }

            // Transitions and transversions

            String ref = vcfRecord.getReference().toUpperCase();
            for (String alt : vcfRecord.getAltAlleles()) {
                alt = alt.toUpperCase();

                if (ref.length() == 1 && alt.length() == 1) {

                    switch (ref) {
                        case "C":
                            if (alt.equals("T")) {
                                transitionsCount++;
                            } else {
                                transversionsCount++;
                            }
                            break;
                        case "T":
                            if (alt.equals("C")) {
                                transitionsCount++;
                            } else {
                                transversionsCount++;
                            }
                            break;
                        case "A":
                            if (alt.equals("G")) {
                                transitionsCount++;

                            } else {
                                transversionsCount++;
                            }
                            break;
                        case "G":
                            if (alt.equals("A")) {
                                transitionsCount++;
                            } else {
                                transversionsCount++;
                            }
                            break;
                    }
                }

            }

            // Update variables finally used to update file_stats_t structure
            variantsCount++;
            if (!vcfRecord.getId().equals(".")) {
                snpsCount++;
            }
            if (vcfRecord.getFilter().toUpperCase().equals("PASS")) {
                passCount++;
            }

            if (vcfStat.getNumAlleles() > 2) {
                multiallelicsCount++;
            } else if (vcfStat.getNumAlleles() > 1) {
                biallelicsCount++;
            }

            if (!vcfRecord.getQuality().equals(".")) {
                float qualAux = Float.valueOf(vcfRecord.getQuality());
                if (qualAux >= 0) {
                    accumQuality += qualAux;
                }
            }

            // Once all samples have been traverse, calculate % that follow inheritance model
            controlsDominant = controlsDominant * 100 / (sampleNames.size() - vcfStat.getMissingGenotypes());
            casesDominant = casesDominant * 100 / (sampleNames.size() - vcfStat.getMissingGenotypes());
            controlsRecessive = controlsRecessive * 100 / (sampleNames.size() - vcfStat.getMissingGenotypes());
            casesRecessive = casesRecessive * 100 / (sampleNames.size() - vcfStat.getMissingGenotypes());


            vcfStat.setTransitionsCount(transitionsCount);
            vcfStat.setTransversionsCount(transversionsCount);

            vcfStat.setCasesPercentDominant(casesDominant);
            vcfStat.setControlsPercentDominant(controlsDominant);
            vcfStat.setCasesPercentRecessive(casesRecessive);
            vcfStat.setControlsPercentRecessive(controlsRecessive);

            statList.add(vcfStat);
            cont++;
        }

        samplesCount = sampleNames.size();

        globalStats.updateStats(variantsCount, samplesCount, snpsCount, indelsCount,
                passCount, transitionsCount, transversionsCount, biallelicsCount,
                multiallelicsCount, accumQuality);

        return statList;
    }

    public static VcfVariantGroupStat groupStats(List<VcfRecord> vcfRecords, Pedigree ped, String group) {

        Set<String> groupValues = getGroupValues(ped, group);
        List<String> sampleList;
        VcfVariantGroupStat groupStats = null;
        List<VcfRecordStat> variantStats = null;

        VcfGlobalStat globalStats = new VcfGlobalStat();

        if (groupValues != null) {
            groupStats = new VcfVariantGroupStat(group, groupValues);


            for (String val : groupValues) {
                sampleList = getSamplesValueGroup(val, group, ped);
                variantStats = variantStats(vcfRecords, sampleList, ped, globalStats);
                groupStats.getVariantStats().put(val, variantStats);
            }

        }
        return groupStats;
    }

    public static void runner(VcfDataReader vcfReader, VcfDataWriter vcfWriter, String pedFileName, String path) throws Exception {

        int batchSize = 2;

        Pedigree ped = new Pedigree(pedFileName);

        List<VcfRecord> batch;
        List<VcfRecordStat> statsList;

        vcfReader.open();
        vcfWriter.open();

        vcfReader.pre();
        vcfWriter.pre();


        VcfGlobalStat globalStats = new VcfGlobalStat();
        VcfSampleStat vcfSampleStat = new VcfSampleStat(vcfReader.getSampleNames());

        VcfSampleGroupStats vcfSampleGroupStatsPhen = new VcfSampleGroupStats();
        VcfSampleGroupStats vcfSampleGroupStatsFam = new VcfSampleGroupStats();

        VcfVariantGroupStat groupStatsBatchPhen;
        VcfVariantGroupStat groupStatsBatchFam;

        batch = vcfReader.read(batchSize);

        while (!batch.isEmpty()) {
            statsList = variantStats(batch, vcfReader.getSampleNames(), ped, globalStats);

            sampleStats(batch, vcfReader.getSampleNames(), ped, vcfSampleStat);

            groupStatsBatchPhen = groupStats(batch, ped, "phenotype");
            groupStatsBatchFam = groupStats(batch, ped, "family");

            sampleGroupStats(batch, ped, "phenotype", vcfSampleGroupStatsPhen);
            sampleGroupStats(batch, ped, "family", vcfSampleGroupStatsFam);


            vcfWriter.write(statsList);
            vcfWriter.write(groupStatsBatchPhen);
            vcfWriter.write(groupStatsBatchFam);

            batch = vcfReader.read(batchSize);
        }

        vcfWriter.write(globalStats);
        vcfWriter.write(vcfSampleStat);

        vcfWriter.write(vcfSampleGroupStatsFam);
        vcfWriter.write(vcfSampleGroupStatsPhen);

        vcfReader.close();
        vcfWriter.close();

    }

    public static void sampleStats(List<VcfRecord> vcfRecords, List<String> sampleNames, Pedigree ped, VcfSampleStat sampleStat) {

        Genotype g = null;
        Individual ind = null;

        for (VcfRecord record : vcfRecords) {

            for (String sample : sampleNames) {


                g = record.getSampleGenotype(sample);

                // Find the missing alleles
                if (g.getCode() != AllelesCode.ALLELES_OK) {                   // Missing genotype (one or both alleles missing)

                    sampleStat.incrementMissingGenotypes(sample);
                }
                // Check mendelian errors
                if (ped != null) {
                    ind = ped.getIndividual(sample);
                    if (g.getCode() == AllelesCode.ALLELES_OK && isMendelianError(ind, g, record)) {
                        sampleStat.incrementMendelianErrors(sample);

                    }

                    //Count homozygotes
                    if (g.getAllele1() == g.getAllele2()) {
                        sampleStat.incrementHomozygotesNumber(sample);
                    }
                }
            }
        }
    }

    public static void sampleGroupStats(List<VcfRecord> batch, Pedigree ped, String group, VcfSampleGroupStats vcfSampleGroupStats) {

        Set<String> groupValues = getGroupValues(ped, group);
        SampleStat s;
        VcfSampleStat vcfSampleStat;

        List<String> sampleList;

        if (vcfSampleGroupStats.getGroup() == null) {
            vcfSampleGroupStats.setGroup(group);
        }

        if (vcfSampleGroupStats.getSampleStats().size() == 0) {
            for (String groupVal : groupValues) {
                sampleList = getSamplesValueGroup(groupVal, group, ped);
                vcfSampleStat = new VcfSampleStat(sampleList);

                vcfSampleGroupStats.getSampleStats().put(groupVal, vcfSampleStat);

            }


        }

        for (Map.Entry<String, VcfSampleStat> entry : vcfSampleGroupStats.getSampleStats().entrySet()) {
            sampleList = getSamplesValueGroup(entry.getKey(), group, ped);
            vcfSampleStat = entry.getValue();
            sampleStats(batch, sampleList, ped, vcfSampleStat);
        }
    }

    private static List<String> getSamplesValueGroup(String val, String group, Pedigree ped) {
        List<String> list = new ArrayList<>(100);
        Individual ind;
        for (Map.Entry<String, Individual> entry : ped.getIndividuals().entrySet()) {
            ind = entry.getValue();
            if (group.toLowerCase().equals("phenotype")) {
                if (ind.getPhenotype().equals(val)) {
                    list.add(ind.getId());
                }
            } else if (group.toLowerCase().equals("family")) {
                if (ind.getFamily().equals(val)) {
                    list.add(ind.getId());
                }
            }
        }

        return list;
    }

    private static Set<String> getGroupValues(Pedigree ped, String group) {

        Set<String> values = new TreeSet<>();
        Individual ind;
        for (Map.Entry<String, Individual> entry : ped.getIndividuals().entrySet()) {
            ind = entry.getValue();
            if (group.toLowerCase().equals("phenotype")) {
                values.add(ind.getPhenotype());
            } else if (group.toLowerCase().equals("family")) {
                values.add(ind.getFamily());
            }


        }
        return values;  //To change body of created methods use File | Settings | File Templates.
    }

    private static boolean isMendelianError(Individual ind, Genotype g, VcfRecord vcfRecord) {

        Genotype gFather;
        Genotype gMother;

        if (ind.getFather() == null || ind.getMother() == null) {
            return false;
        }

        gFather = vcfRecord.getSampleGenotype(ind.getFather().getId());
        gMother = vcfRecord.getSampleGenotype(ind.getMother().getId());

        if (gFather.getCode() != AllelesCode.ALLELES_OK || gMother.getCode() != AllelesCode.ALLELES_OK) {
            return false;
        }

        if (checkMendel(vcfRecord.getChromosome(), gFather, gMother, g, ind.getSexCode()) > 0) {
            return true;
        }

        return false;
    }

    private static int checkMendel(String chromosome, Genotype gFather, Genotype gMother, Genotype gInd, Sex sex) {

        // Ignore if any allele is missing
        if (gFather.getAllele1() < 0 ||
                gFather.getAllele2() < 0 ||
                gMother.getAllele1() < 0 ||
                gMother.getAllele2() < 0 ||
                gInd.getAllele1() < 0 ||
                gInd.getAllele2() < 0) {
            return -1;
        }


        // Ignore haploid chromosomes
        if (chromosome.toUpperCase().equals("Y") || chromosome.toUpperCase().equals("MT")) {
            return -2;
        }

        int mendelType = 0;

        if (!chromosome.toUpperCase().equals("X") || sex == Sex.FEMALE) {
            if ((!gInd.isAllele1Ref() && gInd.isAllele2Ref()) ||
                    (gInd.isAllele1Ref() && !gInd.isAllele2Ref())) {
                // KID = 01/10
                // 00x00 -> 01  (m1)
                // 11x11 -> 01  (m2)
                if ((gFather.isAllele1Ref() && gFather.isAllele2Ref()) &&
                        (gMother.isAllele1Ref() && gMother.isAllele2Ref())) {
                    mendelType = 1;
                } else if ((!gFather.isAllele1Ref() && !gFather.isAllele2Ref()) &&
                        (!gMother.isAllele1Ref() && !gMother.isAllele2Ref())) {
                    mendelType = 2;
                }
            } else if (gInd.isAllele1Ref() && gInd.isAllele2Ref()) {
                // KID = 00
                // 00x11 -> 00 (m3) P11->00
                // 01x11 -> 00 (m3)
                // ??x11 -> 00 (m3)

                // 11x00 -> 00 (m4) M11->00
                // 11x01 -> 00 (m4)
                // 11x?? -> 00 (m4)

                // 11x11 -> 00 (m5) P11+M11->00

                // Hom parent can't breed opposite hom child

                // rule = at least one '11' parent
                if ((!gFather.isAllele1Ref() && !gFather.isAllele2Ref()) ||
                        !gMother.isAllele1Ref() && !gMother.isAllele2Ref()) {

                    if (!gFather.isAllele1Ref() && !gFather.isAllele2Ref() &&
                            !gMother.isAllele1Ref() && !gMother.isAllele2Ref()
                            ) {
                        mendelType = 5;
                    } else if (!gFather.isAllele1Ref() && !gFather.isAllele2Ref()) {
                        mendelType = 4;
                    } else {
                        mendelType = 3;
                    }


                }
            } else {
                // KID = 11

                // 00x01 -> 11 (m6)
                // 00x11 -> 11
                // 00x?? -> 11

                // 01x00 -> 11 (m7)
                // 11x00 -> 11
                // ??x00 -> 11

                // 00x00 -> 11 (m8) P00+M00->11

                // rule = at least one '00' parent

                if ((gFather.isAllele1Ref() && gFather.isAllele2Ref()) ||
                        (gMother.isAllele1Ref() && gMother.isAllele2Ref())
                        ) {
                    if (gFather.isAllele1Ref() && gFather.isAllele2Ref() &&
                            gMother.isAllele1Ref() && gMother.isAllele2Ref()) {
                        mendelType = 8;
                    } else if (gFather.isAllele1Ref() && gFather.isAllele2Ref()) {
                        mendelType = 6;

                    } else {
                        mendelType = 7;
                    }
                }

            }


        } else {
            // Chromosome X in inherited only from the mother and it is haploid
            if (!gInd.isAllele1Ref() && gMother.isAllele1Ref() && gMother.isAllele2Ref()) {
                mendelType = 9;
            }
            if (gInd.isAllele1Ref() && !gMother.isAllele1Ref() && !gMother.isAllele2Ref()) {
                mendelType = 10;
            }

        }


        return mendelType;
    }

}
