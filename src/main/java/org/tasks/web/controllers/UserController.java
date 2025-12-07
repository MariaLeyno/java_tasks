package org.tasks.web.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.tasks.errors.UserException;
import org.tasks.errors.user.AuthenticationFailedException;
import org.tasks.errors.user.PasswordNotValidException;
import org.tasks.errors.user.UserAlreadyExistsException;
import org.tasks.errors.user.UserNameNotValidException;
import org.tasks.errors.user.UserNotFoundException;
import org.tasks.model.EventType;
import org.tasks.service.user.UserService;
import org.tasks.web.annotations.Auditable;
import org.tasks.web.annotations.Loggable;
import org.tasks.web.dto.MessageDTO;
import org.tasks.web.dto.NewUserDTO;
import org.tasks.web.dto.TokenDTO;

@RestController
@Loggable
public class UserController {
    private static final String REGISTRATION_SUCCESSFUL = "New user's registration is successful";

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Auditable(eventType = EventType.SIGN_UP)
    @PostMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDTO registerNewUser(@Valid NewUserDTO userDTO) {
        try {
            userService.registerNewUser(userDTO);
        } catch (UserNameNotValidException | PasswordNotValidException | UserAlreadyExistsException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (UserException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return new MessageDTO(REGISTRATION_SUCCESSFUL);
    }

    @Auditable(eventType = EventType.SIGN_IN)
    @PutMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TokenDTO authenticateUser(@RequestParam("login") String login,
                                     @RequestParam("password") String password) {
        try {
            String token = userService.authenticateUser(login, password);
            return new TokenDTO(token);
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (AuthenticationFailedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (UserException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
