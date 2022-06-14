package org.opencb.commons.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ConfigurationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);

    public static CommandLineConfiguration setUp() throws Exception {
        // Loading the YAML file from the /resources folder
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File file = new File(classLoader.getResource("cli-config.yaml").getFile());
        LOG.info("Loading CLI configuration from: " + file.getAbsolutePath());
        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        // Mapping the config from the YAML file to the Configuration class
        CommandLineConfiguration config = om.readValue(file, CommandLineConfiguration.class);
        return config;
    }
}
