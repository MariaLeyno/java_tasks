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
        }

        String login = checkAuthHeaderAndGetLogin(authHeader, requiredAccess);
        if (login == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            filterChain.doFilter(new LoginHeaderRequestWrapper(httpServletRequest, login), servletResponse);
        }
    }

    private String checkAuthHeaderAndGetLogin(String authHeader, UserAccess requiredAccess) {
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            return null;
        }
        String token = authHeader.substring(BEARER.length());
        return tokenService.validateTokenAndGetSubject(token, requiredAccess);
    }
}
