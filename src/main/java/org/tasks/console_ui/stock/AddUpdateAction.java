package org.tasks.console_ui.stock;

import lombok.Getter;
import org.tasks.console_ui.ActionParameter;

public enum AddUpdateAction implements ActionParameter {
    CATEGORY("Category: ", FieldType.STRING),
    BRAND("Brand: ", FieldType.STRING),
    NAME("Name: ", FieldType.STRING),
    PRICE("Price: ", FieldType.NUMBER);

    @Getter
    private final String message;
    private final FieldType fieldType;

    @Override
    public String getExplanationMessage() {
        return fieldType.explanation;
    }

    public String getRestriction() {
        return fieldType.restriction;
    }

    AddUpdateAction(String message, FieldType fieldType) {
        this.message = message;
        this.fieldType = fieldType;
    }

    enum FieldType {
        STRING("Type string value", null),
        NUMBER("Type integer or double positive number (or leave the field empty for <null>)",
                "^(\\d+(\\.\\d+)?)$");

        private final String explanation;
        private final String restriction;

        FieldType(String explanation, String restriction) {
            this.explanation = explanation;
            this.restriction = restriction;
        }
    }
}
