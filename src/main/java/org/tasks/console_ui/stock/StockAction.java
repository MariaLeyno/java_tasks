package org.tasks.console_ui.stock;

import lombok.Getter;
import org.tasks.console_ui.Action;

public enum StockAction implements Action {
    SELECT("Select items"),
    ADD("Add new items"),
    UPDATE("Update items"),
    DELETE("Delete items");

    @Getter
    private String message;
    StockAction(String message) {
        this.message = message;
    }
}
