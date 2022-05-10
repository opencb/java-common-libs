package org.opencb.commons.utils;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DockerUtilsTest {

    @Test
    public void buildMountPathsCommandLine() throws IOException {
        List<String> dockerOpts = new ArrayList<>();
        dockerOpts.add("--rm");
        dockerOpts.add("--log-driver none");

        String command = "samtools view -bS /home/user/trainning/dataset/corpasome-grch38/test1.sam > /home/user/trainning/dataset/corpasome-grch38/test1.bam";
        System.out.println(DockerUtils.buildMountPathsCommandLine("opencb/opencga-ext-tools:2.3.0", command, Collections.EMPTY_LIST));
        String command2 = "bwa mem -t 4 /workspace/index/index.fa /home/user/trainning/dataset/fastq/read1.fasq /home/user/trainning/dataset/fastq/read1_1.fasq > /home/user/trainning/dataset/corpasome-grch38/bam";
        System.out.println(DockerUtils.buildMountPathsCommandLine("opencb/opencga-ext-tools:2.3.0", command2, Collections.EMPTY_LIST));
        String command3 = "/home/juanfe/gatk-4.2.5.0/gatk HaplotypeCaller --input /home/juanfe/trainning/dataset/corpasome-grch37/output/bam/Falb_COL3.sorted.bam --output /home/juanfe/trainning/dataset/corpasome-grch37/output/vcf/Falb_COL3.vcf --reference /home/juanfe/trainning/index/Falbicolis.chr5.fa";
        System.out.println(DockerUtils.buildMountPathsCommandLine("opencb/opencga-ext-tools:2.3.0", command3, dockerOpts));

    }

}
