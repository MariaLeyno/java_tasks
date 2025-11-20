package org.tasks.database.filtering;

import lombok.Getter;
import org.tasks.model.DataObjectField;
import org.tasks.model.FieldType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.tasks.database.SqlConstants.*;

public class FieldWithValues<T extends Enum<T> & DataObjectField> implements Comparable<FieldWithValues<T>> {
    @Getter
    private final T field;
    private final Set<FilterValue> values = new TreeSet<>();

    public FieldWithValues(T field, Collection<String> filterValues) {
        this.field = field;
        FieldType type = field.getType();
        if (type == FieldType.STRING) {
            filterValues.stream().map(StringFilterValue::new).forEach(this.values::add);
        } else if (type == FieldType.NUMBER) {
            Pattern pattern = Pattern.compile(field.getType().getFilterPattern());
            filterValues.forEach(str -> {
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    values.add(new NumberFilterValue(matcher.group(2), matcher.group(1)));
                }
            });
        }
    }

    public String getSqlRepresentation() {
        String fieldName = field.name();
        return switch (field.getType()) {
            case NUMBER -> values.stream().map(val -> {
                                NumberFilterValue numValue = (NumberFilterValue) val;
                                return fieldName + numValue.getOperation().getSqlOperation() + numValue.getValue();
                            }).collect(Collectors.joining(OR));
            case STRING -> values.stream()
                            .map(val -> fieldName + EQUAL + QUOTE + val.getValue().toString() + QUOTE)
                            .collect(Collectors.joining(OR));
            default -> null;
        };
    }

    @Override
    public int compareTo(FieldWithValues<T> o) {
        return field.compareTo(o.field);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FieldWithValues otherFilter)) {
            return false;
        }

        return field.equals(otherFilter.field)
                && values.size() == otherFilter.values.size()
                && new HashSet<>(values).containsAll(otherFilter.values);
    }

    @Override
    public int hashCode() {
        return field.hashCode() * values.hashCode();
    }
}
