package org.opencb.variant.lib.io.variant.writers;

import org.opencb.variant.lib.core.formats.VcfRecord;
import org.opencb.variant.lib.stats.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfFileDataWriter implements VcfDataWriter {

    private PrintWriter variantPw;
    private PrintWriter globalPw;
    private PrintWriter samplePw;
    private Map<String, Map<String, PrintWriter>> mapGroupPw;


    private String path;
    private String pathSampleGroup;
    private String pathGroup;


    public VcfFileDataWriter(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        this.path = path;

        mapGroupPw = new LinkedHashMap<>(2);
    }

    @Override
    public boolean open() {

        try {
            variantPw = new PrintWriter(new FileWriter(path + "variants.stats"));
            globalPw = new PrintWriter(new FileWriter(path + "global.stats"));
            samplePw = new PrintWriter(new FileWriter(path + "sample.stats"));

            Path dirPath = Paths.get(path + "sampleGroupStats");
            Path dir = Files.createDirectories(dirPath);
            this.pathSampleGroup = dir.toString() + "/";

            dirPath = Paths.get(path + "groupStats");
            dir = Files.createDirectories(dirPath);
            this.pathGroup = dir.toString() + "/";
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return true;
    }

    @Override
    public boolean close() {
        variantPw.close();
        globalPw.close();
        samplePw.close();

        PrintWriter pw;

        for (Map.Entry<String, Map<String, PrintWriter>> entry : mapGroupPw.entrySet()) {
            for (Map.Entry<String, PrintWriter> entryAux : entry.getValue().entrySet()) {
                pw = entryAux.getValue();
                pw.close();
            }
        }

        return true;
    }

    @Override
    public boolean pre() {
        writeVariantStatsHeader();

        return true;
    }

    @Override
    public boolean post() {
        return true;
    }

    @Override
    public boolean writeVariantStats(List<VcfVariantStat> data) {
        for (VcfVariantStat v : data) {
            variantPw.append(String.format("%-5s%-10d%-10s%-5s%-10s%-10s%-10s" +
                    "%-10d%-10d%-10d%-15s%-40s%-10d%-10d%-15d" +
                    "%-10.2f%-10.2f%-10.2f%-10.2f\n",
                    v.getChromosome(),
                    v.getPosition(),
                    (v.getIndel() ? "Y" : "N"),
                    v.getRefAlleles(),
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
        return true;
    }

    @Override
    public boolean writeGlobalStats(VcfGlobalStat globalStats) {

        globalPw.append("Number of variants = " + globalStats.getVariantsCount() + "\n");
        globalPw.append("Number of samples = " + globalStats.getSamplesCount() + "\n");
        globalPw.append("Number of biallelic variants = " + globalStats.getBiallelicsCount() + "\n");
        globalPw.append("Number of multiallelic variants = " + globalStats.getMultiallelicsCount() + "\n");
        globalPw.append("Number of SNP = " + globalStats.getSnpsCount() + "\n");
        globalPw.append("Number of indels = " + globalStats.getIndelsCount() + "\n");
        globalPw.append("Number of transitions = " + globalStats.getTransitionsCount() + "\n");
        globalPw.append("Number of transversions = " + globalStats.getTransversionsCount() + "\n");
        globalPw.append("Ti/TV ratio = " + ((float) globalStats.getTransitionsCount() / (float) globalStats.getTransversionsCount()) + "\n");
        globalPw.append("Percentage of PASS = " + (((float) globalStats.getPassCount() / (float) globalStats.getVariantsCount()) * 100) + "%\n");
        globalPw.append("Average quality = " + (globalStats.getAccumQuality() / (float) globalStats.getVariantsCount()) + "\n");

        return true;
    }

    @Override
    public boolean writeSampleStats(VcfSampleStat vcfSampleStat) {
        SampleStat s;
        samplePw.append(String.format("%-10s%-10s%-10s%-10s\n", "Sample", "MissGt", "Mendel Err", "Homoz Count"));
        for (Map.Entry<String, SampleStat> entry : vcfSampleStat.getSamplesStats().entrySet()) {
            s = entry.getValue();
            samplePw.append(String.format("%-10s%-10d%-10d%10d\n", s.getId(), s.getMissingGenotypes(), s.getMendelianErrors(), s.getHomozygotesNumber()));

        }
        return true;
    }

    @Override
    public boolean writeSampleGroupStats(VcfSampleGroupStats vcfSampleGroupStats) throws IOException {
        PrintWriter pw;
        String filename;
        VcfSampleStat sampleStat;
        SampleStat s;

        for (Map.Entry<String, VcfSampleStat> entry : vcfSampleGroupStats.getSampleStats().entrySet()) {
            filename = pathSampleGroup + "variant_stats_" + vcfSampleGroupStats.getGroup() + "_" + entry.getKey() + ".sample.stats";
            sampleStat = entry.getValue();
            pw = new PrintWriter(new FileWriter(filename));

            pw.append(String.format("%-10s%-10s%-10s%-10s\n", "Sample", "MissGt", "Mendel Err", "Homoz Count"));

            for (Map.Entry<String, SampleStat> entrySample : sampleStat.getSamplesStats().entrySet()) {
                s = entrySample.getValue();
                pw.append(String.format("%-10s%-10d%-10d%10d\n", s.getId(), s.getMissingGenotypes(), s.getMendelianErrors(), s.getHomozygotesNumber()));

            }
            pw.close();

        }

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean writeVariantGroupStats(VcfVariantGroupStat groupStats) throws IOException {
        PrintWriter pw;
        String filename;
        List<VcfVariantStat> list;

        Map<String, PrintWriter> auxMap;

        String group = groupStats.getGroup();
        auxMap = mapGroupPw.get(group);

        if (auxMap == null) {

            auxMap = new LinkedHashMap<>(2);

            for (Map.Entry<String, List<VcfVariantStat>> entry : groupStats.getVariantStats().entrySet()) {
                filename = pathGroup + "variant_stats_" + groupStats.getGroup() + "_" + entry.getKey() + ".stats";
                pw = new PrintWriter(new FileWriter(filename));

                pw.append(String.format("%-5s%-10s%-10s%-5s%-10s%-10s%-10s" +
                        "%-10s%-10s%-10s%-15s%-40s%-10s%-10s%-15s" +
                        "%-10s%-10s%-10s%-10s\n",
                        "Chr", "Pos", "Indel?", "Ref", "Alt", "Maf", "Mgf",
                        "NumAll.", "Miss All.", "Miss Gt", "All. Count", "Gt count", "Trans", "Transv", "Mend Error",
                        "Cases D", "Controls D", "Cases R", "Controls R"));

                auxMap.put(entry.getKey(), pw);
            }

            mapGroupPw.put(group, auxMap);


        }

        auxMap = mapGroupPw.get(group);

        for (Map.Entry<String, PrintWriter> entry : auxMap.entrySet()) {
            pw = entry.getValue();
            list = groupStats.getVariantStats().get(entry.getKey());
            for (VcfVariantStat v : list) {
                pw.append(String.format("%-5s%-10d%-10s%-5s%-10s%-10s%-10s" +
                        "%-10d%-10d%-10d%-15s%-40s%-10d%-10d%-15d" +
                        "%-10.2f%-10.2f%-10.2f%-10.2f\n",
                        v.getChromosome(),
                        v.getPosition(),
                        (v.getIndel() ? "Y" : "N"),
                        v.getRefAlleles(),
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


        return true;
    }

    @Override
    public boolean writeVariantIndex(List<VcfRecord> data) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void writeVariantStatsHeader() {
        variantPw.append(String.format("%-5s%-10s%-10s%-5s%-10s%-10s%-10s" +
                "%-10s%-10s%-10s%-15s%-40s%-10s%-10s%-15s" +
                "%-10s%-10s%-10s%-10s\n",
                "Chr", "Pos", "Indel?", "Ref", "Alt", "Maf", "Mgf",
                "NumAll.", "Miss All.", "Miss Gt", "All. Count", "Gt count", "Trans", "Transv", "Mend Error",
                "Cases D", "Controls D", "Cases R", "Controls R"));
    }
}
