package org.tasks.service.user.errors;

import org.tasks.UserException;

public class UserNameNotValidException extends UserException {
    public UserNameNotValidException() {
        super("User name should be at least 4 symbols");
    }
}
