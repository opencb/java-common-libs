package org.opencb.variant.cli;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.*;
import org.opencb.variant.cli.servlets.GetFoldersServlet;
import org.opencb.variant.cli.servlets.HelloServlet;
import org.opencb.variant.lib.io.VariantAnnotRunner;
import org.opencb.variant.lib.io.VariantStatsRunner;
import org.opencb.variant.lib.io.variant.annotators.VcfAnnotator;
import org.opencb.variant.lib.io.variant.annotators.VcfControlAnnotator;
import org.opencb.variant.lib.io.variant.writers.VariantStatsFileDataWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/5/13
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class VariantMain {

    private static Options options;
    private static CommandLine commandLine;
    private static CommandLineParser parser;
    private static HelpFormatter help;

    private Logger logger;

    static {
        parser = new PosixParser();
        help = new HelpFormatter();
    }

    public VariantMain() {
        initOptions();


    }

    private static void initOptions() {
        options = new Options();

        options.addOption(OptionFactory.createOption("help", "h", "Print this message", false, false));
        options.addOption(OptionFactory.createOption("vcf-file", "Input VCF file", true, true));
        options.addOption(OptionFactory.createOption("outdir", "o", "Output dir", true, true));
        options.addOption(OptionFactory.createOption("out-file", "File output", false, false));
        options.addOption(OptionFactory.createOption("ped-file", "Ped file", false, true));
        options.addOption(OptionFactory.createOption("control", "Control filename", false, true));
        options.addOption(OptionFactory.createOption("control-list", "Control filename list", false, true));
        options.addOption(OptionFactory.createOption("control-prefix", "Control prefix", false, true));

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        initOptions();

        if (args.length == 0) {
            help.printHelp("variant", options);
            System.exit(-1);

        }
        String command = args[0];

        VariantStatsRunner vr;
        VariantAnnotRunner var = null;

        parse(args, false);

        switch (command) {
            case "index":
                System.out.println("===== INDEX =====");
                Runtime r = Runtime.getRuntime();
                Process p;

                String indexDir = commandLine.getOptionValue("outdir") + "/index";
                File indexFileDir = new File(indexDir);
                if (!indexFileDir.exists()) {
                    indexFileDir.mkdir();
                }

                String cmd = "python bin/indexerManager.py -t vcf -i " + commandLine.getOptionValue("vcf-file") + " --outdir " + indexDir;

                p = r.exec(cmd);
                p.waitFor();
                break;

            case "stats":
                System.out.println("===== STATS =====");
                vr = new VariantStatsRunner(commandLine.getOptionValue("vcf-file"), commandLine.getOptionValue("outdir") + "/stastCli.db", commandLine.getOptionValue("ped-file"));

                if (commandLine.hasOption("out-file")) {
                    vr.writer(new VariantStatsFileDataWriter(commandLine.getOptionValue("outdir")));
                }
                vr.run();
                break;

            case "filter":
                System.out.println("===== FILTER =====");
                System.out.println("Under construction");
                break;

            case "annot":
                System.out.println("===== ANNOT =====");

                List<VcfAnnotator> listAnnots = new ArrayList<>();
                VcfAnnotator control = null;
                String infoPrefix = commandLine.hasOption("control-prefix") ? commandLine.getOptionValue("control-prefix") : "CONTROL";

                var = new VariantAnnotRunner(commandLine.getOptionValue("vcf-file"), commandLine.getOptionValue("outdir") + "/" + "annot.vcf");

                if (commandLine.hasOption("control-list")) {
                    HashMap<String, String> controlList = getControlList(commandLine.getOptionValue("control-list"));
                    control = new VcfControlAnnotator(infoPrefix, controlList);

                } else if (commandLine.hasOption("control")) {
                    control = new VcfControlAnnotator(infoPrefix, commandLine.getOptionValue("control"));

                }


                listAnnots.add(control);

                var.annotations(listAnnots);
                var.run();


                break;

            case "server":
                System.out.println("===== SERVER =====");

                Tomcat tomcat;

                tomcat = new Tomcat();
                tomcat.setPort(31415);

                Context ctx = tomcat.addContext("/variant/rest", new File(".").getAbsolutePath());

                Tomcat.addServlet(ctx, "hello", new HelloServlet());
                ctx.addServletMapping("/hello", "hello");

                Tomcat.addServlet(ctx, "getdirs", new GetFoldersServlet());
                ctx.addServletMapping("/getdirs", "getdirs");


                try {
                    tomcat.start();
                    tomcat.getServer().await();

                } catch (LifecycleException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }


                break;

            default:
                help.printHelp("variant", options);
                System.exit(-1);
        }
    }

    private static HashMap<String, String> getControlList(String filename) {
        String line;
        HashMap<String, String> map = new LinkedHashMap<>();
        try {

            BufferedReader reader = new BufferedReader(new FileReader(filename));

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t");
                map.put(fields[0], fields[1]);

            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    private static boolean checkCommand(String command) {
        return command.equalsIgnoreCase("stats") || command.equalsIgnoreCase("filter") || command.equalsIgnoreCase("index") || command.equalsIgnoreCase("annot");
    }

    private static void parse(String[] args, boolean stopAtNoOption) {
        parser = new PosixParser();

        try {
            commandLine = parser.parse(options, args, stopAtNoOption);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            help.printHelp("variant", options);
            System.exit(-1);
        }
    }

}
