package org.tasks.console_ui.common;

import lombok.Getter;
import org.tasks.console_ui.ActionParameter;

public enum YesNoAction implements ActionParameter {
    YES_NO("Answer (Yes/No): ", "Please, type 'Yes' or 'No'", "^(?i)(YES|NO)$");

    @Getter
    private String message;
    @Getter
    private String explanationMessage;
    @Getter
    private String restriction;

    YesNoAction(String message, String explanationMessage, String restriction) {
        this.message = message;
        this.explanationMessage = explanationMessage;
        this.restriction = restriction;
    }
}
