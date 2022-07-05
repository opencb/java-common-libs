package org.opencb.commons.app.cli.main;

import org.apache.commons.lang3.ArrayUtils;
import org.opencb.commons.app.cli.CliOptionsParser;
import org.opencb.commons.app.cli.main.processors.AbstractCommandProcessor;
import org.opencb.commons.app.cli.main.shell.Shell;
import org.opencb.commons.app.cli.main.utils.CommandLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

public abstract class CommandLine {

    private final Logger logger = LoggerFactory.getLogger(CommandLine.class);
    public Mode mode = Mode.CLI;
    public Shell shell;
    public Level logLevel = Level.OFF;

    private final String appName;

    public CommandLine(String appName) {
        this.appName = appName;
    }

    public void init(String[] args) {

        if (args.length == 0) {
            getOptionParser().printUsage();
            System.exit(0);
        }
        checkLogLevel(args);
        checkMode(args);
        logger.debug(Arrays.toString(args));
        try {
            if (Mode.SHELL.equals(getMode())) {
                executeShell(args);
            } else {
                executeCli(args);
            }
            logger.debug("Created command line in " + getMode() + " mode");
        } catch (Exception e) {
            CommandLineUtils.error("Failed to initialize OpenCGA CLI", e);
            logger.error("Failed to initialize OpenCGA CLI " + e.getMessage(), e);
        }
    }

    private void checkMode(String[] args) {
        if (ArrayUtils.contains(args, "--shell")) {
            setMode(Mode.SHELL);
        } else {
            setMode(Mode.CLI);
        }
        logger.debug("Execution mode " + getMode());
    }

    private void checkLogLevel(String[] args) {
        if (ArrayUtils.contains(args, "--log-level")) {
            String level = "";
            try {
                level = args[ArrayUtils.indexOf(args, "--log-level") + 1].toLowerCase(Locale.ROOT);
                Level logLevel = getNormalizedLogLevel(level);
                setLogLevel(logLevel);

                logger.debug("Console verbose mode: " + logLevel);
            } catch (Exception e) {
                setLogLevel(Level.SEVERE);
                CommandLineUtils.error("Invalid log level. Valid values are INFO, WARN, DEBUG, ERROR", e);
                logger.error("Invalid log level " + level + ": Valid values are INFO, WARN, DEBUG, ERROR", e);
                System.exit(0);
            }
        }
    }

    private static Level getNormalizedLogLevel(String level) {
        switch (level) {
            case "debug":
            case "fine":
                return Level.FINE;
            case "info":
                return Level.INFO;
            case "warning":
            case "warn":
                return Level.WARNING;
            case "error":
            case "severe":
                return Level.SEVERE;
            default:
                return Level.OFF;
        }
    }

    private void executeCli(String[] args) throws Exception {
        args = parseCliParams(args);
        if (!ArrayUtils.isEmpty(args)) {
            AbstractCommandProcessor processor = getProcessor();
            processor.process(args, getOptionParser());
        }
    }


    public void executeShell(String[] args) {
        logger.debug("Initializing Shell...  ");

        try {
            shell = getShell(args);
            logger.debug("Shell created ");
            // Launch execute command to begin the execution
            shell.execute();
        } catch (Exception e) {
            CommandLineUtils.error("Failed to execute shell", e);
            logger.error("Failed to execute shell", e);
        }
    }

    private static String[] normalizePasswordArgs(String[] args, String s) {
        for (int i = 0; i < args.length; i++) {
            if (s.equals(args[i])) {
                args[i] = "--password";
                break;
            }
        }
        return args;
    }

    public String[] parseCliParams(String[] args) {
        logger.debug("Executing " + CommandLineUtils.argsToString(args));
        if (CommandLineUtils.isNotHelpCommand(args)) {
            if (ArrayUtils.contains(args, "--user-password")) {
                normalizePasswordArgs(args, "--user-password");
            }
        }
        logger.debug("CLI parsed params ::: " + CommandLineUtils.argsToString(args));
        String shortcut = CommandLineUtils.getShortcut(args);
        args = getProcessor().processShortCuts(args);
        if (args != null) {
            logger.debug("Process shortcut result ::: " + CommandLineUtils.argsToString(args));
        } else {
            logger.debug("Is shortcut " + shortcut);
        }
        return args;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean isShellMode() {
        return getMode().equals(Mode.SHELL);
    }

    public abstract Shell getShell(String[] args) throws Exception;

    public abstract AbstractCommandProcessor getProcessor();

    public abstract CliOptionsParser getOptionParser();


    public void setShell(Shell shell) {
        this.shell = shell;
    }

    public Shell getShell() {
        return shell;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    public enum Mode {
        SHELL, CLI
    }

    public String getAppName() {
        return appName;
    }

}
