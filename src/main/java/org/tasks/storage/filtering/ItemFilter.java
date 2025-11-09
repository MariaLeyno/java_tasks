package org.tasks.storage.filtering;

import lombok.Getter;
import lombok.Setter;
import org.tasks.model.Item;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ItemFilter implements Comparable<ItemFilter> {
    @Getter
    private final ItemField field;
    private final Set<FilterValue> values = new TreeSet<>();
    @Getter
    @Setter
    private Set<Item> items;

    public ItemFilter(ItemField field, Collection<String> filterValues) {
        this.field = field;
        FieldType type = field.getType();
        if (type == FieldType.STRING) {
            filterValues.stream().map(StringFilterValue::new).forEach(this.values::add);
        } else if (type == FieldType.NUMBER) {
            Pattern pattern = Pattern.compile(field.getType().getPattern());
            filterValues.forEach(str -> {
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    values.add(new NumberFilterValue(matcher.group(2), matcher.group(1)));
                }
            });
        }
    }

    public boolean containsFilteredItems() {
        return items != null;
    }

    public Set<String> getStringValues() {
        return values.stream().map(filterValue -> filterValue.getValue().toString())
                .collect(Collectors.toSet());
    }

    public Set<NumberFilterValue> getNumberValues() {
        return values.stream().map(val -> (NumberFilterValue) val).collect(Collectors.toSet());
    }

    @Override
    public int compareTo(ItemFilter o) {
        return field.compareTo(o.field);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ItemFilter otherFilter) {
            return field.equals(otherFilter.field)
                    && values.size() == otherFilter.values.size()
                    && new HashSet<>(values).containsAll(otherFilter.values);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return field.hashCode() * values.hashCode();
    }
}
