package org.tasks.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

public abstract class AbstractHttpServlet extends HttpServlet {
    protected final String APPLICATION_JSON = "application/json";

    protected ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.objectMapper = new ObjectMapper();
    }
}
