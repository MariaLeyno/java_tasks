package org.tasks.errors.stock;

import org.tasks.errors.ItemException;

public class StockServiceIsNotInstantiatedException extends ItemException {
    public StockServiceIsNotInstantiatedException(Throwable throwable) {
        super(throwable);
    }
}
