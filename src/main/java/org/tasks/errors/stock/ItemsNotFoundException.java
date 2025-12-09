package org.tasks.errors.stock;

import org.tasks.errors.ItemException;

public class ItemsNotFoundException extends ItemException {
    public ItemsNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
