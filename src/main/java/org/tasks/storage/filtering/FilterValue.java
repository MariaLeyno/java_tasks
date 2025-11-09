package org.tasks.storage.filtering;

public abstract class FilterValue implements Comparable<FilterValue> {
    public abstract FieldType getType();
    public abstract Object getValue();
}
