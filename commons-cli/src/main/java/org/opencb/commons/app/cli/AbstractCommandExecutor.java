/*
 * Copyright 2015-2017 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.commons.app.cli;

import com.beust.jcommander.JCommander;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.opencb.commons.app.cli.session.AbstractSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Created by imedina on 19/04/16.
 */
public abstract class AbstractCommandExecutor {

    protected String logLevel;
    protected String conf;

    protected String appHome;
    protected String userId;
    protected String token;


    protected String host;
    protected AbstractSessionManager sessionManager;

    protected GeneralCliOptions.CommonCommandOptions options;

    protected Logger logger;
    //  private Logger privateLogger;

    public AbstractCommandExecutor(GeneralCliOptions.CommonCommandOptions options, boolean loadClientConfiguration) {
        this.options = options;
        init(options.logLevel, options.conf, loadClientConfiguration);
    }

    public static String getParsedSubCommand(JCommander jCommander) {
        return CliOptionsParser.getSubCommand(jCommander);
    }

    private static void configureLogger(String logLevel) throws IOException {
        // Command line parameters have preference over anything
        if (StringUtils.isNotBlank(logLevel)) {
            Level level = Level.toLevel(logLevel);
            System.setProperty("opencga.log.level", level.name());
            Configurator.reconfigure();
        }
    }

    protected void init(String logLevel, String conf, boolean loadClientConfiguration) {
        this.logLevel = logLevel;
        this.conf = conf;

        // System property 'app.home' is automatically set up in opencga.sh. If by any reason
        // this is 'null' then OPENCGA_HOME environment variable is used instead.
        this.appHome = System.getProperty("app.home", System.getenv("OPENCGA_HOME"));

        if (StringUtils.isEmpty(conf)) {
            this.conf = appHome + "/conf";
        }

        // Loggers can be initialized, the configuration happens just below these lines
        logger = LoggerFactory.getLogger(this.getClass().toString());


        try {
            configureLogger(this.logLevel);

            loadConf(loadClientConfiguration);

            // We need to check if parameter --host has been provided.
            // Then set the host and make it the default
            sessionManager = configureSession();

            // Let's check the session file, maybe the session is still valid
//            privateLogger.debug("CLI session file is: {}", CliSessionManager.getInstance().getCurrentFile());
            logger.debug("CLI session file is: {}", this.sessionManager.getSessionPath(this.host).toString());

            if (StringUtils.isNotBlank(options.token)) {
                this.token = options.token;
            } else {
//                this.token = CliSessionManager.getInstance().getToken();
//                this.userId = CliSessionManager.getInstance().getUser();
                this.token = sessionManager.getSession().getToken();
                this.userId = sessionManager.getSession().getUser();

            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }


    }

    protected abstract AbstractSessionManager configureSession();

    public abstract void execute() throws Exception;

    public abstract void loadConf(boolean loadClientConfiguration) throws IOException;


    public String getLogLevel() {
        return logLevel;
    }

    public AbstractCommandExecutor setLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public String getConf() {
        return conf;
    }

    public AbstractCommandExecutor setConf(String conf) {
        this.conf = conf;
        return this;
    }

    public String getAppHome() {
        return appHome;
    }

    public AbstractCommandExecutor setAppHome(String appHome) {
        this.appHome = appHome;
        return this;
    }


    public AbstractSessionManager getSessionManager() {
        return sessionManager;
    }

    public AbstractCommandExecutor setSessionManager(AbstractSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        return this;
    }


}
