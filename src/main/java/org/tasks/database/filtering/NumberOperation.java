package org.tasks.database.filtering;

import lombok.Getter;

public enum NumberOperation {
    EQ("="),
    NE("!="),
    LT("<"),
    GT(">");

    @Getter
    private final String sqlOperation;

    NumberOperation(String sqlOperation) {
        this.sqlOperation = sqlOperation;
    }
}
