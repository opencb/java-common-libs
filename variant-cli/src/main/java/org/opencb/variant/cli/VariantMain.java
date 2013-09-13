package org.opencb.variant.cli;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.*;
import org.opencb.variant.cli.servlets.GetFoldersServlet;
import org.opencb.variant.cli.servlets.HelloServlet;
import org.opencb.variant.lib.io.VariantStatsRunner;
import org.opencb.variant.lib.io.variant.writers.VcfFileDataWriter;

import java.io.*;
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

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        initOptions();

        if (args.length == 0) {
            help.printHelp("variant", options);
            System.exit(-1);

        }
        String command = args[0];

        VariantStatsRunner vr;

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
                    vr.writer(new VcfFileDataWriter(commandLine.getOptionValue("outdir")));
                }
                vr.run();
                break;

            case "filter":
                System.out.println("===== STATS =====");
                System.out.println("Under construction");
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

    private static boolean checkCommand(String command) {
        return command.equalsIgnoreCase("stats") || command.equalsIgnoreCase("filter") || command.equalsIgnoreCase("index");
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
