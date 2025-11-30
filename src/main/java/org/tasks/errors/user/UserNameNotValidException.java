package org.tasks.errors.user;

import org.tasks.errors.UserException;

public class UserNameNotValidException extends UserException {
    public UserNameNotValidException() {
        super("User name should be at least 4 symbols");
    }
}
