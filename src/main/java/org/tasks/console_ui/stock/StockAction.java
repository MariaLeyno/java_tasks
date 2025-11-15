package org.tasks.console_ui.stock;

import lombok.Getter;
import org.tasks.UserAccess;
import org.tasks.console_ui.Action;

public enum StockAction implements Action {
    SELECT("Select items", UserAccess.READ),
    ADD("Add new items", UserAccess.CHANGE),
    UPDATE("Update items", UserAccess.CHANGE),
    DELETE("Delete items", UserAccess.CHANGE);

    @Getter
    private String message;
    @Getter
    private UserAccess access;

    StockAction(String message, UserAccess access) {
        this.message = message;
        this.access = access;
    }
}
