package org.tasks.service.user.errors;

import org.tasks.UserException;

public class AuthenticationFailedException extends UserException {

    public AuthenticationFailedException() {
        super("Authentication failed");
    }
}
