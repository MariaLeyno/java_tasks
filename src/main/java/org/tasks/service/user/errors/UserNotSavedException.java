package org.tasks.service.user.errors;

import org.tasks.UserException;

public class UserNotSavedException extends UserException {
    public UserNotSavedException(Throwable throwable) {
        super(throwable);
    }
}
