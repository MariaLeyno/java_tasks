package org.tasks.model;

import lombok.Getter;

public enum AuditEventField implements DataObjectField {
    ID(FieldType.NUMBER),
    TYPE(FieldType.STRING),
    AUTHOR(FieldType.STRING),
    START_TIME(FieldType.DATE_TIME),
    END_TIME(FieldType.DATE_TIME),
    PARAMETERS(FieldType.STRING),
    RESULT(FieldType.STRING);

    @Getter
    private final FieldType type;

    AuditEventField(FieldType type) {
        this.type = type;
    }

    @Override
    public boolean isToSave() {
        return true;
    }
}
