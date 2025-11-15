package org.tasks.storage.filtering;

import org.tasks.model.FieldType;

public abstract class FilterValue implements Comparable<FilterValue> {
    public abstract FieldType getType();
    public abstract Object getValue();
}
