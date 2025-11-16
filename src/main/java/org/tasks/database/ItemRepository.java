package org.tasks.database;

import com.google.common.collect.Multimap;
import org.tasks.DatabaseException;
import org.tasks.database.utility.DbManagerException;
import org.tasks.model.Item;
import org.tasks.model.ItemField;

import java.util.*;

public class ItemRepository extends Repository<ItemField> {
    private static final String ITEM_TABLE = "catalog_items";
    private static final String ITEM_SEQUENCE = "catalog_items_seq";

    public ItemRepository() throws DatabaseException {
        super(ITEM_TABLE, ITEM_SEQUENCE, ItemField.ID);
    }

    public int deleteItems(Set<String> itemIds) throws DbManagerException {
        return super.deleteDataObjects(itemIds);
    }

    public int updateItems(Set<String> itemIds, Map<ItemField, String> parameters) throws DatabaseException {
        return super.updateDataObjects(itemIds, parameters);
    }

    public int addNewItem(Item item) throws DatabaseException {
        Map<ItemField, String> itemParameters = new TreeMap<>();
        itemParameters.put(ItemField.CATEGORY, item.getCategory());
        itemParameters.put(ItemField.BRAND, item.getBrand());
        itemParameters.put(ItemField.NAME, item.getName());
        Double price = item.getPrice();
        itemParameters.put(ItemField.PRICE, price == null ? null : price.toString());

        return super.addNewDataObject(itemParameters);
    }

    public List<Item> findItemsByParameters(Multimap<String, String> parameters) throws DatabaseException {
        List<Map<ItemField, String>> result = findByParameters(parameters);
        List<Item> items = new ArrayList<>();
        for (Map<ItemField, String> objectMap : result) {
            int id = Integer.parseInt(objectMap.get(ItemField.ID));
            String name = objectMap.get(ItemField.NAME);
            String category = objectMap.get(ItemField.CATEGORY);
            String brand = objectMap.get(ItemField.BRAND);
            String strPrice = objectMap.get(ItemField.PRICE);
            Double price = strPrice == null ? null : Double.valueOf(strPrice);

            items.add(new Item(id, name, category, brand, price));
        }
        return items;
    }

    public Set<String> findItemIdsByParameters(Multimap<String, String> parameters) throws DatabaseException {
        return super.findIdsByParameters(parameters);
    }
}
