/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.commons.app.cli.session;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.opencb.commons.app.cli.CliOptionsParser;
import org.opencb.commons.app.cli.main.config.IClientConfiguration;
import org.opencb.commons.utils.GitRepositoryState;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSessionManager {

    public static final String SESSION_FILENAME_SUFFIX = "_session.json";
    public static final String NO_TOKEN = "NO_TOKEN";
    public static final String NO_STUDY = "NO_STUDY";
    public static final String ANONYMOUS = "anonymous";
    protected final IClientConfiguration clientConfiguration;
    protected final String host;
    protected Path sessionFolder;
    protected ObjectWriter objectWriter;
    protected ObjectReader objectReader;
    protected Logger logger;
    protected CliOptionsParser parser;


    protected String logLevel;


    public AbstractSessionManager(IClientConfiguration clientConfiguration, String host) {
        this.clientConfiguration = clientConfiguration;
        this.host = host;
        this.init();
    }

    public abstract void init();


    private Session createEmptySession() {
        Session session = new Session();
        session.setHost(host);
        session.setVersion(GitRepositoryState.get().getBuildVersion());
        session.setTimestamp(System.currentTimeMillis());
        session.setStudies(new ArrayList());
        session.setCurrentStudy(NO_STUDY);
        session.setToken(NO_TOKEN);
        session.setUser(ANONYMOUS);

        return session;
    }

    public Path getSessionPath() {
        return getSessionPath(this.host);
    }

    public Path getSessionPath(String host) {
        return sessionFolder.resolve(host + SESSION_FILENAME_SUFFIX);
    }

    public Session getSession() {
        return getSession(this.host);
    }

    public Session getSession(String host) {
        Path sessionPath = sessionFolder.resolve(host + SESSION_FILENAME_SUFFIX);
        if (Files.exists(sessionPath)) {
            try {
                logger.debug("Retrieving session from file " + sessionPath);
                return objectReader.readValue(sessionPath.toFile());
            } catch (IOException e) {
                logger.debug("Could not parse the session file properly");
            }
        }
        logger.debug("Creating an empty session");
        Session session = createEmptySession();
        try {
            saveSession(session);
        } catch (IOException e) {
            logger.debug("Could not create the session file properly");
        }
        return session;
    }


    public void updateSessionToken(String token, String host) throws IOException {
        // Get current Session and update token
        Session session = getSession(host);
        session.setToken(token);

        // Save updated Session
        saveSession(session);
    }

    public void saveSession(String user, String token, String refreshToken, List<String> studies, String host)
            throws IOException {
        Session session = new Session(host, user, token, refreshToken, studies);
        if (CollectionUtils.isNotEmpty(studies)) {
            session.setCurrentStudy(studies.get(0));
        } else {
            session.setCurrentStudy(NO_STUDY);
        }
        saveSession(session, host);
    }

    public void refreshSession(String refreshToken, String host)
            throws IOException {
        Session session = getSession().setRefreshToken(refreshToken);

        saveSession(session, host);
    }

    public void saveSession() throws IOException {
        saveSession(getSession(), host);
    }

    public void saveSession(Session session) throws IOException {
        saveSession(session, host);
    }

    public void saveSession(Session session, String host) throws IOException {
        // Check if ~/.opencga folder exists
        if (!Files.exists(sessionFolder)) {
            Files.createDirectory(sessionFolder);
        }
        logger.debug("Saving '{}'", session);
        logger.debug("Session file '{}'", host + SESSION_FILENAME_SUFFIX);

        Path sessionPath = sessionFolder.resolve(host + SESSION_FILENAME_SUFFIX);
        objectWriter.writeValue(sessionPath.toFile(), session);
    }

    public void logoutSessionFile() throws IOException {
        // We just need to save an empty session, this will delete user and token for this host
        logger.debug("Session logout for host '{}'", host);
        saveSession(createEmptySession(), host);
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SessionManager{");
        sb.append("clientConfiguration=").append(clientConfiguration);
        sb.append(", host='").append(host).append('\'');
        sb.append(", Session='").append(getSession().toString()).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public CliOptionsParser getParser() {
        return parser;
    }

    public AbstractSessionManager setParser(CliOptionsParser parser) {
        this.parser = parser;
        return this;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public AbstractSessionManager setLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

}
