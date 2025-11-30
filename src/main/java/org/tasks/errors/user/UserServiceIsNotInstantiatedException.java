package org.tasks.errors.user;

import org.tasks.errors.UserException;

public class UserServiceIsNotInstantiatedException extends UserException {
    public UserServiceIsNotInstantiatedException(Throwable throwable) {
        super(throwable);
    }
}
