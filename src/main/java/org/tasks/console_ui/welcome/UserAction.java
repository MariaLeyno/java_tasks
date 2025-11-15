package org.tasks.console_ui.welcome;

import org.tasks.console_ui.Action;
import lombok.Getter;

public enum UserAction implements Action {
    SIGN_UP("Register as a new user"),
    SIGN_IN("Sign in");

    @Getter
    private final String message;

    UserAction(String message) {
        this.message = message;
    }

}
