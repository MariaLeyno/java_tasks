package org.tasks.database.filtering;

import lombok.Getter;
import org.tasks.model.FieldType;

public class StringFilterValue extends FilterValue {
    @Getter
    private FieldType type = FieldType.STRING;
    @Getter
    private String value;

    public StringFilterValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(FilterValue o) {
        if (type != o.getType()) {
            return type.compareTo(o.getType());
        }
        return value.compareTo((String) o.getValue());
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
