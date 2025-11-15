package org.tasks.console_ui;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public abstract class ConsoleScreen<T extends Action> {
    private static final String EXIT = "exit";

    protected PrintStream out = System.out;
    protected Scanner inputScanner;
    protected List<T> actions;

    protected ConsoleScreen(Scanner scanner) {
        this.inputScanner = scanner;
        this.actions = getActionsList();
    }

    protected abstract List<T> getActionsList();
    public abstract void interact();

    protected void printHeader() {
        out.println();
    }

    protected void orExit(String line) {
        if (EXIT.equals(line)) {
            inputScanner.close();
            System.exit(0);
        }
    }
}
