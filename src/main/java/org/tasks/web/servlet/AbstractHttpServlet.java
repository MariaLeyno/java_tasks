package org.tasks.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import org.tasks.AppPropertiesManager;

import java.io.IOException;

public abstract class AbstractHttpServlet extends HttpServlet {
    protected final String APPLICATION_JSON = "application/json";

    protected ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            AppPropertiesManager.loadApplicationProperties(config.getServletContext());
        } catch (IOException ex) {
            throw new ServletException(ex);
        }
        this.objectMapper = new ObjectMapper();
    }
}
