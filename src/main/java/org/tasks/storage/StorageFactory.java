package org.tasks.storage;

import lombok.Getter;

public class StorageFactory {
    @Getter
    private static final UserStorage userStorage = new UserStorage();
    @Getter
    private static final StockStorage stockStorage = new StockStorage();
}
