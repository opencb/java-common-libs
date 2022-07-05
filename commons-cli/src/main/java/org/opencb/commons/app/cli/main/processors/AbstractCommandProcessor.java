package org.opencb.commons.app.cli.main.processors;

import org.opencb.commons.app.cli.CliOptionsParser;

public abstract class AbstractCommandProcessor {


    public abstract void process(String[] args, CliOptionsParser parser);

    public abstract String[] processShortCuts(String[] args);

}
