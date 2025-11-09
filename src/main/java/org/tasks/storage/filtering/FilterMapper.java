package org.tasks.storage.filtering;

import org.tasks.model.Item;

public class FilterMapper {
    public static Object getFieldValue(Item item, ItemField field) {
        return switch (field) {
            case NAME -> item.getName();
            case CATEGORY -> item.getCategory();
            case BRAND -> item.getBrand();
            case PRICE -> item.getPrice();
            default -> null;
        };
    }
}
