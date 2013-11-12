package org.opencb.commons.bioformats.variant.vcf4.annotators;

import net.sf.samtools.util.StringUtil;
import org.broad.tribble.readers.TabixReader;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.variant.vcf4.stats.CalculateStats;
import org.opencb.commons.bioformats.variant.vcf4.stats.VcfVariantStat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA. User: aleman Date: 9/14/13 Time: 1:11 PM To
 * change this template use File | Settings | File Templates.
 */
public class VcfEVSControlAnnotator implements VcfAnnotator {

    private String tabixFile;
    private List<String> samples;
    private TabixReader tabix;
    private String prefix;

    public VcfEVSControlAnnotator(String infoPrefix, String control) {

        this.prefix = infoPrefix;
        this.tabixFile = control;
        try {
            this.tabix = new TabixReader(control);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void annot(List<VcfRecord> batch) {
        VcfRecord tabixRecord;

        for (VcfRecord record : batch) {
            try {
                TabixReader.Iterator it = this.tabix.query(record.getChromosome() + ":" + record.getPosition() + "-" + record.getPosition());

                if (it != null) {
                    String line = it.next();
                    while (it != null && line != null) {
                        String[] fields = line.split("\t");
                        tabixRecord = new VcfRecord(fields[0], Integer.valueOf(fields[1]), fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);


                        if (tabixRecord.getReference().equals(record.getReference()) && tabixRecord.getAlternate().equals(record.getAlternate())) {

                            parseAndAnnot(record, tabixRecord);


                        }
                        line = it.next();
                    }
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) { // If the Chr does not exist in Controls... TabixReader throws ArrayIndexOut...
                continue;
            }


        }

    }

    private void parseAndAnnot(VcfRecord record, VcfRecord tabixRecord) {

        Pattern pattern;
        Matcher matcher;
        String info = tabixRecord.getInfo();

        pattern = Pattern.compile("MAF=(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+)");
        matcher = pattern.matcher(info);
        if (matcher.find()) {
            float maf = Float.valueOf(matcher.group(3)) / 100;
            record.addInfoField(this.prefix + "_maf=" + String.format("%.3f", maf));
        }

//        if (tabixRecord.isIndel()) {
//            pattern = Pattern.compile("GTS=((\\w,?)*)");
//            matcher = pattern.matcher(info);
//            if(matcher.find()){
//                System.out.println(matcher.toString());
//            }
//
//        }else{
//            pattern = Pattern.compile("GTS=(([ACTG],?)*)");
//            matcher = pattern.matcher(info);
//            if(matcher.find()){
//                System.out.println(matcher.toString());
//            }
//        }


//        record.addInfoField(this.prefix + "_gt=.");
//        record.addInfoField(this.prefix + "_amaf=.");


    }

    @Override
    public void annot(VcfRecord elem) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}