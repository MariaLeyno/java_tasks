package org.tasks.storage.filtering;

import lombok.Getter;

public class NumberFilterValue extends FilterValue {
    @Getter
    private FieldType type = FieldType.NUMBER;
    @Getter
    private Double value;
    @Getter
    private NumberOperation operation;

    public NumberFilterValue(String value, String operation) {
        this.value = Double.valueOf(value);
        this.operation = NumberOperation.valueOf(operation);
    }

    @Override
    public int compareTo(FilterValue o) {
        if (type != o.getType()) {
            return type.compareTo(o.getType());
        }
        return value.compareTo((Double) o.getValue());
    }
}
