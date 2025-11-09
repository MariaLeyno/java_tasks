package org.tasks.console_ui.signup;

import org.tasks.console_ui.FillParametersScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SignUpScreen extends FillParametersScreen<SignUpAction> {

    public SignUpScreen(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected List<SignUpAction> getActionsList() {
        return Arrays.asList(SignUpAction.values());
    }

    @Override
    protected boolean isValid(SignUpAction action, String line) {
        return switch (action) {
            case LOGIN -> line.length() >= 4;
            case PASSWORD, PASSWORD_AGAIN -> line.length() >= 6;
            default -> false;
        };
    }

    @Override
    protected boolean goNext(SignUpAction action, String line) {
        return true;
    }
}
