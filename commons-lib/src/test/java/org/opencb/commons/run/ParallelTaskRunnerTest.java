package org.opencb.commons.run;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.io.StringDataReader;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class ParallelTaskRunnerTest {


    protected static final int lines = 10000;
    protected static final String fileName = "/tmp/dummyFile.txt";
    protected static final String outputFileName = "/tmp/output.log";


    @BeforeAll
    public static void beforeClass() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

//        org.apache.logging.log4j.core.config.Configurator.reconfigure();
        for (int l = 0; l < lines; l++) {
            fileOutputStream.write(new StringBuilder()
                    .append(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(0, 16))).append(" ")
                    .append(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(0, 16))).append(" ")
                    .append(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(0, 16))).append("\n")
                    .toString().getBytes());
        }
    }

    @AfterAll
    public static void afterClass() throws IOException {
        if (Files.exists(Paths.get(fileName))) {
            Files.delete(Paths.get(fileName));
        }
        if (Files.exists(Paths.get(outputFileName))) {
            Files.delete(Paths.get(outputFileName));
        }
    }

    final Long[] l = {0l, 0l, 0l};
    Task<String, Integer> wc = strings -> {
        List<Integer> list = new ArrayList<>(strings.size());
        long lines = 0, words = 0, chars = 0;
        for (String string : strings) {
            if ((lines & 63) == 0) {
                System.out.println("[" + Thread.currentThread().getName() + "] ->" + (l[0] + lines));
            }
            list.add(string.length());
            lines++;                                     //lines
            words += string.split("[\n\t ]").length;     //words
            chars += string.length() + 1;                //chars
            for (int i = 1; i < 100; i++) {
                int ignored = (int) (string.length() * 0.5 + 5 * Math.log10(string.split("[\n\t ]").length * 50.0 * Math.abs(Math.sin(l[1]))));  //stupid operation
            }
        }
        synchronized (l) {
            l[0] += lines;
            l[1] += words;
            l[2] += chars;
        }
        return list;
    };

    @Test
    public void test() throws Exception {
        ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(8, 100, 10, false);

        l[0] = l[1] = l[2] = 0l;
        Path path = Paths.get(fileName);


        DataOutputStream os = new DataOutputStream(new FileOutputStream(outputFileName));
        DataWriter<Integer> dataWriter = batch -> {
            for (Integer integer : batch) {
                try {
                    os.writeBytes(integer + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        };

        ParallelTaskRunner<String, Integer> runner = new ParallelTaskRunner<>(new StringDataReader(path), wc, dataWriter, config);
        runner.run();
        System.out.println("WC : " + l[0] + " " + l[1] + " " + l[2]);

        os.close();

    }


    @Test
    public void testWithoutReader() throws Exception {
        ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(8, 100, 10, false);

        l[0] = l[1] = l[2] = 0l;
        Path path = Paths.get(fileName);
        final int[] generatedLines = {0};

        Task<String, Integer> generateAndwc = strings -> {
            //Generate data
            int linesToGenerate;
            synchronized (config) {
                linesToGenerate = lines - generatedLines[0] > 100 ? 100 : Math.max(lines - generatedLines[0], 0);
                generatedLines[0] += linesToGenerate;
            }
            strings = new ArrayList<>(linesToGenerate);
            for (int i = 0; i < linesToGenerate; i++) {
                strings.add(new StringBuilder()
                        .append(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(0, 16))).append(" ")
                        .append(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(0, 16))).append(" ")
                        .append(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(0, 16))).append("\n")
                        .toString());
            }
            return wc.apply(strings);
        };

        DataOutputStream os = new DataOutputStream(new FileOutputStream(outputFileName));
        DataWriter<Integer> dataWriter = batch -> {
            for (Integer integer : batch) {
                try {
                    os.writeBytes(integer + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        };

        ParallelTaskRunner<String, Integer> runner = new ParallelTaskRunner<>(null, generateAndwc, dataWriter, config);
        runner.run();
        System.out.println("WC : " + l[0] + " " + l[1] + " " + l[2]);

        os.close();

    }


    @Test
    public void testTimeOut() throws Exception {
        assertThrows(ExecutionException.class, () -> {
            final AtomicInteger i = new AtomicInteger(0);
            ParallelTaskRunner<String, Void> runner = new ParallelTaskRunner<>(
                    (size) -> {
                        return Collections.singletonList(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(0, 16)));
                    },
                    (batch) -> {
                        try {
                            if (i.addAndGet(1) > 10) {
                                System.out.println(Thread.currentThread().getName() + " -- sleeping 5s");
                                Thread.sleep(5000L);
                            } else {
                                System.out.println(Thread.currentThread().getName() + " -- don't sleep! " + i.get());
                            }
                        } catch (InterruptedException e) {
                            boolean exit = false;
                            while (!exit) {
                                try {
                                    //  System.err.println("Sleep! No ok!");
                                    Thread.sleep(2000L);
                                    exit = true;
                                } catch (InterruptedException ee) {
//                                exit = true;
                                    //                              e.printStackTrace();
                                }
                            }
                        }
                        try {
                            System.out.println("[" + Thread.currentThread().getName() + "] Start Sleep");
                            long start = System.currentTimeMillis();
                            Thread.sleep(1000);
                            System.out.println("[" + Thread.currentThread().getName() + "] Finish Sleep : " + (System.currentTimeMillis() - start));
                        } catch (Exception e) {
                            System.out.println("[" + Thread.currentThread().getName() + "] Sleep interrupted!! ###### ");
                        }
                        return null;
                    },
                    null,
                    new ParallelTaskRunner.Config(5, 1, 2, true, false, 2)
            );
            try {
                runner.run();
            } finally {
                System.out.println("Sleep 10s");
                Thread.sleep(10000);
            }
        });
    }

    @Test
    @Timeout(10)
    public void testReaderTimeOut() throws Exception {
        assertThrows(ExecutionException.class, () -> {
            ParallelTaskRunner<String, Void> runner = new ParallelTaskRunner<>(
                    (size) -> Arrays.asList(
                            RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1, 16)),
                            RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1, 16)),
                            RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1, 16)),
                            RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1, 16)),
                            RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1, 16))
                    ),
                    (batch) -> {
                        if (RandomUtils.nextBoolean()) {
                            return null;
                        }
                        try {
                            Thread.sleep(100000);
                        } catch (InterruptedException e) {
                            System.out.println("[" + Thread.currentThread().getName() + "] Interrupted");
                        }
                        return null;
                    },
                    null,
                    new ParallelTaskRunner.Config(5, 1, 2, true, false, 2)
            );


            runner.run();
        });
    }

    @Test
    public void testInterruptRunner() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(10000);
        ParallelTaskRunner<String, String> ptr = new ParallelTaskRunner<>(
                batchSize -> {
                    int i = count.getAndDecrement();
                    return i > 0 ? Collections.singletonList("i: " + i) : Collections.emptyList();
                },
                batch -> {
                    // Simulate work
                    Thread.sleep(50);
                    return batch;
                },
                batch -> {
                    for (String s : batch) {
                        System.out.println(s);
                    }
                    return true;
                }, ParallelTaskRunner.Config.builder().setNumTasks(8).setBatchSize(1).setAbortOnFail(true).build());

        Thread thread = new Thread(() -> {
            try {
                ptr.run();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        StopWatch watch = new StopWatch();

        thread.start();
        Thread.sleep(300);

        watch.start();
        thread.interrupt();
        thread.join();
        watch.stop();

        System.out.println(watch.getTime());
        assertTrue(watch.getTime() < 100);

    }

    @Test
    public void testInterruptWrongTaskRunner() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(10000);
        ParallelTaskRunner<String, String> ptr = new ParallelTaskRunner<>(
                batchSize -> {
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    int i = count.getAndDecrement();
                    return i > 0 ? Collections.singletonList("i: " + i) : Collections.emptyList();
                },
                batch -> {
                    // Simulate work
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return batch;
                },
                batch -> {
                    for (String s : batch) {
                        System.out.println(s);
                    }
                    return true;
                }, ParallelTaskRunner.Config.builder().setNumTasks(8).setBatchSize(1).setAbortOnFail(true).build());

        Thread thread = new Thread(() -> {
            try {
                ptr.run();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        StopWatch watch = new StopWatch();

        thread.start();
        Thread.sleep(300);

        watch.start();
        thread.interrupt();
        thread.join();
        watch.stop();

        System.out.println(watch.getTime());
        assertTrue(watch.getTime() < 100);

    }

    @Test
    public void testSorted() throws Exception {
        ParallelTaskRunner.Config config = ParallelTaskRunner.Config.builder()
                .setNumTasks(80)
                .setBatchSize(10)
                .setCapacity(10)
                .setSorted(true)
                .build();

        int limit = 10000;
        ArrayList<Integer> values = new ArrayList<>(limit);
        final int[] count = {0};

        ParallelTaskRunner<Integer, Integer> runner = new ParallelTaskRunner<>(batchSize -> {
            batchSize = RandomUtils.nextInt(1, 10);
            List<Integer> batch = new ArrayList<>(batchSize);
            for (int i = 0; i < batchSize && count[0] < limit; i++) {
                batch.add(count[0]++);
            }
            System.out.println("count[0] = " + count[0]);
            return batch;
        }, batch -> {
            System.out.println("batch.get(0) = " + batch.get(0));
            Thread.sleep(RandomUtils.nextInt(0, 20));
            return batch;
        }, batch -> {
            values.addAll(batch);
            return true;
        }, config);


        runner.run();

        ArrayList<Integer> expected = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            expected.add(i);
        }
        assertEquals(expected, values);

    }

    @Test
    @Timeout(10)
    public void testBlockAtWriterFailure() throws ExecutionException {
        assertThrows(ExecutionException.class, () -> {
            AtomicInteger i = new AtomicInteger();
            new ParallelTaskRunner<String, String>(
                    b -> IntStream.range(0, b).mapToObj(String::valueOf).collect(Collectors.toList()),
                    b -> b,
                    b -> {
                        try {
                            if (i.get() == 1) {
                                throw new RuntimeException("Fail!");
                            }
                            i.getAndIncrement();
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                        return true;
                    },
                    ParallelTaskRunner.Config.builder().setReadQueuePutTimeout(3, TimeUnit.SECONDS).build()
            ).run();
        });
    }
}