package org.tasks.console_ui.stock;

import lombok.Getter;
import org.tasks.console_ui.ActionParameter;

public enum FilterAction implements ActionParameter {
    CATEGORY("Category: ", FilterType.STRING),
    BRAND("Brand: ", FilterType.STRING),
    NAME("Name: ", FilterType.STRING),
    PRICE("Price: ", FilterType.NUMBER);

    @Getter
    private String message;
    private FilterType filterType;

    FilterAction(String message, FilterType filterType) {
        this.message = message;
        this.filterType = filterType;
    }

    @Override
    public String getExplanationMessage() {
        return filterType.explanation;
    }

    public String getRestriction() {
        return filterType.restriction;
    }

    enum FilterType {
        STRING("Type string value (or leave the filter empty)", null),
        NUMBER("Type expression with operators EQ/NE/LT/GT and number (or leave the filter empty)",
                "^(EQ|NE|LT|GT)\\s?(\\d+(\\.\\d+)?)$");

        private final String explanation;
        private final String restriction;

        FilterType(String explanation, String restriction) {
            this.explanation = explanation;
            this.restriction = restriction;
        }
    }
}
