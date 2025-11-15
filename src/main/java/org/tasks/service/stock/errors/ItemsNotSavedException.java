package org.tasks.service.stock.errors;

import org.tasks.ItemException;

public class ItemsNotSavedException extends ItemException {
    public ItemsNotSavedException(Throwable throwable) {
        super(throwable.getMessage());
    }
}
