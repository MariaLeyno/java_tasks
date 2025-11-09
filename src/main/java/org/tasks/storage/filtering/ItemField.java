package org.tasks.storage.filtering;

import lombok.Getter;

public enum ItemField {
    CATEGORY(FieldType.STRING),
    BRAND(FieldType.STRING),
    NAME(FieldType.STRING),
    PRICE(FieldType.NUMBER);

    @Getter
    private FieldType type;

    ItemField(FieldType type) {
        this.type = type;
    }
}
