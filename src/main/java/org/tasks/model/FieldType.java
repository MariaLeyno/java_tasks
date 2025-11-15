package org.tasks.model;

import lombok.Getter;

public enum FieldType {
    STRING(null),
    NUMBER("^(EQ|NE|LT|GT)\\s?(\\d+(\\.\\d+)?)$");

    @Getter
    private final String pattern;

    FieldType(String pattern) {
        this.pattern = pattern;
    }
}
