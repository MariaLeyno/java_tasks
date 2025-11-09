package org.tasks.service.stock;

import com.google.common.collect.Multimap;
import org.tasks.model.Item;
import org.tasks.storage.StockStorage;
import org.tasks.storage.StorageFactory;

import java.util.Set;

public class StockService {
    private final StockStorage stockStorage;

    public StockService() {
        this.stockStorage = StorageFactory.getStockStorage();
    }

    public Set<Item> findItems(Multimap<String, String> parameters) {
        return stockStorage.findItemsByParameters(parameters);
    }
}
