package org.tasks.console_ui.welcome;

import org.tasks.console_ui.ChooseOptionScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class WelcomeScreen extends ChooseOptionScreen<UserAction> {

    public WelcomeScreen(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected List<UserAction> getActionsList() {
        return Arrays.asList(UserAction.values());
    }
}
