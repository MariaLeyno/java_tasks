package org.tasks.console_ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class FillParametersScreen<T extends ActionParameter> extends ConsoleScreen<T> {
    protected Map<T, String> inputParameters = new HashMap<>();

    protected FillParametersScreen(Scanner scanner) {
        super(scanner);
    }

    protected abstract boolean isValid(T action, String line);

    @Override
    public void interact() {
        printHeader();
        int i = 0;
        while (i < actions.size()) {
            T action = actions.get(i);
            out.print(action.getMessage());
            String line = inputScanner.nextLine();
            orExit(line);

            if (isValid(action, line)) {
                addParameterValue(action, line);
                if (goNext(action, line)) {
                    i++;
                }
            } else {
                out.println(action.getExplanationMessage());
            }
        }
    }

    public Map<String, String> getParameters() {
        return inputParameters.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), Map.Entry::getValue));
    }


    protected boolean goNext(T action, String line) {
        return true;
    }

    protected void addParameterValue(T action, String value) {
        inputParameters.put(action, value);
    }

    protected boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    protected boolean matches(String value, String pattern) {
        return Pattern.compile(pattern).matcher(value).matches();
    }
}
