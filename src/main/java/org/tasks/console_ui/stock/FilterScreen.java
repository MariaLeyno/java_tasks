package org.tasks.console_ui.stock;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import org.tasks.console_ui.FillParametersScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class FilterScreen extends FillParametersScreen<FilterAction> {
    private static final String HEADER_MESSAGE = "For each field you can add as many filters as you want.%n"
            + "Leave the field value empty if you don't want to add any more.%n"
            + "Filters for one field are combined by OR-operator.%n"
            + "Filters for different fields are combined by AND-operator.%n%n";
    @Getter
    private final Multimap<String, String> multiParameters = ArrayListMultimap.create();

    public FilterScreen(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected void printHeader() {
        super.printHeader();
        System.out.printf(HEADER_MESSAGE);
    }

    @Override
    protected List<FilterAction> getActionsList() {
        return Arrays.asList(FilterAction.values());
    }

    @Override
    protected boolean isValid(FilterAction action, String line) {
        String pattern = action.getRestriction();
        if (!isEmpty(line) && pattern != null) {
            return Pattern.compile(pattern).matcher(line).matches();
        }
        return true;
    }

    @Override
    protected boolean goNext(FilterAction action, String line) {
        return isEmpty(line);
    }

    @Override
    protected void addParameterValue(FilterAction action, String value) {
        if (!isEmpty(value)) {
            multiParameters.put(action.name(), value);
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
