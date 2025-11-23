package org.tasks.model;

import lombok.Getter;

public enum ItemField implements DataObjectField {
    ID(FieldType.NUMBER),
    CATEGORY(FieldType.STRING),
    BRAND(FieldType.STRING),
    NAME(FieldType.STRING),
    PRICE(FieldType.NUMBER);

    @Getter
    private final FieldType type;

    ItemField(FieldType type) {
        this.type = type;
    }

    @Override
    public boolean isToSave() {
        return true;
    }
}
