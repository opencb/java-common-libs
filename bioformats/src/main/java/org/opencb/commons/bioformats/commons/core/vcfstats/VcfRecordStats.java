package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.opencb.commons.bioformats.commons.core.feature.Individual;
import org.opencb.commons.bioformats.commons.core.feature.Ped;
import org.opencb.commons.bioformats.commons.core.feature.Pedigree;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/26/13
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfRecordStats {


    public static VcfRecordStat calculateStats(VcfRecord vcf_record) {
        return calculateStats(vcf_record, null, null);
    }

    public static VcfRecordStat calculateStats(VcfRecord vcf_record, List<String> sampleNames, Pedigree ped) {
        int genotype_current_pos;
        int total_alleles_count = 0;
        int total_genotypes_count = 0;
        String mgf_genotype = "";
        String sampleName;
        Individual ind;
        int control_dominant = 0;
        int cases_dominant = 0;
        int control_recessive = 0;
        int cases_recessive = 0;


        VcfRecordStat vcf_stat = new VcfRecordStat();
        vcf_stat.setChromosome(vcf_record.getChromosome());
        vcf_stat.setPosition(new Long(vcf_record.getPosition()));
        vcf_stat.setRefAllele(vcf_record.getReference());
        Float maf = Float.MAX_VALUE;
        Float mgf = Float.MAX_VALUE;
        Float cur_gt_freq;

        String[] alt_alleles = vcf_record.getAltAlleles();

        vcf_stat.setAltAlleles(alt_alleles);
        vcf_stat.setNumAlleles(alt_alleles.length + 1);

        Integer[] alleles_count = new Integer[vcf_stat.getNumAlleles()];
        Arrays.fill(alleles_count, 0);
        Integer[] genotypes_count = new Integer[vcf_stat.getNumAlleles() * vcf_stat.getNumAlleles()];
        Arrays.fill(genotypes_count, 0);
        Float[] alleles_freq = new Float[vcf_stat.getNumAlleles()];
        Float[] genotypes_freq = new Float[vcf_stat.getNumAlleles() * vcf_stat.getNumAlleles()];

        for (int i = 0; i < vcf_record.getSamples().size(); i++) {
            String sample = vcf_record.getSamples().get(i);

            sampleName = sampleNames.get(i);

            Genotype g = vcf_record.getSampleGenotype(sample);
            Genotypes.addGenotypeToList(vcf_stat.getGenotypes(), g);


            // Check missing alleles and genotypes
            if (g.getCode() == AllelesCode.ALLELES_OK) {
                // Both alleles set
                genotype_current_pos = g.getAllele_1() * (vcf_stat.getNumAlleles()) + g.getAllele_2();
                assert (g.getAllele_1() <= vcf_stat.getNumAlleles());
                assert (g.getAllele_2() <= vcf_stat.getNumAlleles());
                assert (genotype_current_pos <= vcf_stat.getNumAlleles() * vcf_stat.getNumAlleles());


                alleles_count[g.getAllele_1()]++;
                alleles_count[g.getAllele_2()]++;
                genotypes_count[genotype_current_pos]++;

                total_alleles_count += 2;
                total_genotypes_count++;

            } else if (g.getCode() == AllelesCode.HAPLOID) {
                // Haploid (chromosome X/Y)
                alleles_count[g.getAllele_1()]++;
                total_alleles_count++;
            } else {
                // Missing genotype (one or both alleles missing)

                vcf_stat.setMissingGenotypes(vcf_stat.getMissingGenotypes() + 1);
                if (g.getAllele_1() == null) {
                    vcf_stat.setMissingAlleles(vcf_stat.getMissingAlleles() + 1);
                } else {
                    alleles_count[g.getAllele_1()]++;
                    total_alleles_count++;
                }


                if (g.getAllele_2() == null) {
                    vcf_stat.setMissingAlleles(vcf_stat.getMissingAlleles() + 1);
                } else {
                    alleles_count[g.getAllele_2()]++;
                    total_alleles_count++;

                }


            }

            // Include statistics that depend on pedigree information
            if (ped != null) {
                if (g.getCode() == AllelesCode.ALLELES_OK || g.getCode() == AllelesCode.HAPLOID) {
                    ind = ped.getIndividual(sampleName);
                    if (isMendelianError(ind, g, vcf_record, sampleNames)) {
                        vcf_stat.setMendelinanErrors(vcf_stat.getMendelinanErrors() + 1);

                    }

                    if (g.getCode() == AllelesCode.ALLELES_OK) {
                        // Check inheritance models
                        if (ind.getCondition() == Condition.UNAFFECTED) {
                            if (g.getAllele_1() == 0 && g.getAllele_2() == 0) { // 0|0
                                control_dominant++;
                                control_recessive++;

                            } else if ((g.getAllele_1() == 0 && g.getAllele_2() == 1) || (g.getAllele_1() == 1 || g.getAllele_1() == 0)) { // 0|1 or 1|0
                                control_recessive++;

                            }
                        } else if (ind.getCondition() == Condition.AFFECTED) {
                            if (g.getAllele_1() == 1 && g.getAllele_1() == 1 && g.getAllele_1() == g.getAllele_2()) {// 1|1, 2|2, and so on
                                cases_recessive++;
                                cases_dominant++;
                            } else if (g.getAllele_1() == 1 || g.getAllele_2() == 1) { // 0|1, 1|0, 1|2, 2|1, 1|3, and so on
                                cases_dominant++;

                            }
                        }

                    }

                }
            }

        }

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


        if ((vcf_stat.getRef_alleles().equals(".") && !vcf_record.getAlternate().equals(".")) ||
                (vcf_record.getAlternate().equals(".") && !vcf_stat.getRef_alleles().equals(".")) ||
                (!vcf_record.getAlternate().equals("<INS>")) ||
                (!vcf_record.getAlternate().equals("<DEL>")) ||
                vcf_record.getReference().length() != vcf_record.getAlternate().length()) {
            vcf_stat.setIndel(true);
        } else {
            vcf_stat.setIndel(false);
        }

        // Transitions and transversions

        for (int i = 0; i < vcf_stat.getAltAlleles().length; i++) {
            if (vcf_record.getReference().length() == 1 && vcf_stat.getAltAlleles().length == 1) {
                char ref = vcf_stat.getRef_alleles().toUpperCase().charAt(0);
                char alt = vcf_stat.getAltAlleles()[i].toUpperCase().charAt(0);

                switch (ref) {
                    case 'C':
                        if (alt == 'T') {
                            vcf_stat.setTransitionsCount(vcf_stat.getTransitionsCount() + 1);
                        } else {
                            vcf_stat.setTransversionsCount(vcf_stat.getTransversionsCount() + 1);
                        }
                        break;
                    case 'T':
                        if (alt == 'C') {
                            vcf_stat.setTransitionsCount(vcf_stat.getTransitionsCount() + 1);
                        } else {
                            vcf_stat.setTransversionsCount(vcf_stat.getTransversionsCount() + 1);
                        }
                        break;
                    case 'A':
                        if (alt == 'G') {
                            vcf_stat.setTransitionsCount(vcf_stat.getTransitionsCount() + 1);
                        } else {
                            vcf_stat.setTransversionsCount(vcf_stat.getTransversionsCount() + 1);
                        }
                        break;
                    case 'G':
                        if (alt == 'A') {
                            vcf_stat.setTransitionsCount(vcf_stat.getTransitionsCount() + 1);
                        } else {
                            vcf_stat.setTransversionsCount(vcf_stat.getTransversionsCount() + 1);
                        }
                        break;
                }
            }

        }

        return vcf_stat;

    }

    private static boolean isMendelianError(Individual ind, Genotype g, VcfRecord vcf_record, List<String> sampleNames) {

        Genotype g_father;
        Genotype g_mother;

        if (ind.getFather() == null || ind.getMother() == null) {
            return false;
        }

        int father_pos = sampleNames.indexOf(ind.getFather().getId());
        int mother_pos = sampleNames.indexOf(ind.getMother().getId());

        g_father = vcf_record.getSampleGenotype(vcf_record.getSamples().get(father_pos));
        g_mother = vcf_record.getSampleGenotype(vcf_record.getSamples().get(mother_pos));

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
        if (g_father.getAllele_1() < 0 ||
                g_father.getAllele_2() < 0 ||
                g_mother.getAllele_1() < 0 ||
                g_mother.getAllele_2() < 0 ||
                g_ind.getAllele_1() < 0 ||
                g_ind.getAllele_2() < 0) {
            return -1;
        }


        // Ignore haploid chromosomes
        if (chromosome.toUpperCase().equals("Y") || chromosome.toUpperCase().equals("MT")) {
            return -2;
        }

        int mendel_type = 0;

        if (!chromosome.toUpperCase().equals("X") || sex == Sex.FEMALE) {
            if ((g_ind.getAllele_1() == 1 && g_ind.getAllele_2() == 0) ||
                    (g_ind.getAllele_1() == 0 && g_ind.getAllele_2() == 1)) {
                // KID = 01/10
                // 00x00 -> 01  (m1)
                // 11x11 -> 01  (m2)
                if ((g_father.getAllele_1() == 0 && g_father.getAllele_2() == 0) &&
                        (g_mother.getAllele_1() == 0 && g_mother.getAllele_2() == 0)) {
                    mendel_type = 1;
                } else if ((g_father.getAllele_1() == 1 && g_father.getAllele_2() == 1) &&
                        (g_mother.getAllele_1() == 1 && g_mother.getAllele_2() == 1)) {
                    mendel_type = 2;
                }
            } else if (g_ind.getAllele_1() == 0 && g_ind.getAllele_2() == 0) {
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
                if ((g_father.getAllele_1() == 1 && g_father.getAllele_2() == 1) ||
                        g_mother.getAllele_1() == 1 && g_mother.getAllele_2() == 1) {

                    if (g_father.getAllele_1() == 1 && g_father.getAllele_2() == 1 &&
                            g_mother.getAllele_1() == 1 && g_mother.getAllele_2() == 1
                            ) {
                        mendel_type = 5;
                    } else if (g_father.getAllele_1() == 1 && g_father.getAllele_2() == 1) {
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

                if ((g_father.getAllele_1() == 0 && g_father.getAllele_2() == 0) ||
                        (g_mother.getAllele_1() == 0 && g_mother.getAllele_2() == 0)
                        ) {
                    if (g_father.getAllele_1() == 0 && g_father.getAllele_2() == 0 &&
                            g_mother.getAllele_1() == 0 && g_mother.getAllele_2() == 0) {
                        mendel_type = 8;
                    } else if (g_father.getAllele_1() == 0 && g_father.getAllele_2() == 0) {
                        mendel_type = 6;

                    } else {
                        mendel_type = 7;
                    }
                }

            }


        } else {
            // Chromosome X in inherited only from the mother and it is haploid
            if (g_ind.getAllele_1() == 1 && g_mother.getAllele_1() == 0 && g_mother.getAllele_2() == 0) {
                mendel_type = 9;
            }
            if (g_ind.getAllele_1() == 0 && g_mother.getAllele_1() == 1 && g_mother.getAllele_2() == 1) {
                mendel_type = 10;
            }

        }


        return mendel_type;
    }


    public static List<VcfRecordStat> calculateStats(List<VcfRecord> list_vcf_records, List<String> sampleNames, Pedigree ped) {
        List<VcfRecordStat> list_stats = new ArrayList<VcfRecordStat>(list_vcf_records.size());
        VcfRecordStat vcf_record_stat;
        for (VcfRecord vcf_record : list_vcf_records) {
            vcf_record_stat = calculateStats(vcf_record, sampleNames, ped);
            list_stats.add(vcf_record_stat);
        }
        return list_stats;
    }

    public static List<VcfRecordStat> calculateStats(List<VcfRecord> list_vcf_records) {
        return calculateStats(list_vcf_records, null, null);
    }

}
