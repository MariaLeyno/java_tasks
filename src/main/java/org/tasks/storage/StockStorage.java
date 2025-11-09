package org.tasks.storage;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.tasks.model.Item;
import org.tasks.storage.filtering.*;

import java.util.*;
import java.util.stream.Collectors;

public class StockStorage extends Storage<Item> {
    private static final String INFO = "%d items were loaded from the file";
    private static final String ITEMS_FILE = "stock/items";

    private Map<ItemField, Multimap<String, Item>> indexMap;
    private Map<Integer, ItemFilter> filterCache = new HashMap<>();

    StockStorage() {
        super(ITEMS_FILE, Item.class);

        createIndexes();
    }

    public Set<Item> findItemsByParameters(Multimap<String, String> parameters) {
        Set<ItemFilter> filters = getFilters(parameters);
        Set<Item> foundItems = new HashSet<>(dataMap.values());
        for (ItemFilter filter : filters) {
            Set<Item> filteredItems;
            if (filter.containsFilteredItems()) {
                filteredItems = new HashSet<>(filter.getItems());
            } else {
                ItemField field = filter.getField();
                Set<String> strValues = filter.getStringValues();
                if (indexMap.containsKey(field)) {
                    Multimap<String, Item> fieldIndex = indexMap.get(field);
                    filteredItems = strValues.stream()
                            .flatMap(val -> fieldIndex.get(val).stream())
                            .collect(Collectors.toSet());
                } else {
                    filteredItems = foundItems.stream().filter(item -> {
                        Object value = FilterMapper.getFieldValue(item, field);
                        if (value == null) {
                            return false;
                        }
                        if (field.getType() == FieldType.STRING) {
                            return strValues.contains(value.toString());
                        } else if (field.getType() == FieldType.NUMBER) {
                            boolean valueMaps = false;
                            for (NumberFilterValue numValue : filter.getNumberValues()) {
                                Double number = numValue.getValue();
                                Double itemNumber = (Double) value;
                                valueMaps = switch (numValue.getOperation()) {
                                    case EQ -> itemNumber.equals(number);
                                    case NE -> !itemNumber.equals(number);
                                    case GT -> itemNumber > number;
                                    case LT -> itemNumber < number;
                                    default -> false;
                                };
                                if (valueMaps) {
                                    break;
                                }
                            }
                            return valueMaps;
                        }
                        return false;
                    }).collect(Collectors.toSet());
                }
                filter.setItems(filteredItems);
            }

            foundItems = filteredItems.stream().filter(foundItems::contains).collect(Collectors.toSet());
        }

        updateCache(filters);

        return foundItems;
    }

    private Set<ItemFilter> getFilters(Multimap<String, String> parameters) {
        Set<ItemFilter> filters = new TreeSet<>();
        for(Map.Entry<String, Collection<String>> entry : parameters.asMap().entrySet()) {
            ItemField field = ItemField.valueOf(entry.getKey());
            ItemFilter filter = new ItemFilter(field, entry.getValue());

            int hash = filter.hashCode();
            if (filterCache.containsKey(hash)) {
                filter = filterCache.get(hash);
            }

            filters.add(filter);
        }
        return filters;
    }

    private void updateCache(Set<ItemFilter> filters) {
        filters.stream()
                .filter(ItemFilter::containsFilteredItems)
                .filter(filter -> !filterCache.containsKey(filter.hashCode()))
                .forEach(filter -> filterCache.put(filter.hashCode(), filter));
    }

    private void createIndexes() {
        Multimap<String, Item> brandIndex = TreeMultimap.create();
        Multimap<String, Item> categoryIndex = TreeMultimap.create();

        for (Item item : dataMap.values()) {
            brandIndex.put(item.getBrand(), item);
            categoryIndex.put(item.getCategory(), item);
        }

        indexMap = new HashMap<>();
        indexMap.put(ItemField.BRAND, brandIndex);
        indexMap.put(ItemField.CATEGORY, categoryIndex);
    }
}
