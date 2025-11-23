package org.tasks.model;

public interface DataObjectField {
    FieldType getType();
    boolean isToSave();
}
