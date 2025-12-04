package org.tasks.service.stock;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tasks.errors.DatabaseException;
import org.tasks.errors.ItemException;
import org.tasks.database.ItemRepository;
import org.tasks.errors.stock.ItemsNotDeletedException;
import org.tasks.errors.stock.ItemsNotFoundException;
import org.tasks.errors.stock.ItemsNotSavedException;
import org.tasks.model.Item;
import org.tasks.model.ItemField;
import org.tasks.web.dto.FilterDTO;
import org.tasks.web.dto.ItemDTO;
import org.tasks.web.dto.UpdateItemDTO;
import org.tasks.web.mappers.ItemMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * StockStorage provides business logic for managing catalog items. It validates data from UI and requests
 * the internal storage to filter and manipulate items parameters and states. Then it returns results to UI.
 * In case of validation or internal errors the service throws {@link ItemException}
 */
@Service
public class StockService {

    /** String NULL constant is used to update an item field with null-value (if the field allows nulls). */
    private static final String STR_NULL = "<null>";

    /** Storage for catalog items */
    private final ItemRepository itemRepository;
    /** Service to map HTTP data transfer object and database entity */
    private final ItemMapper itemMapper;

    @Autowired
    public StockService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        this.itemMapper = ItemMapper.INSTANCE;
    }

    /**
     * Method requests the internal storage to extract catalog items with the specified parameters.
     * @param filters items parameters to filter
     * @return collection of catalog items
     */
    public List<ItemDTO> findItems(FilterDTO filters) throws ItemsNotFoundException {
        Multimap<String, String> parameters = getParametersToFilter(filters);
        try {
            List<Item> items = itemRepository.findItemsByParameters(parameters);
            return itemMapper.fromEntitiesToDtoList(items);
        } catch (DatabaseException ex) {
            throw new ItemsNotFoundException(ex);
        }
    }

    /**
     * Method requests the internal storage to extract catalog items identifiers filtered with the specified parameters.
     * @param filters items parameters to filter
     * @return collection of catalog items identifiers
     */
    public Set<String> findItemIds(FilterDTO filters) throws ItemsNotFoundException {
        Multimap<String, String> parameters = getParametersToFilter(filters);
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
     * @param itemParameters new parameters values
     * @return count of updated catalog items
     * @throws ItemsNotSavedException - if updating failed
     */
    public int updateItems(Set<String> itemIds, UpdateItemDTO itemParameters) throws ItemsNotSavedException {
        Map<ItemField, String> parametersToUpdate = new TreeMap<>();
        if (!StringUtils.isEmpty(itemParameters.name())) {
            parametersToUpdate.put(ItemField.NAME, itemParameters.name());
        }
        if (!StringUtils.isEmpty(itemParameters.category())) {
            parametersToUpdate.put(ItemField.CATEGORY, itemParameters.category());
        }
        if (!StringUtils.isEmpty(itemParameters.brand())) {
            parametersToUpdate.put(ItemField.BRAND, itemParameters.brand());
        }
        String price = itemParameters.price();
        if (!StringUtils.isEmpty(price)) {
            if (STR_NULL.equals(price)) {
                price = null;
            }
            parametersToUpdate.put(ItemField.PRICE, price);
        }

        return updateItemsInDatabase(itemIds, parametersToUpdate);
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

    /** Method requests the internal storage to update catalog items with specified identifiers.
     * @param ids collection of items identifiers
     * @param parametersToUpdate new parameters values
     * @return coutn of updated catalog items
     * @throws ItemsNotSavedException - if updating failed
     */
    private int updateItemsInDatabase(Set<String> ids, Map<ItemField, String> parametersToUpdate) throws ItemsNotSavedException {
        try {
            return itemRepository.updateItems(ids, parametersToUpdate);
        } catch (DatabaseException ex) {
            throw new ItemsNotSavedException(ex);
        }
    }

    /**
     * Method maps items filters to parameters multimap.
     * @param filters parameters and values to filter with
     * @return parameters multimap
     */
    private Multimap<String, String> getParametersToFilter(FilterDTO filters) {
        Multimap<String, String> multimap = TreeMultimap.create();
        if (CollectionUtils.isNotEmpty(filters.name())) {
            multimap.putAll(ItemField.NAME.name(), filters.name());
        }
        if (CollectionUtils.isNotEmpty(filters.category())) {
            multimap.putAll(ItemField.CATEGORY.name(), filters.category());
        }
        if (CollectionUtils.isNotEmpty(filters.brand())) {
            multimap.putAll(ItemField.BRAND.name(), filters.brand());
        }
        if (CollectionUtils.isNotEmpty(filters.price())) {
            multimap.putAll(ItemField.PRICE.name(), filters.price());
        }
        return multimap;
    }
}
