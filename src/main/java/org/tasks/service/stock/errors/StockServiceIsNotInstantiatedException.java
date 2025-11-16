package org.tasks.service.stock.errors;

import org.tasks.ItemException;

public class StockServiceIsNotInstantiatedException extends ItemException {
    public StockServiceIsNotInstantiatedException(Throwable throwable) {
        super(throwable);
    }
}
