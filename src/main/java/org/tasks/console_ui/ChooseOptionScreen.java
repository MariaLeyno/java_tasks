package org.tasks.console_ui;

import java.util.Scanner;
import java.util.regex.Pattern;

public abstract class ChooseOptionScreen<T extends Action> extends ConsoleScreen<T> {
    private static final String CHOOSE_MESSAGE = "Choose the option:";
    private static final String INVALID_INPUT_MESSAGE = "Please, type number to choose the option or 'exit' to leave the app";
    private static final String INVALID_NUMBER_MESSAGE = "Please, choose number from %d to %d";
    private static final Pattern numberPattern = Pattern.compile("\\d+");

    protected T result;

    protected ChooseOptionScreen(Scanner scanner) {
        super(scanner);
    }

    public T getChoice() {
        return result;
    }

    @Override
    public void interact() {
        while(true) {
            print();
            String line = inputScanner.nextLine();
            orExit(line);
            if (!isValid(line)) {
                out.println(INVALID_INPUT_MESSAGE);
            } else {
                int choice = Integer.parseInt(line);
                result = getAction(choice);
                if (result == null) {
                    out.printf(INVALID_NUMBER_MESSAGE + "%n", 1, actions.size());
                } else {
                    break;
                }
            }
        }
    }

    private void print() {
        printHeader();
        out.println(CHOOSE_MESSAGE);
        for (int i = 0; i < actions.size(); i++) {
            out.println((i + 1) + ". " + actions.get(i).getMessage());
        }
    }

    private T getAction(int choice) {
        int index = choice - 1;
        if (index >= 0 && index < actions.size()) {
            return actions.get(index);
        } else {
            return null;
        }
    }

    private boolean isValid(String line) {
        return numberPattern.matcher(line).matches();
    }
}
