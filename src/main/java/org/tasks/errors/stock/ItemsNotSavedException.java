package org.tasks.errors.stock;

import org.tasks.errors.ItemException;

public class ItemsNotSavedException extends ItemException {
    public ItemsNotSavedException(Throwable throwable) {
        super(throwable.getMessage());
    }
}
