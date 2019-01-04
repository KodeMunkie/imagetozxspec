package uk.co.silentsoftware.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildProperties {

    private static final Logger log = LoggerFactory.getLogger(BuildProperties.class);
    private static final String BUILD_PROPERTIES_PATH = "META-INF/maven/uk.co.silentsoftware/imagetozxspec/pom.properties";
    private static final Properties BUILD_PROPERTIES = new Properties();

    static {
        try (InputStream in = BuildProperties.class.getClassLoader().getResourceAsStream(BUILD_PROPERTIES_PATH)) {
            if (in == null) {
                log.error("Build properties do not exist on path {}", BUILD_PROPERTIES_PATH);
            } else {
                BUILD_PROPERTIES.load(in);
            }
        } catch (IOException e) {
            log.warn("Unable to load maven properties", e);
        }
    }

    public static String getProperty(String name) {
        return BUILD_PROPERTIES.getProperty(name, StringUtils.EMPTY);
    }
}
