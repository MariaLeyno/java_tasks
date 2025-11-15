package org.tasks.storage;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.tasks.model.Item;
import org.tasks.model.ItemField;
import org.tasks.storage.filtering.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StockStorage extends Storage<Item> {
    private static final String INFO = "%d items were loaded from the file";
    private static final String ITEMS_FILE = "stock/items";

    private FilteringManager filteringManager = new FilteringManager();
    private Map<ItemField, Multimap<String, String>> indexMap;
    private Map<Integer, ItemFilter> filterCache = new HashMap<>();

    StockStorage() {
        super(ITEMS_FILE, Item.class);

        createIndexes();
    }

    @Override
    public int deleteDataObjets(Set<String> itemIds) throws IOException {
        int count = super.deleteDataObjets(itemIds);

        deleteFromIndexes(itemIds);
        clearCache();

        return count;
    }

    @Override
    public int saveDataObjects(Set<Item> items) throws IOException {
        int count = super.saveDataObjects(items);

        updateIndexes(items);
        clearCache();

        return count;
    }

    @Override
    public void addNewDataObject(Item item) throws IOException {
        saveDataObjects(Set.of(item));
    }

    public Set<String> findItemIdsByParameters(Multimap<String, String> parameters) {
        return findItemsByParameters(parameters).stream().map(Item::getId).map(UUID::toString).collect(Collectors.toSet());
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
                    Multimap<String, String> fieldIndex = indexMap.get(field);
                    filteredItems = strValues.stream()
                            .flatMap(val -> fieldIndex.get(val).stream())
                            .map(primary -> dataMap.get(primary))
                            .collect(Collectors.toSet());
                } else {
                    filteredItems = dataMap.values().stream()
                            .filter(item -> filteringManager.itemMeetsFilter(item, filter))
                            .collect(Collectors.toSet());
                }
                filter.setItems(filteredItems);
            }

            foundItems = filteredItems.stream().filter(foundItems::contains).collect(Collectors.toSet());
        }

        addToCache(filters);

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

    private void clearCache() {
        filterCache.clear();
    }

    private void addToCache(Set<ItemFilter> filters) {
        filters.stream()
                .filter(ItemFilter::containsFilteredItems)
                .filter(filter -> !filterCache.containsKey(filter.hashCode()))
                .forEach(filter -> filterCache.put(filter.hashCode(), filter));
    }

    private void deleteFromIndexes(Set<String> items) {
        Multimap<String, String> brandIndex = indexMap.get(ItemField.BRAND);
        Multimap<String, String> categoryIndex = indexMap.get(ItemField.CATEGORY);
        items.forEach(primary -> {
            deleteFromIndex(brandIndex, primary);
            deleteFromIndex(categoryIndex, primary);
        });
    }

    private void deleteFromIndex(Multimap<String, String> index, String primary) {
        index.entries().stream()
                .filter(entry -> primary.equals(entry.getValue()))
                .map(Map.Entry::getKey).toList().forEach(oldValue -> index.remove(oldValue, primary));
    }

    private void updateIndexes(Set<Item> items) {
        for (Item item : items) {
            String primary = item.getPrimary();
            updateIndex(indexMap.get(ItemField.BRAND), primary, item.getBrand());
            updateIndex(indexMap.get(ItemField.CATEGORY), primary, item.getCategory());
        }
    }

    private void updateIndex(Multimap<String, String> index, String primary, String value) {
        if(!index.containsEntry(value, primary)) {
            deleteFromIndex(index, primary);
            index.put(value, primary);
        }
    }

    private void createIndexes() {
        Multimap<String, String> brandIndex = TreeMultimap.create();
        Multimap<String, String> categoryIndex = TreeMultimap.create();

        for (Item item : dataMap.values()) {
            brandIndex.put(item.getBrand(), item.getPrimary());
            categoryIndex.put(item.getCategory(), item.getPrimary());
        }

        indexMap = new HashMap<>();
        indexMap.put(ItemField.BRAND, brandIndex);
        indexMap.put(ItemField.CATEGORY, categoryIndex);
    }
}
