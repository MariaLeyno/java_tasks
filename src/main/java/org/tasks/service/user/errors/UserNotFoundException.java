package org.tasks.service.user.errors;

import org.tasks.UserException;

public class UserNotFoundException extends UserException {
    private static final String message = "User with name '%s' does not exist";

    public UserNotFoundException(String user) {
        super(String.format(message, user));
    }
}
