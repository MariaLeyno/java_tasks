package org.tasks.console_ui.common;

import org.tasks.console_ui.FillParametersScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class YesNoScreen extends FillParametersScreen<YesNoAction> {
    private final String question;

    public YesNoScreen(String question, Scanner scanner) {
        super(scanner);
        this.question = question;
    }

    public YesNoValue getAnswer() {
        String answer = inputParameters.get(YesNoAction.YES_NO);
        if (!isEmpty(answer)) {
            return YesNoValue.valueOf(answer.toUpperCase());
        }
        return null;
    }

    @Override
    protected void printHeader() {
        super.printHeader();
        out.printf(question);
    }

    @Override
    protected List<YesNoAction> getActionsList() {
        return Arrays.asList(YesNoAction.values());
    }

    @Override
    protected boolean isValid(YesNoAction action, String line) {
        String pattern = action.getRestriction();
        if (!isEmpty(line) && pattern != null) {
            return matches(line, pattern);
        }
        return false;
    }
}
