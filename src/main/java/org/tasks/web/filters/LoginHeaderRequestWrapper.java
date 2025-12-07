package org.tasks.web.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class LoginHeaderRequestWrapper extends HttpServletRequestWrapper {
    private static final String LOGIN_HEADER = "user";

    private final String login;

    public LoginHeaderRequestWrapper(HttpServletRequest request, String login) {
        super(request);
        this.login = login;
    }

    @Override
    public String getHeader(String name) {
        return LOGIN_HEADER.equals(name) ? login : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        ArrayList<String> headerNames = Collections.list(super.getHeaderNames());
        headerNames.add(LOGIN_HEADER);
        return Collections.enumeration(headerNames);
    }
}
