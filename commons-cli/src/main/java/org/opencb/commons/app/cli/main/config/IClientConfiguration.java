package org.opencb.commons.app.cli.main.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;


public interface IClientConfiguration {

    static IClientConfiguration load(Path clientConfigurationPath) throws IOException {
        return null;
    }

    static IClientConfiguration load(InputStream configurationInputStream) throws IOException {
        return null;
    }

    static IClientConfiguration load(InputStream configurationInputStream, String format) throws IOException {
        return null;
    }

    void serialize(OutputStream configurationOutputStream) throws IOException;
}
