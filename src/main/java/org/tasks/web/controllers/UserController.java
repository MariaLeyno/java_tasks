package org.tasks.web.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tasks.errors.UserException;
import org.tasks.errors.user.AuthenticationFailedException;
import org.tasks.errors.user.PasswordNotValidException;
import org.tasks.errors.user.UserAlreadyExistsException;
import org.tasks.errors.user.UserNameNotValidException;
import org.tasks.errors.user.UserNotFoundException;
import org.tasks.service.user.UserService;
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
