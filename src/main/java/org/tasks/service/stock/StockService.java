package org.tasks.service.stock;

import com.google.common.collect.Multimap;
import org.tasks.ItemException;
import org.tasks.model.Item;
import org.tasks.model.ItemField;
import org.tasks.service.stock.errors.ItemsNotSavedException;
import org.tasks.service.stock.errors.ItemParametersNotValidException;
import org.tasks.service.stock.errors.ItemsNotDeletedException;
import org.tasks.storage.StockStorage;
import org.tasks.storage.StorageFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class StockService {
    private static final String STR_NULL = "<null>";

    private final StockStorage stockStorage;

    public StockService() {
        this.stockStorage = StorageFactory.getStockStorage();
    }

    public Set<Item> findItems(Multimap<String, String> parameters) {
        return stockStorage.findItemsByParameters(parameters);
    }

    public Set<String> findItemIds(Multimap<String, String> parameters) {
        return stockStorage.findItemIdsByParameters(parameters);
    }

    public int deleteItems(Set<String> itemIds) throws ItemsNotDeletedException {
        try {
            return stockStorage.deleteDataObjets(itemIds);
        } catch (IOException ex) {
            throw new ItemsNotDeletedException(ex);
        }
    }

    public int updateItems(Set<String> itemIds, Map<String, String> parametersToUpdate) throws ItemsNotSavedException {
        Set<Item> items = stockStorage.findDataObjectsByPrimary(itemIds);
        for (Item item : items) {
            updateItemWithParameters(item, parametersToUpdate);
        }
        try {
            return stockStorage.saveDataObjects(items);
        } catch (IOException ex) {
            throw new ItemsNotSavedException(ex);
        }
    }

    public void addNewItem(Map<String, String> parameters) throws ItemException {
        Item newItem = getNewItem(parameters);
        try {
            stockStorage.addNewDataObject(newItem);
        } catch (IOException ex) {
            throw new ItemsNotSavedException(ex);
        }
    }

    private void updateItemWithParameters(Item item, Map<String, String> parameters) {
        for (ItemField field : ItemField.values()) {
            String value = parameters.get(field.name());
            if (isEmpty(value)) {
                continue;
            }

            switch (field) {
                case NAME:
                    item.setName(value);
                    break;
                case CATEGORY:
                    item.setCategory(value);
                    break;
                case BRAND:
                    item.setBrand(value);
                    break;
                case PRICE:
                    if (STR_NULL.equals(value)) {
                        item.setPrice(null);
                    } else {
                        item.setPrice(Double.valueOf(value));
                    }
            }
        }
    }

    private Item getNewItem(Map<String, String> parameters) throws ItemParametersNotValidException {
        String name = null;
        String category = null;
        String brand = null;
        Double price = null;
        for (ItemField field : ItemField.values()) {
            String value = parameters.get(field.name());
            if (!isEmpty(value)) {
                switch (field) {
                    case NAME:
                        name = value;
                        break;
                    case CATEGORY:
                        category = value;
                        break;
                    case BRAND:
                        brand = value;
                        break;
                    case PRICE:
                        price = Double.valueOf(value);
                }
            }
        }

        if (isEmpty(name) || isEmpty(category) || isEmpty(brand)) {
            throw new ItemParametersNotValidException();
        }

        return new Item(name, category, brand, price);
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
