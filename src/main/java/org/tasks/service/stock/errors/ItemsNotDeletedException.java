package org.tasks.service.stock.errors;

import org.tasks.ItemException;

public class ItemsNotDeletedException extends ItemException {
    public ItemsNotDeletedException(Throwable throwable) {
        super(throwable.getMessage());
    }
}
