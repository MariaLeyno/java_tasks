package org.tasks.errors.user;

import org.tasks.errors.UserException;

public class AuthenticationFailedException extends UserException {

    public AuthenticationFailedException() {
        super("Authentication failed");
    }
}
