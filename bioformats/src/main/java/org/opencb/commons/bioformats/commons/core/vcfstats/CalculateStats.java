package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.opencb.commons.bioformats.commons.core.feature.Individual;
import org.opencb.commons.bioformats.commons.core.feature.Pedigree;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        List<VcfRecordStat> list_stats = new ArrayList<>(vcfRecordsList.size());

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

            Integer[] allelesCount = new Integer[vcfStat.getNumAlleles()];
            Arrays.fill(allelesCount, 0);
            Integer[] genotypesCount = new Integer[vcfStat.getNumAlleles() * vcfStat.getNumAlleles()];
            Arrays.fill(genotypesCount, 0);
            Float[] allelesFreq = new Float[vcfStat.getNumAlleles()];
            Float[] genotypesFreq = new Float[vcfStat.getNumAlleles() * vcfStat.getNumAlleles()];

            for (String sampleName : sampleNames) {

                Genotype g = vcfRecord.getSampleGenotype(sampleName);

                Genotypes.addGenotypeToList(vcfStat.getGenotypes(), g);

                // Check missing alleles and genotypes
                if (g.getCode() == AllelesCode.ALLELES_OK) {
                    // Both alleles set
                    genotypeCurrentPos = g.getAllele1() * (vcfStat.getNumAlleles()) + g.getAllele2();

                    allelesCount[g.getAllele1()]++;
                    allelesCount[g.getAllele2()]++;
                    genotypesCount[genotypeCurrentPos]++;

                    totalAllelesCount += 2;
                    totalGenotypesCount++;

                } else if (g.getCode() == AllelesCode.HAPLOID) {
                    // Haploid (chromosome X/Y)
                    allelesCount[g.getAllele1()]++;
                    totalAllelesCount++;
                } else {
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


            for (int i = 0; i < vcfStat.getNumAlleles(); i++) {
                allelesFreq[i] = (totalAllelesCount > 0) ? allelesCount[i] / (float) totalAllelesCount : 0;
                if (allelesFreq[i] < maf) {
                    maf = allelesFreq[i];
                    vcfStat.setMafAllele((i == 0) ? vcfStat.getRef_alleles() : vcfStat.getAltAlleles()[i - 1]);


                }
            }

            vcfStat.setMaf(maf);

            for (int i = 0; i < vcfStat.getNumAlleles() * vcfStat.getNumAlleles(); i++) {
                genotypesFreq[i] = (totalGenotypesCount > 0) ? genotypesCount[i] / (float) totalGenotypesCount : 0;


            }


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
                        String first_allele = (i == 0) ? vcfStat.getRef_alleles() : vcfStat.getAltAlleles()[i - 1];
                        String second_allele = (j == 0) ? vcfStat.getRef_alleles() : vcfStat.getAltAlleles()[j - 1];
                        mgfGenotype = first_allele + "|" + second_allele;
                        mgf = currentGtFreq;

                    }
                }
            }

            vcfStat.setMgf(mgf);
            vcfStat.setMgfAllele(mgfGenotype);

            vcfStat.setAllelesCount(allelesCount);
            vcfStat.setGenotypesCount(genotypesCount);

            vcfStat.setAlleles_freg(allelesFreq);
            vcfStat.setGenotypesFreq(genotypesFreq);

                       /*
         * 3 possibilities for being an INDEL:
         * - The value of the ALT field is <DEL> or <INS>
         * - The REF allele is not . but the ALT is
         * - The REF allele is . but the ALT is not
         * - The REF field length is different than the ALT field length
         */
            if ((!vcfStat.getRef_alleles().equals(".") && vcfRecord.getAlternate().equals(".")) ||
                    (vcfRecord.getAlternate().equals(".") && !vcfStat.getRef_alleles().equals(".")) ||
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

            if(!vcfRecord.getQuality().equals(".")){
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

            list_stats.add(vcfStat);
            cont++;
        }

        samplesCount = sampleNames.size();

        globalStats.updateStats(variantsCount, samplesCount, snpsCount, indelsCount,
                passCount, transitionsCount, transversionsCount, biallelicsCount,
                multiallelicsCount, accumQuality);

        return list_stats;
    }

    public static VcfVariantGroupStat groupStats(List<VcfRecord> vcfRecords, Pedigree ped, String group) {

        Set<String> groupValues = getGroupValues(ped, group);
        List<String> list_samples;
        VcfVariantGroupStat groupStats = null;
        List<VcfRecordStat> variantStats = null;

        VcfGlobalStat globalStats = new VcfGlobalStat();

        if (groupValues != null) {
            groupStats = new VcfVariantGroupStat(group, groupValues);


            for (String val : groupValues) {
                list_samples = getSamplesValueGroup(val, group, ped);
                variantStats = variantStats(vcfRecords, list_samples, ped, globalStats);
                groupStats.getVariantStats().put(val, variantStats);
            }

        }
        return groupStats;
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

    public static void runner(String vcfFileName, String pedFileName, String path) throws Exception {

        int batch_size = 1000;
        boolean firstTime = true;

        Vcf4Reader vcf = new Vcf4Reader(vcfFileName);
        Pedigree ped = new Pedigree(pedFileName);

        VariantStatsWriter variantWriter = new VariantStatsWriter(path);
        SampleStatsWriter sampleWriter = new SampleStatsWriter(path);
        GroupStatsWriter groupWriterPhen = new GroupStatsWriter(path);
        GroupStatsWriter groupWriterFam = new GroupStatsWriter(path);
        GlobalStatsWriter globalWriter = new GlobalStatsWriter(path);

        List<VcfRecord> batch;
        List<VcfRecordStat> stats_list;

        VcfGlobalStat globalStats = new VcfGlobalStat();
        VcfSampleStat vcfSampleStat = new VcfSampleStat(vcf.getSampleNames());

        VcfVariantGroupStat groupStatsBatchPhen;
        VcfVariantGroupStat groupStatsBatchFam;

        batch = vcf.read(batch_size);

        variantWriter.printHeader();

        while (!batch.isEmpty()) {
            stats_list = variantStats(batch, vcf.getSampleNames(), ped, globalStats);

            sampleStats(batch, vcf.getSampleNames(), ped, vcfSampleStat);

            groupStatsBatchPhen = groupStats(batch, ped, "phenotype");
            groupStatsBatchFam = groupStats(batch, ped, "family");

            if (firstTime) {
                groupWriterPhen.setFilenames(groupStatsBatchPhen);
                groupWriterPhen.printHeader();

                groupWriterFam.setFilenames(groupStatsBatchFam);
                groupWriterFam.printHeader();
                firstTime = false;
            }

            variantWriter.printStatRecord(stats_list);
            groupWriterPhen.printGroupStats(groupStatsBatchPhen);
            groupWriterFam.printGroupStats(groupStatsBatchFam);

            batch = vcf.read(batch_size);
        }


        sampleWriter.printStats(vcfSampleStat);
        sampleWriter.close();

        globalWriter.printStats(globalStats);
        globalWriter.close();


        vcf.close();
        variantWriter.close();
        groupWriterPhen.close();
        groupWriterFam.close();
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

    private static boolean isMendelianError(Individual ind, Genotype g, VcfRecord vcf_record) {

        Genotype g_father;
        Genotype g_mother;

        if (ind.getFather() == null || ind.getMother() == null) {
            return false;
        }

        g_father = vcf_record.getSampleGenotype(ind.getFather().getId());
        g_mother = vcf_record.getSampleGenotype(ind.getMother().getId());

        if (g_father.getCode() != AllelesCode.ALLELES_OK || g_mother.getCode() != AllelesCode.ALLELES_OK) {
            return false;
        }

        if (check_mendel(vcf_record.getChromosome(), g_father, g_mother, g, ind.getSexCode()) > 0) {
            return true;
        }

        return false;
    }

    private static int check_mendel(String chromosome, Genotype g_father, Genotype g_mother, Genotype g_ind, Sex sex) {

        // Ignore if any allele is missing
        if (g_father.getAllele1() < 0 ||
                g_father.getAllele2() < 0 ||
                g_mother.getAllele1() < 0 ||
                g_mother.getAllele2() < 0 ||
                g_ind.getAllele1() < 0 ||
                g_ind.getAllele2() < 0) {
            return -1;
        }


        // Ignore haploid chromosomes
        if (chromosome.toUpperCase().equals("Y") || chromosome.toUpperCase().equals("MT")) {
            return -2;
        }

        int mendel_type = 0;

        if (!chromosome.toUpperCase().equals("X") || sex == Sex.FEMALE) {
            if ((!g_ind.isAllele1Ref() && g_ind.isAllele2Ref()) ||
                    (g_ind.isAllele1Ref() && !g_ind.isAllele2Ref())) {
                // KID = 01/10
                // 00x00 -> 01  (m1)
                // 11x11 -> 01  (m2)
                if ((g_father.isAllele1Ref() && g_father.isAllele2Ref()) &&
                        (g_mother.isAllele1Ref() && g_mother.isAllele2Ref())) {
                    mendel_type = 1;
                } else if ((!g_father.isAllele1Ref() && !g_father.isAllele2Ref()) &&
                        (!g_mother.isAllele1Ref() && !g_mother.isAllele2Ref())) {
                    mendel_type = 2;
                }
            } else if (g_ind.isAllele1Ref() && g_ind.isAllele2Ref()) {
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
                if ((!g_father.isAllele1Ref() && !g_father.isAllele2Ref()) ||
                        !g_mother.isAllele1Ref() && !g_mother.isAllele2Ref()) {

                    if (!g_father.isAllele1Ref() && !g_father.isAllele2Ref() &&
                            !g_mother.isAllele1Ref() && !g_mother.isAllele2Ref()
                            ) {
                        mendel_type = 5;
                    } else if (!g_father.isAllele1Ref() && !g_father.isAllele2Ref()) {
                        mendel_type = 4;
                    } else {
                        mendel_type = 3;
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

                if ((g_father.isAllele1Ref() && g_father.isAllele2Ref()) ||
                        (g_mother.isAllele1Ref() && g_mother.isAllele2Ref())
                        ) {
                    if (g_father.isAllele1Ref() && g_father.isAllele2Ref() &&
                            g_mother.isAllele1Ref() && g_mother.isAllele2Ref()) {
                        mendel_type = 8;
                    } else if (g_father.isAllele1Ref() && g_father.isAllele2Ref()) {
                        mendel_type = 6;

                    } else {
                        mendel_type = 7;
                    }
                }

            }


        } else {
            // Chromosome X in inherited only from the mother and it is haploid
            if (!g_ind.isAllele1Ref() && g_mother.isAllele1Ref() && g_mother.isAllele2Ref()) {
                mendel_type = 9;
            }
            if (g_ind.isAllele1Ref() && !g_mother.isAllele1Ref() && !g_mother.isAllele2Ref()) {
                mendel_type = 10;
            }

        }


        return mendel_type;
    }

    private static class VariantStatsWriter {
        private PrintWriter pw;

        private VariantStatsWriter(String path) throws IOException {
            this.pw = new PrintWriter(new FileWriter(path + "variants.stats"));
        }

        public void printHeader() {
            pw.append(String.format("%-5s%-10s%-10s%-5s%-10s%-10s%-10s" +
                    "%-10s%-10s%-10s%-15s%-40s%-10s%-10s%-15s" +
                    "%-10s%-10s%-10s%-10s\n",
                    "Chr", "Pos", "Indel?", "Ref", "Alt", "Maf", "Mgf",
                    "NumAll.", "Miss All.", "Miss Gt", "All. Count", "Gt count", "Trans", "Transv", "Mend Error",
                    "Cases D", "Controls D", "Cases R", "Controls R"));
        }

        public void printStatRecord(List<VcfRecordStat> list) {
            for (VcfRecordStat v : list) {
                pw.append(String.format("%-5s%-10d%-10s%-5s%-10s%-10s%-10s" +
                        "%-10d%-10d%-10d%-15s%-40s%-10d%-10d%-15d" +
                        "%-10.2f%-10.2f%-10.2f%-10.2f\n",
                        v.getChromosome(),
                        v.getPosition(),
                        (v.getIndel() ? "Y" : "N"),
                        v.getRef_alleles(),
                        Arrays.toString(v.getAltAlleles()),
                        v.getMafAllele(),
                        v.getMgfAllele(),
                        v.getNumAlleles(),
                        v.getMissingAlleles(),
                        v.getMissingGenotypes(),
                        Arrays.toString(v.getAllelesCount()),
                        v.getGenotypes(),
                        v.getTransitionsCount(),
                        v.getTransversionsCount(),
                        v.getMendelinanErrors(),
                        v.getCasesPercentDominant(),
                        v.getControlsPercentDominant(),
                        v.getCasesPercentRecessive(),
                        v.getControlsPercentRecessive()
                ));
            }
        }

        public void close() {
            pw.close();
        }
    }

    private static class GroupStatsWriter {

        private Map<String, PrintWriter> mapPw;
        private String path;

        public GroupStatsWriter(String path) {
            this.path = path;

        }

        public void setFilenames(VcfVariantGroupStat gs) throws IOException {
            PrintWriter aux;
            String filename;

            mapPw = new LinkedHashMap<>(gs.getVariantStats().size());

            for (Map.Entry<String, List<VcfRecordStat>> entry : gs.getVariantStats().entrySet()) {
                filename = path + "variant_stats_" + gs.getGroup() + "_" + entry.getKey() + ".stats";
                aux = new PrintWriter(new FileWriter(filename));
                mapPw.put(entry.getKey(), aux);
            }

        }

        public void printHeader() {

            PrintWriter pw;
            for (Map.Entry<String, PrintWriter> entry : mapPw.entrySet()) {
                pw = entry.getValue();
                pw.append(String.format("%-5s%-10s%-10s%-5s%-10s%-10s%-10s" +
                        "%-10s%-10s%-10s%-15s%-40s%-10s%-10s%-15s" +
                        "%-10s%-10s%-10s%-10s\n",
                        "Chr", "Pos", "Indel?", "Ref", "Alt", "Maf", "Mgf",
                        "NumAll.", "Miss All.", "Miss Gt", "All. Count", "Gt count", "Trans", "Transv", "Mend Error",
                        "Cases D", "Controls D", "Cases R", "Controls R"));
            }
        }


        public void close() {
            PrintWriter pw;
            for (Map.Entry<String, PrintWriter> entry : mapPw.entrySet()) {
                pw = entry.getValue();
                pw.close();
            }
        }

        public void printGroupStats(VcfVariantGroupStat groupStatsBatch) {
            PrintWriter pw;
            List<VcfRecordStat> list;
            for (Map.Entry<String, PrintWriter> entry : mapPw.entrySet()) {
                pw = entry.getValue();
                list = groupStatsBatch.getVariantStats().get(entry.getKey());
                for (VcfRecordStat v : list) {
                    pw.append(String.format("%-5s%-10d%-10s%-5s%-10s%-10s%-10s" +
                            "%-10d%-10d%-10d%-15s%-40s%-10d%-10d%-15d" +
                            "%-10.2f%-10.2f%-10.2f%-10.2f\n",
                            v.getChromosome(),
                            v.getPosition(),
                            (v.getIndel() ? "Y" : "N"),
                            v.getRef_alleles(),
                            Arrays.toString(v.getAltAlleles()),
                            v.getMafAllele(),
                            v.getMgfAllele(),
                            v.getNumAlleles(),
                            v.getMissingAlleles(),
                            v.getMissingGenotypes(),
                            Arrays.toString(v.getAllelesCount()),
                            v.getGenotypes(),
                            v.getTransitionsCount(),
                            v.getTransversionsCount(),
                            v.getMendelinanErrors(),
                            v.getCasesPercentDominant(),
                            v.getControlsPercentDominant(),
                            v.getCasesPercentRecessive(),
                            v.getControlsPercentRecessive()
                    ));

                }

            }
        }
    }

    private static class SampleStatsWriter {
        private PrintWriter pw;

        private SampleStatsWriter(String path) throws IOException {
            this.pw = new PrintWriter(new FileWriter(path + "sample.stats"));
        }

        public void printStats(VcfSampleStat samplesStats) {
            SampleStat s;
            pw.append(String.format("%-10s%-10s%-10s%-10s\n", "Sample", "MissGt", "Mendel Err", "Homoz Count"));
            for (Map.Entry<String, SampleStat> entry : samplesStats.getSamplesStats().entrySet()) {
                s = entry.getValue();
                pw.append(String.format("%-10s%-10d%-10d%10d\n", s.getId(), s.getMissingGenotypes(), s.getMendelianErrors(), s.getHomozygotesNumeber()));

            }

        }

        public void close() {
            pw.close();
        }
    }

    private static class GlobalStatsWriter {
        private PrintWriter pw;

        private GlobalStatsWriter(String path) throws IOException {
            this.pw = new PrintWriter(new FileWriter(path + "global.stats"));
        }

        public void printStats(VcfGlobalStat globalStats) {
            pw.append("Number of variants = " + globalStats.getVariantsCount() + "\n");
            pw.append("Number of samples = " + globalStats.getSamplesCount() + "\n");
            pw.append("Number of biallelic variants = " + globalStats.getBiallelicsCount() + "\n");
            pw.append("Number of multiallelic variants = " + globalStats.getMultiallelicsCount() + "\n");
            pw.append("Number of SNP = " + globalStats.getSnpsCount() + "\n");
            pw.append("Number of indels = " + globalStats.getIndelsCount() + "\n");
            pw.append("Number of transitions = " + globalStats.getTransitionsCount() + "\n");
            pw.append("Number of transversions = " + globalStats.getTransversionsCount() + "\n");
            pw.append("Ti/TV ratio = " + ((float) globalStats.getTransitionsCount() / (float) globalStats.getTransversionsCount()) + "\n");
            pw.append("Percentage of PASS = " + (((float) globalStats.getPassCount() / (float) globalStats.getVariantsCount()) * 100) + "%\n");
            pw.append("Average quality = " + (globalStats.getAccumQuality() / (float) globalStats.getVariantsCount()) + "\n");
        }

        public void close() {
            pw.close();
        }
    }
}
