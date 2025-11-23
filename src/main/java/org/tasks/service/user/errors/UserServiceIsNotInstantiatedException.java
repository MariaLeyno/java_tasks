package org.tasks.service.user.errors;

import org.tasks.UserException;

public class UserServiceIsNotInstantiatedException extends UserException {
    public UserServiceIsNotInstantiatedException(Throwable throwable) {
        super(throwable);
    }
}
