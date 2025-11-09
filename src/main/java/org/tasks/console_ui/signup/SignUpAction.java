package org.tasks.console_ui.signup;

import org.tasks.console_ui.ActionParameter;
import lombok.Getter;

public enum SignUpAction implements ActionParameter {
    LOGIN("Login: ", "Login should be at least 4 symbols"),
    PASSWORD("Password: ", "Password should be at least 6 symbols"),
    PASSWORD_AGAIN("Password once again: ", "Password should be at least 6 symbols");

    @Getter
    private String message;
    @Getter
    private String explanationMessage;

    SignUpAction(String message, String explanationMessage) {
        this.message = message;
        this.explanationMessage = explanationMessage;
    }
}
