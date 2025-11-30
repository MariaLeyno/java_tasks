package org.tasks.errors.user;

import org.tasks.errors.UserException;

public class UserAlreadyExistsException extends UserException {
    private static final String message = "User with name '%s' already exists";

    public UserAlreadyExistsException(String user) {
        super(String.format(message, user));
    }
}
