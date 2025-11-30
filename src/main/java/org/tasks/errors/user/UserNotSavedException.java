package org.tasks.errors.user;

import org.tasks.errors.UserException;

public class UserNotSavedException extends UserException {
    public UserNotSavedException(Throwable throwable) {
        super(throwable);
    }
}
