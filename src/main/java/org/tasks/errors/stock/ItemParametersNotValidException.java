package org.tasks.errors.stock;

import org.tasks.errors.ItemException;

public class ItemParametersNotValidException extends ItemException {
    public ItemParametersNotValidException() {
        super("Item parameters 'name', 'category' and 'brand' should not be empty");
    }
}
