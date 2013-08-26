package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.bioinfo.commons.utils.ListUtils;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;

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

    public static VcfRecordStat calculateStats(VcfRecord vcf_record){
        VcfRecordStat vcf_stat= new  VcfRecordStat();
        vcf_stat.setChromosome(vcf_record.getChromosome());
        vcf_stat.setPosition(new Long(vcf_record.getPosition()));
        vcf_stat.setRef_allele(vcf_record.getReference());
        vcf_stat.setMaf(Float.MAX_VALUE);
        vcf_stat.setMgf(Float.MAX_VALUE);

        String[] alt_alleles = vcf_record.getAlternate().split(",");

        vcf_stat.setAlt_alleles(alt_alleles);
        vcf_stat.setNum_alleles(alt_alleles.length);

        Integer[] alleles_count = new Integer[alt_alleles.length];
        Integer[] genotypes_count = new Integer[alt_alleles.length * alt_alleles.length];
        Float[]  alleles_freq = new Float[alt_alleles.length];
        Float[] genotypes_freq = new Float[alt_alleles.length * alt_alleles.length];



        String[]  format = vcf_record.getFormat().split(":");
        int gt_pos = Arrays.asList(format).indexOf("GT");

        if (gt_pos >= 0){

            for(String sample: vcf_record.getSamples()){
                String gt = sample.split(":")[gt_pos];
                String[]  alleles = gt.split("/");
                Integer allele_1 = Integer.valueOf(alleles[0]);
                Integer allele_2 = Integer.valueOf(alleles[1]);
                System.out.println(allele_1 + "--" + allele_2);

            }

        }



        return vcf_stat;

    }

    public static List<VcfRecordStat> calculateStats(List<VcfRecord> list_vcf_records){
        List<VcfRecordStat> list_stats = new ArrayList<VcfRecordStat>(list_vcf_records.size());
        VcfRecordStat vcf_record_stat;
        for(VcfRecord vcf_record: list_vcf_records){
            vcf_record_stat = calculateStats(vcf_record);
            list_stats.add(vcf_record_stat);
        }
        return list_stats;
    }
}
