package org.opencb.commons.run;

import junit.framework.TestCase;
import org.junit.Test;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.io.StringDataReader;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class ParallelTaskRunnerTest extends TestCase {

    @Test
    public void test() throws Exception {
        ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(8, 1000, 10, false);

        final Long[] l = {0l, 0l, 0l};
//        Path path = Paths.get("/home/hpccoll1/Documents/vcfs/10k.chr22.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf");
//        Path path = Paths.get("/home/hpccoll1/Documents/vcfs/100k.chr22.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz");
//        Path path = Paths.get("/home/hpccoll1/Documents/vcfs/10k.chr22.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz.variants.json");
        Path path = Paths.get("/home/hpccoll1/Documents/vcfs/100k.chr22.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz.variants.json");

        Function<List<String>, List<Integer>> wc = strings -> {
            LinkedList<Integer> list = new LinkedList<>();
            System.out.println(l[0]);
            long lines = 0, words = 0, chars = 0;
            for (String string : strings) {
                list.add(string.length());
                lines++;                                     //lines
                words += string.split("[\n\t ]").length;     //words
                chars += string.length() + 1;                //chars
                for (int i1 = 0; i1 < 10; i1++) {
                    int o = (int)(string.length()*0.5+5*Math.log10(string.split("[\n\t ]").length*50.0*Math.abs(Math.sin(l[1]))));                //stupid operation
                }
            }
            synchronized (l) {
                l[0] += lines;
                l[1] += words;
                l[2] += chars;
            }
            return list;
        };

//        ParallelTaskRunner.BatchFunction<String, Integer> wc2 = strings -> {
//            LinkedList<Integer> list = new LinkedList<>();
//            System.out.println(l[0]);
//            long lines = 0, words = 0, chars = 0;
//            for (String string : strings) {
//                list.add(string.length());
//                lines++;                                     //lines
//                words += string.split("[\n\t ]").length;     //words
//                chars += string.length() + 1;                //chars
//                for (int i1 = 0; i1 < 10; i1++) {
//                    int o = (int)(string.length()*0.5+5*Math.log10(string.split("[\n\t ]").length*50.0*Math.abs(Math.sin(l[1]))));                //stupid operation
//                }
//            }
//            synchronized (l) {
//                l[0] += lines;
//                l[1] += words;
//                l[2] += chars;
//            }
//            return list;
//        };

        DataWriter<Integer> dataWriter = new DataWriter<Integer>() {
            DataOutputStream os = new DataOutputStream(new FileOutputStream("/tmp/output.log"));
            @Override public boolean open() {return false;}
            @Override public boolean close() {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            @Override public boolean pre() {return false;}
            @Override public boolean post() {return false;}
            @Override public boolean write(Integer elem) {return false;}
            @Override
            public boolean write(List<Integer> batch) {
                for (Integer integer : batch) {
                    try {
                        os.writeBytes(integer + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        };
        ParallelTaskRunner<String, Integer> runner = new ParallelTaskRunner<>(new StringDataReader(path), wc, dataWriter, config);

        runner.run();
        System.out.println("WC : " + l[0] + " " + l[1] + " " + l[2]);
    }
}