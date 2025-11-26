package org.tasks.web.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.tasks.UserException;
import org.tasks.service.user.UserService;
import org.tasks.service.user.errors.*;
import org.tasks.web.annotations.Loggable;
import org.tasks.web.dto.MessageDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/user")
@Loggable
public class UserServlet extends AbstractHttpServlet {

    private UserService userService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            this.userService = new UserService();
        } catch (UserServiceIsNotInstantiatedException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> userData = getUserData(req.getParameterMap());

        String responseStr = "New user's registration is successful";
        int responseCode = HttpServletResponse.SC_CREATED;
        try {
            userService.registerNewUser(userData);
        } catch (UserNameNotValidException | PasswordNotValidException | UserAlreadyExistsException ex) {
            responseStr = ex.getMessage();
            responseCode = HttpServletResponse.SC_BAD_REQUEST;
        } catch (UserException ex) {
            responseStr = ex.getMessage();
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        resp.setStatus(responseCode);
        resp.setContentType(APPLICATION_JSON);
        MessageDTO message = new MessageDTO(responseStr);
        resp.getOutputStream().write(objectMapper.writeValueAsBytes(message));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        Map<String, String> userData = getUserData(req.getParameterMap());
        int responseCode = HttpServletResponse.SC_ACCEPTED;
        try {
            String authToken = userService.authenticateUser(userData);
            resp.addHeader("Auth token", authToken);
        } catch (UserNotFoundException ex) {
            responseCode = HttpServletResponse.SC_NOT_FOUND;
        } catch (AuthenticationFailedException ex) {
            responseCode = HttpServletResponse.SC_BAD_REQUEST;
        } catch (UserException ex) {
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        resp.setStatus(responseCode);
    }

    private Map<String, String> getUserData(Map<String, String[]> httpParameters) {
        Map<String, String> userData = new HashMap<>();
        for (Map.Entry<String, String[]> entry : httpParameters.entrySet()) {
            String paramName = entry.getKey().toUpperCase();
            String paramValue = null;

            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                paramValue = values[0];
            }

            userData.put(paramName, paramValue);
        }
        return userData;
    }
}
