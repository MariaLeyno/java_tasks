package org.tasks.service.stock.errors;

import org.tasks.ItemException;

public class ItemsNotFoundException extends ItemException {
    public ItemsNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
