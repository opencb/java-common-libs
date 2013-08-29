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

    public static List<VcfRecordStat> variantStats(List<VcfRecord> list_vcf_records, List<String> sampleNames, Pedigree ped, VcfGlobalStat globalStats) {
        List<VcfRecordStat> list_stats = new ArrayList<>(list_vcf_records.size());

        // Temporary variables for file stats updating
        int variants_count = 0, samples_count = 0, snps_count = 0, indels_count = 0, pass_count = 0;
        int transitions_count = 0, transversions_count = 0, biallelics_count = 0, multiallelics_count = 0;
        float accum_quality = 0;


        int cont = 0;
        for (VcfRecord vcf_record : list_vcf_records) {

            int genotype_current_pos;
            int total_alleles_count = 0;
            int total_genotypes_count = 0;
            String mgf_genotype = "";
            Individual ind;

            float maf = Float.MAX_VALUE;
            float mgf = Float.MAX_VALUE;
            float cur_gt_freq;


            float controlsDominant = 0;
            float casesDominant = 0;
            float controlsRecessive = 0;
            float casesRecessive = 0;


            VcfRecordStat vcf_stat = new VcfRecordStat();


            vcf_stat.setChromosome(vcf_record.getChromosome());
            vcf_stat.setPosition(new Long(vcf_record.getPosition()));
            vcf_stat.setRefAllele(vcf_record.getReference());

            String[] alt_alleles = vcf_record.getAltAlleles();

            vcf_stat.setAltAlleles(alt_alleles);
            vcf_stat.setNumAlleles(alt_alleles.length + 1);

            Integer[] alleles_count = new Integer[vcf_stat.getNumAlleles()];
            Arrays.fill(alleles_count, 0);
            Integer[] genotypes_count = new Integer[vcf_stat.getNumAlleles() * vcf_stat.getNumAlleles()];
            Arrays.fill(genotypes_count, 0);
            Float[] alleles_freq = new Float[vcf_stat.getNumAlleles()];
            Float[] genotypes_freq = new Float[vcf_stat.getNumAlleles() * vcf_stat.getNumAlleles()];

            for (String sampleName : sampleNames) {

                Genotype g = vcf_record.getSampleGenotype(sampleName);

                Genotypes.addGenotypeToList(vcf_stat.getGenotypes(), g);

                // Check missing alleles and genotypes
                if (g.getCode() == AllelesCode.ALLELES_OK) {
                    // Both alleles set
                    genotype_current_pos = g.getAllele1() * (vcf_stat.getNumAlleles()) + g.getAllele2();
                    assert (g.getAllele1() <= vcf_stat.getNumAlleles());
                    assert (g.getAllele2() <= vcf_stat.getNumAlleles());
                    assert (genotype_current_pos <= vcf_stat.getNumAlleles() * vcf_stat.getNumAlleles());


                    alleles_count[g.getAllele1()]++;
                    alleles_count[g.getAllele2()]++;
                    genotypes_count[genotype_current_pos]++;

                    total_alleles_count += 2;
                    total_genotypes_count++;

                } else if (g.getCode() == AllelesCode.HAPLOID) {
                    // Haploid (chromosome X/Y)
                    alleles_count[g.getAllele1()]++;
                    total_alleles_count++;
                } else {
                    // Missing genotype (one or both alleles missing)

                    vcf_stat.setMissingGenotypes(vcf_stat.getMissingGenotypes() + 1);
                    if (g.getAllele1() == null) {
                        vcf_stat.setMissingAlleles(vcf_stat.getMissingAlleles() + 1);
                    } else {
                        alleles_count[g.getAllele1()]++;
                        total_alleles_count++;
                    }


                    if (g.getAllele2() == null) {
                        vcf_stat.setMissingAlleles(vcf_stat.getMissingAlleles() + 1);
                    } else {
                        alleles_count[g.getAllele2()]++;
                        total_alleles_count++;

                    }


                }

                // Include statistics that depend on pedigree information
                if (ped != null) {
                    if (g.getCode() == AllelesCode.ALLELES_OK || g.getCode() == AllelesCode.HAPLOID) {
                        ind = ped.getIndividual(sampleName);
                        if (isMendelianError(ind, g, vcf_record)) {
                            vcf_stat.setMendelinanErrors(vcf_stat.getMendelinanErrors() + 1);

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


            for (int i = 0; i < vcf_stat.getNumAlleles(); i++) {
                alleles_freq[i] = (total_alleles_count > 0) ? new Float(alleles_count[i] / (float) total_alleles_count) : 0;
                if (alleles_freq[i] < maf) {
                    maf = alleles_freq[i];
                    vcf_stat.setMafAllele((i == 0) ? vcf_stat.getRef_alleles() : vcf_stat.getAltAlleles()[i - 1]);


                }
            }

            vcf_stat.setMaf(maf);

            for (int i = 0; i < vcf_stat.getNumAlleles() * vcf_stat.getNumAlleles(); i++) {
                genotypes_freq[i] = (total_genotypes_count > 0) ? new Float(genotypes_count[i] / (float) total_genotypes_count) : 0;


            }


            for (int i = 0; i < vcf_stat.getNumAlleles(); i++) {
                for (int j = 0; j < vcf_stat.getNumAlleles(); j++) {
                    int idx1 = i * vcf_stat.getNumAlleles() + j;
                    if (i == j) {
                        cur_gt_freq = genotypes_freq[idx1];
                    } else {
                        int idx2 = j * vcf_stat.getNumAlleles() + i;
                        cur_gt_freq = genotypes_freq[idx1] + genotypes_freq[idx2];
                    }

                    if (cur_gt_freq < mgf) {
                        String first_allele = (i == 0) ? vcf_stat.getRef_alleles() : vcf_stat.getAltAlleles()[i - 1];
                        String second_allele = (j == 0) ? vcf_stat.getRef_alleles() : vcf_stat.getAltAlleles()[j - 1];
                        mgf_genotype = first_allele + "|" + second_allele;
                        mgf = cur_gt_freq;

                    }
                }
            }

            vcf_stat.setMgf(mgf);
            vcf_stat.setMgfAllele(mgf_genotype);

            vcf_stat.setAllelesCount(alleles_count);
            vcf_stat.setGenotypesCount(genotypes_count);

            vcf_stat.setAlleles_freg(alleles_freq);
            vcf_stat.setGenotypesFreq(genotypes_freq);

                       /*
         * 3 possibilities for being an INDEL:
         * - The value of the ALT field is <DEL> or <INS>
         * - The REF allele is not . but the ALT is
         * - The REF allele is . but the ALT is not
         * - The REF field length is different than the ALT field length
         */
            if ((!vcf_stat.getRef_alleles().equals(".") && vcf_record.getAlternate().equals(".")) ||
                    (vcf_record.getAlternate().equals(".") && !vcf_stat.getRef_alleles().equals(".")) ||
                    (vcf_record.getAlternate().equals("<INS>")) ||
                    (vcf_record.getAlternate().equals("<DEL>")) ||
                    vcf_record.getReference().length() != vcf_record.getAlternate().length()) {
                vcf_stat.setIndel(true);
                indels_count++;
            } else {
                vcf_stat.setIndel(false);
            }

            // Transitions and transversions

            String ref = vcf_record.getReference().toUpperCase();
            for (String alt : vcf_record.getAltAlleles()) {
                alt = alt.toUpperCase();

                if (ref.length() == 1 && alt.length() == 1) {

                    switch (ref) {
                        case "C":
                            if (alt.equals("T")) {
                                transitions_count++;
                            } else {
                                transversions_count++;
                            }
                            break;
                        case "T":
                            if (alt.equals("C")) {
                                transitions_count++;
                            } else {
                                transversions_count++;
                            }
                            break;
                        case "A":
                            if (alt.equals("G")) {
                                transitions_count++;

                            } else {
                                transversions_count++;
                            }
                            break;
                        case "G":
                            if (alt.equals("A")) {
                                transitions_count++;
                            } else {
                                transversions_count++;
                            }
                            break;
                    }
                }

            }

            // Update variables finally used to update file_stats_t structure
            variants_count++;
            if (!vcf_record.getId().equals(".")) {
                snps_count++;
            }
            if (vcf_record.getFilter().toUpperCase().equals("PASS")) {
                pass_count++;
            }

            if (vcf_stat.getNumAlleles() > 2) {
                multiallelics_count++;
            } else if (vcf_stat.getNumAlleles() > 1) {
                biallelics_count++;
            }

            float qualAux = Float.valueOf(vcf_record.getQuality());
            if (qualAux >= 0) {
                accum_quality += qualAux;
            }
            // Once all samples have been traverse, calculate % that follow inheritance model
            controlsDominant = controlsDominant * 100 / (sampleNames.size() - vcf_stat.getMissingGenotypes());
            casesDominant = casesDominant * 100 / (sampleNames.size() - vcf_stat.getMissingGenotypes());
            controlsRecessive = controlsRecessive * 100 / (sampleNames.size() - vcf_stat.getMissingGenotypes());
            casesRecessive = casesRecessive * 100 / (sampleNames.size() - vcf_stat.getMissingGenotypes());


            vcf_stat.setTransitionsCount(transitions_count);
            vcf_stat.setTransversionsCount(transversions_count);

            vcf_stat.setCasesPercentDominant(casesDominant);
            vcf_stat.setControlsPercentDominant(controlsDominant);
            vcf_stat.setCasesPercentRecessive(casesRecessive);
            vcf_stat.setControlsPercentRecessive(controlsRecessive);

            list_stats.add(vcf_stat);
            cont++;
        }

        samples_count = sampleNames.size();

        globalStats.updateStats(variants_count, samples_count, snps_count, indels_count,
                pass_count, transitions_count, transversions_count, biallelics_count,
                multiallelics_count, accum_quality);

        return list_stats;
    }

    public static VcfGroupStat groupStats(List<VcfRecord> vcfRecords, Pedigree ped, String group) {

        Set<String> groupValues = getGroupValues(ped, group);
        List<String> list_samples;
        VcfGroupStat groupStats = null;
        List<VcfRecordStat> variantStats = null;

        VcfGlobalStat globalStats = new VcfGlobalStat();

        if (groupValues != null) {
            groupStats = new VcfGroupStat(group, groupValues);

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

        int batch_size = 4;
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
        VcfGroupStat groupStatsBatchPhen;
        VcfGroupStat groupStatsBatchFam;
        VcfSampleStat vcfSampleStat = new VcfSampleStat(vcf.getSampleNames());


        variantWriter.printHeader();


        batch = vcf.read(batch_size);

        while (!batch.isEmpty()) {
            stats_list = variantStats(batch, vcf.getSampleNames(), ped, globalStats);

            sampleStats(batch, vcf.getSampleNames(), ped, vcfSampleStat);

//            groupStatsBatchPhen = groupStats(batch, ped, "phenotype");
//            groupStatsBatchFam = groupStats(batch, ped, "family");

            if (firstTime) {
//                groupWriterPhen.setFilenames(groupStatsBatchPhen);
//                groupWriterPhen.printHeader();
//
//                groupWriterFam.setFilenames(groupStatsBatchFam);
//                groupWriterFam.printHeader();
//                firstTime = false;
            }

            variantWriter.printStatRecord(stats_list);
//            groupWriterPhen.printGroupStats(groupStatsBatchPhen);
//            groupWriterFam.printGroupStats(groupStatsBatchFam);

            batch = vcf.read(batch_size);
        }


        sampleWriter.printStats(vcfSampleStat);
        sampleWriter.close();

        globalWriter.printStats(globalStats);
        globalWriter.close();


        vcf.close();
        variantWriter.close();
//        groupWriterPhen.close();
//        groupWriterFam.close();
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

        public void setFilenames(VcfGroupStat gs) throws IOException {
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
                pw.append(String.format("%-5s%-10s%-5s%-10s%-10s%-10s" +
                        "%-10s%-10s%-10s%-15s%-40s%-10s%-10s%-15s" +
                        "%-10s%-10s%-10s%-10s\n",
                        "Chr", "Pos", "Ref", "Alt", "Maf", "Mgf",
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

        public void printGroupStats(VcfGroupStat groupStatsBatch) {
            PrintWriter pw;
            List<VcfRecordStat> list;
            for (Map.Entry<String, PrintWriter> entry : mapPw.entrySet()) {
                pw = entry.getValue();
                list = groupStatsBatch.getVariantStats().get(entry.getKey());
                for (VcfRecordStat v : list) {
                    pw.append(String.format("%-5s%-10d%-5s%-10s%-10s%-10s" +
                            "%-10d%-10d%-10d%-15s%-40s%-10d%-10d%-15d" +
                            "%-10.0f%-10.0f%-10.0f%-10.0f\n",
                            v.getChromosome(),
                            v.getPosition(),
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
            pw.append("Average quality = " + ((float) globalStats.getAccumQuality() / (float) globalStats.getVariantsCount()) + "\n");
        }

        public void close() {
            pw.close();
        }
    }
}
