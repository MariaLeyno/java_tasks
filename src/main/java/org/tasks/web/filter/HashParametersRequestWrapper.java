package org.tasks.web.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HashParametersRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String[]> hashedParameters;

    public HashParametersRequestWrapper(HttpServletRequest request) {
        super(request);

        Map<String, String[]> parametersMap = request.getParameterMap();
        Base64.Encoder encoder = Base64.getEncoder();
        this.hashedParameters = new HashMap<>();
        this.hashedParameters.putAll(parametersMap);
        Map<String, String[]> passwords = parametersMap.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains("password"))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> Arrays.stream(entry.getValue()).map(String::getBytes)
                                .map(encoder::encode).map(String::new).toArray(String[]::new)));
        this.hashedParameters.putAll(passwords);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return hashedParameters;
    }

    @Override
    public String[] getParameterValues(String name) {
        return Optional.ofNullable(getParameterMap().get(name))
                .map(values -> Arrays.copyOf(values, values.length))
                .orElse(null);
    }

    @Override
    public String getParameter(String name) {
        return Optional.ofNullable(getParameterValues(name))
                .map(values -> values[0])
                .orElse(null);
    }
}
