package org.tasks.service.user.errors;

import org.tasks.UserException;

public class PasswordNotValidException extends UserException {

    public PasswordNotValidException() {
        super("Password should be at least 6 symbols");
    }

    public PasswordNotValidException(String message) {
        super(message);
    }
}
