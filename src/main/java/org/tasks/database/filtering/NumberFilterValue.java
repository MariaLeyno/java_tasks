package org.tasks.database.filtering;

import lombok.Getter;
import org.tasks.model.FieldType;

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

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
