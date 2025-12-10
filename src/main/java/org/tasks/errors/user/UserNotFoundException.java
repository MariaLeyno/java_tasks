package org.tasks.errors.user;

import org.tasks.errors.UserException;

public class UserNotFoundException extends UserException {
    private static final String message = "User with name '%s' does not exist";

    public UserNotFoundException(String user) {
        super(String.format(message, user));
    }

    public UserNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
