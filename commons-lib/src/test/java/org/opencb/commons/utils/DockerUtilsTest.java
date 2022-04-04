package org.opencb.commons.utils;

import org.junit.Test;

import java.io.IOException;

public class DockerUtilsTest {

    @Test
    public void buildMountPathsCommandLine() throws IOException {
        String command = "samtools view -bS /home/user/trainning/dataset/corpasome-grch38/test1.sam > /home/user/trainning/dataset/corpasome-grch38/test1.bam";
        DockerUtils.buildMountPathsCommandLine("opencb/opencga-ext-tools:2.3.0", command);
        String command2 = "bwa mem -t 4 /workspace/index/index.fa /home/user/trainning/dataset/fastq/read1.fasq /home/user/trainning/dataset/fastq/read1_1.fasq > /home/user/trainning/dataset/corpasome-grch38/bam";
        DockerUtils.buildMountPathsCommandLine("opencb/opencga-ext-tools:2.3.0", command2);
    }

}
