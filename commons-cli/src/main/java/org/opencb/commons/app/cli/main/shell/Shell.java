package org.opencb.commons.app.cli.main.shell;

import org.apache.commons.lang3.ArrayUtils;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.opencb.commons.app.cli.AbstractCommandExecutor;
import org.opencb.commons.app.cli.CliOptionsParser;
import org.opencb.commons.app.cli.GeneralCliOptions;
import org.opencb.commons.app.cli.main.processors.AbstractCommandProcessor;
import org.opencb.commons.app.cli.main.utils.CommandLineUtils;
import org.opencb.commons.app.cli.session.AbstractSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.opencb.commons.utils.PrintUtils.*;

public abstract class Shell {


    private LineReader lineReader = null;
    private Terminal terminal = null;
    private String host = null;
    protected Logger logger;

    protected final AbstractCommandExecutor executor;

    public Shell(GeneralCliOptions.CommonCommandOptions options, AbstractCommandExecutor executor) {
        if (options.host != null) {
            host = options.host;
        }
        this.executor = executor;
    }

    private LineReader getTerminal() {
        LineReader reader = null;
        logger = LoggerFactory.getLogger(Shell.class);
        try {
            if (terminal == null) {
                terminal = TerminalBuilder.builder()
                        .system(true).nativeSignals(true)
                        .build();

                System.out.print(eraseScreen());
                printShellHeaderMessage();
            }
            History defaultHistory = new DefaultHistory();

            // Register a shutdown-hook per JLine documentation to save history
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    defaultHistory.save();
                } catch (IOException e) {
                    CommandLineUtils.error("Failed to save terminal history", e);
                    logger.error("Failed to save terminal history", e);
                }
            }));
            reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .highlighter(new DefaultHighlighter())
                    .history(defaultHistory).completer(getCompleter())
                    .build();
        } catch (Exception e) {
            CommandLineUtils.error("Failed to create terminal ", e);
            logger.error("Failed to create terminal ", e);

        }

        return reader;
    }


    public void execute() throws Exception {
        try {
            if (lineReader == null) {
                lineReader = getTerminal();
            }
            String PROMPT;
            while (true) {
                // Read and sanitize the input
                String line;

                // Renew the prompt for set the current study, host and user
                PROMPT = getPrompt();

                // Read the shell command line for next execution
                try {
                    line = lineReader.readLine(PROMPT);
                } catch (UserInterruptException e) {
                    printWarn("If you want to close OpenCGA. Type \"exit\"");
                    continue;
                } catch (EndOfFileException e) {
                    break;
                }
                if (line == null) {
                    continue;
                }
                line = line.trim();

                // Send the line read to the processor for process
                if (!line.equals("")) {
                    String[] args = line.split(" ");
                    logger.debug("Command: " + line);

                    args = parseParams(args);
                    if (!ArrayUtils.isEmpty(args)) {
                        ArrayUtils.addAll(args, "--host", this.host);
                        getProcessor().process(args, getCliOptionsParser());
                    }
                }
            }
            terminal.writer().flush();
        } catch (Exception e) {
            CommandLineUtils.error("OpenCGA execution error ", e);
            logger.error("OpenCGA execution error ", e);
            logger.debug("sessionManager:" + this.executor.getSessionManager());
            logger.debug("getCliSession:" + this.executor.getSessionManager().getSession());

        }
    }

    private void printShellHeaderMessage() {

        println("     ███████                                    █████████    █████████    █████████  ", Color.GREEN);
        println("   ███░░░░░███                                 ███░░░░░███  ███░░░░░███  ███░░░░░███ ", Color.GREEN);
        println("  ███     ░░███ ████████   ██████  ████████   ███     ░░░  ███     ░░░  ░███    ░███ ", Color.GREEN);
        println("  ███      ░███░░███░░███ ███░░███░░███░░███ ░███         ░███          ░███████████ ", Color.GREEN);
        println("  ███      ░███ ░███ ░███░███████  ░███ ░███ ░███         ░███    █████ ░███░░░░░███ ", Color.GREEN);
        println("  ░███     ███  ░███ ░███░███░░░   ░███ ░███ ░░███     ███░░███  ░░███  ░███    ░███ ", Color.GREEN);
        println("  ░░░███████░   ░███████ ░░██████  ████ █████ ░░█████████  ░░█████████  █████   █████", Color.GREEN);
        println("    ░░░░░░░     ░███░░░   ░░░░░░  ░░░░ ░░░░░   ░░░░░░░░░    ░░░░░░░░░  ░░░░░   ░░░░░ ", Color.GREEN);
        println("                ░███                                                                 ", Color.GREEN);
        println("                █████                                                                ", Color.GREEN);
        println("               ░░░░░                                                                 ", Color.GREEN);

        println("");
        println(CommandLineUtils.getVersionString());
        println("");
        println("\nTo close the application type \"exit\"", Color.BLUE);
        println("");
        println("");
        println("");
        println("");
        // println("Opencga is running in " + this.executor.getSessionManager().getLogLevel() + " mode");
        println("");
        println("");
        println("");
        println("");
        println("");
        println("");
        println("");
        println("");
    }


    public String[] parseParams(String[] args) {
        logger.debug("Executing " + String.join(" ", args));


        if (args.length == 1 && "exit".equals(args[0].trim())) {
            println("\nThanks for using OpenCGA. See you soon.\n\n", Color.YELLOW);
            System.exit(0);
        }

        args = parseCustomParams(args);


        //Is for scripting login method
        if (isNotHelpCommand(args)) {
            if (ArrayUtils.contains(args, "--user-password")) {
                char[] passwordArray =
                        System.console().readPassword(format("\nEnter your password: ", Color.GREEN));
                ArrayUtils.addAll(args, "--password", new String(passwordArray));
                return args;
            }
        }
        return getProcessor().processShortCuts(args);

    }

    protected boolean isNotHelpCommand(String[] args) {
        return !ArrayUtils.contains(args, "--help") && !ArrayUtils.contains(args, "-h");
    }

    public abstract String[] parseCustomParams(String[] args);

    public abstract Completer getCompleter();

    public abstract CliOptionsParser getCliOptionsParser();

    public abstract AbstractCommandProcessor getProcessor();

    public abstract String getPrompt();

    public abstract AbstractSessionManager getSessionManager();


}
