package org.tasks.console_ui.stock;

import org.tasks.console_ui.ChooseOptionScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class StockScreen extends ChooseOptionScreen<StockAction> {

    public StockScreen(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected List<StockAction> getActionsList() {
        return Arrays.asList(StockAction.values());
    }
}
