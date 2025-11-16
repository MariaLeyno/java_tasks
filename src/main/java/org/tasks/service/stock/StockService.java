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

/**
 * StockStorage provides business logic for managing catalog items. It validates data from UI and requests
 * the internal storage to filter and manipulate items parameters and states. Then it returns results to UI.
 * In case of validation or internal errors the service throws {@link ItemException}
 */
public class StockService {

    /** String NULL constant is used to update an item field with null-value (if the field allows nulls). */
    private static final String STR_NULL = "<null>";

    /** Storage for catalog items */
    private final StockStorage stockStorage;

    public StockService() {
        this.stockStorage = StorageFactory.getStockStorage();
    }

    /**
     * Method requests the internal storage to extract catalog items with the specified parameters.
     * @param parameters items parameters to filter
     * @return collection of catalog items
     */
    public Set<Item> findItems(Multimap<String, String> parameters) {
        return stockStorage.findItemsByParameters(parameters);
    }

    /**
     * Method requests the internal storage to extract catalog items identifiers filtered with the specified parameters.
     * @param parameters items parameters to filter
     * @return collection of catalog items identifiers
     */
    public Set<String> findItemIds(Multimap<String, String> parameters) {
        return stockStorage.findItemIdsByParameters(parameters);
    }

    /**
     * Method requests the internal storage to delete catalog items with the specified identifiers.
     * @param itemIds collection of items identifiers
     * @return number of deleted catalog items
     * @throws ItemsNotDeletedException - if deleting failed
     */
    public int deleteItems(Set<String> itemIds) throws ItemsNotDeletedException {
        try {
            return stockStorage.deleteDataObjets(itemIds);
        } catch (IOException ex) {
            throw new ItemsNotDeletedException(ex);
        }
    }

    /**
     * Method requests the internal storage to update catalog items with specified identifiers
     * with new parameters values.
     * @param itemIds collection of items identifiers
     * @param parametersToUpdate collection of new parameters values
     * @return count of updated catalog items
     * @throws ItemsNotSavedException - if updating failed
     */
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

    /**
     * Method requests the internal storage to store a new catalog item with specified parameters values.
     * @param parameters collection of item parameters values
     * @throws ItemException - if item parameters are invalid or storing failed
     */
    public void addNewItem(Map<String, String> parameters) throws ItemException {
        Item newItem = getNewItem(parameters);
        try {
            stockStorage.addNewDataObject(newItem);
        } catch (IOException ex) {
            throw new ItemsNotSavedException(ex);
        }
    }

    /**
     * Method updates catalog items parameters with new values. If parameter value is empty, method skips it.
     * If parameter value is '<null>', method sets null-value to the parameter (if the field allows nulls).
     * @param item catalog item data object
     * @param parameters collection of new values for item's parameters
     */
    private void updateItemWithParameters(Item item, Map<String, String> parameters) {
        for (ItemField field : ItemField.values()) {
            String value = parameters.get(field.name());
            if (isEmpty(value)) {
                continue;
            }

            switch (field) {
                case NAME -> item.setName(value);
                case CATEGORY -> item.setCategory(value);
                case BRAND -> item.setBrand(value);
                case PRICE -> {
                    if (STR_NULL.equals(value)) {
                        item.setPrice(null);
                    } else {
                        item.setPrice(Double.valueOf(value));
                    }
                }
            }
        }
    }

    /**
     * Method validates parameters values and, if they are valid, creates a new catalog item data object.
     * @param parameters collection of item's parameters
     * @return catalog item data object
     * @throws ItemParametersNotValidException - if parameters values are not valid
     */
    private Item getNewItem(Map<String, String> parameters) throws ItemParametersNotValidException {
        String name = null;
        String category = null;
        String brand = null;
        Double price = null;
        for (ItemField field : ItemField.values()) {
            String value = parameters.get(field.name());
            if (!isEmpty(value)) {
                switch (field) {
                    case NAME -> name = value;
                    case CATEGORY -> category = value;
                    case BRAND -> brand = value;
                    case PRICE -> price = Double.valueOf(value);
                }
            }
        }

        if (isEmpty(name) || isEmpty(category) || isEmpty(brand)) {
            throw new ItemParametersNotValidException();
        }

        return new Item(name, category, brand, price);
    }

    /**
     * Method checks if the string value is null or empty
     * @param value string value to check
     * @return is string value null or empty
     */
    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}