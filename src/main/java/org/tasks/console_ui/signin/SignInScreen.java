package org.tasks.console_ui.signin;

import org.tasks.console_ui.FillParametersScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SignInScreen extends FillParametersScreen<SignInAction> {

    public SignInScreen(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected List<SignInAction> getActionsList() {
        return Arrays.asList(SignInAction.values());
    }

    @Override
    protected boolean isValid(SignInAction action, String line) {
        return switch (action) {
            case LOGIN -> !line.isBlank();
            case PASSWORD -> !line.isBlank();
            default -> false;
        };
    }

    @Override
    protected boolean goNext(SignInAction action, String line) {
        return true;
    }
}
