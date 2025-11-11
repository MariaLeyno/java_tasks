package org.tasks.storage.filtering;

import org.tasks.model.FieldType;
import org.tasks.model.Item;
import org.tasks.model.ItemField;

public class FilteringManager {

    public boolean itemMeetsFilter(Item item, ItemFilter filter) {
        ItemField field = filter.getField();
        Object value = getFieldValue(item, field);
        if (value == null) {
            return false;
        }
        if (field.getType() == FieldType.STRING) {
            return filter.getStringValues().contains(value.toString());
        } else if (field.getType() == FieldType.NUMBER) {
            boolean valueMatches = false;
            for (NumberFilterValue numValue : filter.getNumberValues()) {
                Double number = numValue.getValue();
                Double itemNumber = (Double) value;
                valueMatches = switch (numValue.getOperation()) {
                    case EQ -> itemNumber.equals(number);
                    case NE -> !itemNumber.equals(number);
                    case GT -> itemNumber > number;
                    case LT -> itemNumber < number;
                    default -> false;
                };
                if (valueMatches) {
                    break;
                }
            }
            return valueMatches;
        }
        return false;
    }

    public Object getFieldValue(Item item, ItemField field) {
        return switch (field) {
            case NAME -> item.getName();
            case CATEGORY -> item.getCategory();
            case BRAND -> item.getBrand();
            case PRICE -> item.getPrice();
            default -> null;
        };
    }
}
