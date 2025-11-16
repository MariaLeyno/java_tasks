package org.tasks.model;

import lombok.Getter;

public enum UserField implements DataObjectField {
    ID(FieldType.NUMBER),
    LOGIN(FieldType.STRING),
    PASSWORD(FieldType.STRING),
    PASSWORD_AGAIN(FieldType.STRING, false),
    ACCESS(FieldType.STRING);

    @Getter
    private final FieldType type;
    @Getter
    private final boolean toSave;

    UserField(FieldType type) {
        this(type, true);
    }

    UserField(FieldType type, boolean toSave) {
        this.type = type;
        this.toSave = toSave;
    }
}
