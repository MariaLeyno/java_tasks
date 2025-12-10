package org.tasks.model;

import lombok.Getter;

public enum FieldType {
    STRING(null),
    NUMBER("^(EQ|NE|LT|GT)\\s?(\\d+(\\.\\d+)?)$"),
    DATE_TIME(null);

    @Getter
    private final String filterPattern;

    FieldType(String filterPattern) {
        this.filterPattern = filterPattern;
    }
}
