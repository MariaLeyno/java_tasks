package org.tasks.console_ui.stock;

import org.tasks.console_ui.FillParametersScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class UpdateScreen extends FillParametersScreen<AddUpdateAction> {
    private static final String HEADER_MESSAGE = "Fill new values for fields to update (other fields leave empty).%n"
            + "To set null-value to 'Price' field, please, type '<null>'.%n";
    private static final String STR_NULL = "<null>";

    public UpdateScreen(Scanner scanner) {
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
        if (isEmpty(line)) {
            return true;
        }

        if (isStrNull(line)) {
            return action == AddUpdateAction.PRICE;
        }

        String pattern = action.getRestriction();
        return pattern == null || matches(line, pattern);
    }

    private boolean isStrNull(String value) {
        return STR_NULL.equals(value);
    }
}
