package org.tasks.service.stock.errors;

import org.tasks.ItemException;

public class ItemParametersNotValidException extends ItemException {
    public ItemParametersNotValidException() {
        super("Item parameters 'name', 'category' and 'brand' should not be empty");
    }
}
