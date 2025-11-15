package org.tasks.service.user.errors;

import org.tasks.UserException;

public class UserAlreadyExistsException extends UserException {
    private static final String message = "User with name '%s' already exists";

    public UserAlreadyExistsException(String user) {
        super(String.format(message, user));
    }
}
