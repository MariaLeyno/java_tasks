package org.tasks.service.stock;

import com.google.common.collect.Multimap;
import org.tasks.DatabaseException;
import org.tasks.ItemException;
import org.tasks.database.ItemRepository;
import org.tasks.model.Item;
import org.tasks.model.ItemField;
import org.tasks.service.stock.errors.*;
import org.tasks.web.dto.ItemDTO;
import org.tasks.web.dto.ItemMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * StockStorage provides business logic for managing catalog items. It validates data from UI and requests
 * the internal storage to filter and manipulate items parameters and states. Then it returns results to UI.
 * In case of validation or internal errors the service throws {@link ItemException}
 */
public class StockService {

    /** String NULL constant is used to update an item field with null-value (if the field allows nulls). */
    private static final String STR_NULL = "<null>";

    /** Storage for catalog items */
    private final ItemRepository itemRepository;
    /** Service to map HTTP data transfer object and database entity */
    private final ItemMapper itemMapper;

    public StockService() throws StockServiceIsNotInstantiatedException {
        try {
            this.itemRepository = new ItemRepository();
        } catch (DatabaseException ex) {
            throw new StockServiceIsNotInstantiatedException(ex);
        }
        this.itemMapper = ItemMapper.INSTANCE;
    }

    /**
     * Method requests the internal storage to extract catalog items with the specified parameters.
     * @param parameters items parameters to filter
     * @return collection of catalog items
     */
    public List<ItemDTO> findItems(Multimap<String, String> parameters) throws ItemsNotFoundException {
        try {
            List<Item> items = itemRepository.findItemsByParameters(parameters);
            return itemMapper.fromEntitiesToDtoList(items);
        } catch (DatabaseException ex) {
            throw new ItemsNotFoundException(ex);
        }
    }

    /**
     * Method requests the internal storage to extract catalog items identifiers filtered with the specified parameters.
     * @param parameters items parameters to filter
     * @return collection of catalog items identifiers
     */
    public Set<String> findItemIds(Multimap<String, String> parameters) throws ItemsNotFoundException {
        try {
            return itemRepository.findItemIdsByParameters(parameters);
        } catch (DatabaseException ex) {
            throw new ItemsNotFoundException(ex);
        }
    }

    /**
     * Method requests the internal storage to delete catalog items with the specified identifiers.
     * @param itemIds collection of items identifiers
     * @return number of deleted catalog items
     * @throws ItemsNotDeletedException - if deleting failed
     */
    public int deleteItems(Set<String> itemIds) throws ItemsNotDeletedException {
        try {
            return itemRepository.deleteItems(itemIds);
        } catch (DatabaseException ex) {
            throw new ItemsNotDeletedException(ex);
        }
    }

    /**
     * Method requests the internal storage to update catalog items with specified identifiers
     * with new parameters values. If parameter value is empty, method skips it.
     * If parameter value is '<null>', method sets null-value to the parameter (if the field allows nulls).
     * @param itemIds collection of items identifiers
     * @param parameters collection of new parameters values
     * @return count of updated catalog items
     * @throws ItemsNotSavedException - if updating failed
     */
    public int updateItems(Set<String> itemIds, Map<String, String> parameters) throws ItemsNotSavedException {
        Map<ItemField, String> parametersToUpdate = new TreeMap<>();
        for (ItemField field : ItemField.values()) {
            String value = parameters.get(field.name());
            if (isEmpty(value)) {
                continue;
            }
            if (field == ItemField.PRICE && STR_NULL.equals(value)) {
                value = null;
            }
            parametersToUpdate.put(field, value);
        }

        return updateItemsInDatabase(itemIds, parametersToUpdate);
    }

    /**
     * Method requests the internal storage to update catalog items with specified identifiers
     * with new parameters values. If parameter value is empty, method skips it.
     * If parameter value is '<null>', method sets null-value to the parameter (if the field allows nulls).
     * @param itemIds collection of items identifiers
     * @param itemParameters new parameters values
     * @return count of updated catalog items
     * @throws ItemsNotSavedException - if updating failed
     */
    public int updateItems(Set<String> itemIds, ItemDTO itemParameters) throws ItemsNotSavedException {
        Map<ItemField, String> parametersToUpdate = new TreeMap<>();
        if (!isEmpty(itemParameters.getName())) {
            parametersToUpdate.put(ItemField.NAME, itemParameters.getName());
        }
        if (!isEmpty(itemParameters.getCategory())) {
            parametersToUpdate.put(ItemField.CATEGORY, itemParameters.getCategory());
        }
        if (!isEmpty(itemParameters.getBrand())) {
            parametersToUpdate.put(ItemField.BRAND, itemParameters.getBrand());
        }
        String price = itemParameters.getPrice();
        if (!isEmpty(price)) {
            if (STR_NULL.equals(price)) {
                price = null;
            }
            parametersToUpdate.put(ItemField.PRICE, price);
        }

        return updateItemsInDatabase(itemIds, parametersToUpdate);
    }

    private int updateItemsInDatabase(Set<String> ids, Map<ItemField, String> parametersToUpdate) throws ItemsNotSavedException {
        try {
            return itemRepository.updateItems(ids, parametersToUpdate);
        } catch (DatabaseException ex) {
            throw new ItemsNotSavedException(ex);
        }
    }

    /**
     * Method creates a new catalog item with specified parameters values and stores it to the database.
     * @param parameters collection of item parameters values
     * @throws ItemException - if item parameters are invalid or storing failed
     */
    public void addNewItem(Map<String, String> parameters) throws ItemException {
        Item newItem = getNewItem(parameters);
        storeNewItemToDatabase(newItem);
    }

    /**
     * Method creates a new catalog item with specified parameters values and stores it to the database.
     * @param itemDTO data transfer object with item parameter values
     * @throws ItemException - if item parameters are invalid or storing failed
     */
    public void addNewItem(ItemDTO itemDTO) throws ItemException {
        Item newItem = itemMapper.fromDtoToEntity(itemDTO);
        storeNewItemToDatabase(newItem);
    }

    /**
     * Method requests the internal storage to store a new catalog item.
     * @param newItem new catalog item
     * @throws ItemsNotSavedException - if item storing failed
     */
    private void storeNewItemToDatabase(Item newItem) throws ItemsNotSavedException {
        try {
            itemRepository.addNewItem(newItem);
        } catch (DatabaseException ex) {
            throw new ItemsNotSavedException(ex);
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
