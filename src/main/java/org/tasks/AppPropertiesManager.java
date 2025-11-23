package org.tasks;

import jakarta.servlet.ServletContext;
import org.tasks.database.utility.DbParameters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppPropertiesManager {
    private static String POSTGRES_PROPERTIES_FILE = "/WEB-INF/classes/postgresql.env";
    private static String APPLICATION_PROPERTIES_FILE = "/WEB-INF/classes/application.properties";

    private static boolean propertiesLoaded = false;

    public static void loadApplicationProperties(ServletContext servletContext) throws IOException {
        if (!propertiesLoaded) {
            try (InputStream postgresEnv = servletContext.getResourceAsStream(POSTGRES_PROPERTIES_FILE);
                 InputStream appProperties = servletContext.getResourceAsStream(APPLICATION_PROPERTIES_FILE)) {
                Properties properties = new Properties();
                properties.load(postgresEnv);
                properties.load(appProperties);
                extractParametersValues(properties);
            }
            propertiesLoaded = true;
        }
    }

    /**
     * Method stores application properties loaded from files.
     * @param properties application properties
     */
    private static void extractParametersValues(Properties properties) {
        for (DbParameters param : DbParameters.values()) {
            String value = properties.getProperty(param.name());
            if (value != null) {
                param.setValue(value);
            }
        }
    }
}
