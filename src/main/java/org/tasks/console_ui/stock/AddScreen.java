package org.tasks.console_ui.stock;

import org.tasks.console_ui.FillParametersScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class AddScreen extends FillParametersScreen<AddUpdateAction> {
    private static final String HEADER_MESSAGE = "Fill all fields for a new item. 'Price' field could be empty.%n";

    public AddScreen(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected void printHeader() {
        super.printHeader();
        System.out.printf(HEADER_MESSAGE);
    }

    @Override
    protected List<AddUpdateAction> getActionsList() {
        return Arrays.asList(AddUpdateAction.values());
    }

    @Override
    protected boolean isValid(AddUpdateAction action, String line) {
        boolean isEmpty = isEmpty(line);
        if (isEmpty && (action == AddUpdateAction.NAME || action == AddUpdateAction.CATEGORY || action == AddUpdateAction.BRAND)) {
            return false;
        }

        String pattern = action.getRestriction();
        if (!isEmpty && pattern != null) {
            return matches(line, pattern);
        }
        return true;
    }
}
