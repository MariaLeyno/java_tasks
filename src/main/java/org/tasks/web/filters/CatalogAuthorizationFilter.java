package org.tasks.web.filters;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.tasks.model.UserAccess;
import org.tasks.service.security.TokenService;

import java.io.IOException;

@WebFilter(urlPatterns = { "/catalog" })
public class CatalogAuthorizationFilter implements Filter {
    private static final String BEARER = "Bearer ";

    private final TokenService tokenService = new TokenService();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String authHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String method = httpServletRequest.getMethod();

        UserAccess requiredAccess = "GET".equals(method) ? UserAccess.READ : UserAccess.CHANGE;

        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        if (authHeader == null || authHeader.isBlank()) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else if (!checkAuthHeader(authHeader, requiredAccess)) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean checkAuthHeader(String authHeader, UserAccess requiredAccess) {
        if (!authHeader.startsWith(BEARER)) {
            return false;
        }
        String token = authHeader.substring(BEARER.length());
        return tokenService.validateToken(token, requiredAccess);
    }
}
