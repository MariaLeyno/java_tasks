package org.tasks.web.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.tasks.UserException;
import org.tasks.service.user.UserService;
import org.tasks.service.user.errors.*;
import org.tasks.web.dto.MessageDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(value = "/user")
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String body = req.getParameterMap().entrySet().stream()
                .map(entry -> {
                    StringBuilder str = new StringBuilder(entry.getKey() + ": [");
                    for (String val : entry.getValue()) {
                        str.append(val).append(", ");
                    }
                    str.append("]");
                    return str.toString();
                }).collect(Collectors.joining("\r\n"));
        resp.getOutputStream().write(body.getBytes());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> userData = getUserData(req.getParameterMap());

        String responseStr = "New user's registration is successful";
        int responseCode = 201;
        try {
            userService.registerNewUser(userData);
        } catch (UserNameNotValidException | PasswordNotValidException | UserAlreadyExistsException ex) {
            responseStr = ex.getMessage();
            responseCode = 400;
        } catch (UserException ex) {
            responseStr = ex.getMessage();
            responseCode = 500;
        }

        resp.setStatus(responseCode);
        resp.setContentType(APPLICATION_JSON);
        MessageDTO message = new MessageDTO(responseStr);
        resp.getOutputStream().write(objectMapper.writeValueAsBytes(message));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> userData = getUserData(req.getParameterMap());
        int responseCode = 202;
        try {
            userService.authenticateUser(userData);
        } catch (UserNotFoundException ex) {
            responseCode = 404;
        } catch (AuthenticationFailedException ex) {
            responseCode = 400;
        } catch (UserException ex) {
            responseCode = 500;
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
