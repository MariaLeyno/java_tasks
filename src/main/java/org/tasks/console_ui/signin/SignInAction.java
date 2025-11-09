package org.tasks.console_ui.signin;

import org.tasks.console_ui.ActionParameter;
import lombok.Getter;

public enum SignInAction implements ActionParameter {
    LOGIN("Login: ", "Login shouldn't be empty"),
    PASSWORD("Password: ", "Password shouldn't be empty");

    @Getter
    private String message;
    @Getter
    private String explanationMessage;

    SignInAction(String message, String explanationMessage) {
        this.message = message;
        this.explanationMessage = explanationMessage;
    }
}
