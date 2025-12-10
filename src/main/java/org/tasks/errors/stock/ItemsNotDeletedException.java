package org.tasks.errors.stock;

import org.tasks.errors.ItemException;

public class ItemsNotDeletedException extends ItemException {
    public ItemsNotDeletedException(Throwable throwable) {
        super(throwable.getMessage());
    }
}
