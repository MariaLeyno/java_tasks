package org.tasks;

import jakarta.servlet.*;
import org.tasks.database.utility.DbManager;
import org.tasks.database.utility.DbManagerException;
import org.tasks.database.utility.DbParameters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StartUpManager extends GenericServlet {
    private static String POSTGRES_PROPERTIES_FILE = "/WEB-INF/classes/postgresql.env";
    private static String APPLICATION_PROPERTIES_FILE = "/WEB-INF/classes/application.properties";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            loadApplicationProperties(config.getServletContext());
            DbManager.getInstance();
        } catch (IOException | DbManagerException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {}

    private void loadApplicationProperties(ServletContext servletContext) throws IOException {
        try (InputStream postgresEnv = servletContext.getResourceAsStream(POSTGRES_PROPERTIES_FILE);
             InputStream appProperties = servletContext.getResourceAsStream(APPLICATION_PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(postgresEnv);
            properties.load(appProperties);
            extractParametersValues(properties);
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
