package org.tasks;

import com.google.common.collect.Multimap;
import org.tasks.console_ui.common.YesNoScreen;
import org.tasks.console_ui.common.YesNoValue;
import org.tasks.console_ui.signin.SignInScreen;
import org.tasks.console_ui.signup.SignUpScreen;
import org.tasks.console_ui.stock.*;
import org.tasks.console_ui.welcome.UserAction;
import org.tasks.console_ui.welcome.WelcomeScreen;
import org.tasks.service.stock.StockService;
import org.tasks.service.stock.errors.ItemsNotDeletedException;
import org.tasks.service.user.UserService;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static org.tasks.console_ui.MarketConstants.*;

public class Market {

    public void work() {
        Scanner scanner = new Scanner(System.in);
        UserAccess userAccess = null;

        while(true) {
            if (userAccess == null) {
                UserAction userAction = showWelcomeScreen(scanner);
                userAccess = showUserScreen(userAction, scanner);
            } else {
                StockAction stockAction = showStockScreen(scanner);
                if (stockAction == StockAction.SELECT) {
                    selectItemsFlow(scanner);
                } else if (stockAction == StockAction.ADD) {
                    addItemFlow(scanner);
                } else if (stockAction == StockAction.UPDATE) {
                    updateItemsFlow(scanner);
                } else if (stockAction == StockAction.DELETE) {
                    deleteItemsFlow(scanner);
                }
            }
        }
    }

    private void selectItemsFlow(Scanner scanner) {
        Multimap<String, String> filterMap = showFilterScreen(scanner, FILTERS_HEADER_FOR_SELECT);
        Set<?> items = selectItems(filterMap);
        System.out.printf(SEARCH_RESULT, items.size());
        items.forEach(System.out::println);
    }

    private void addItemFlow(Scanner scanner) {
        Map<String, String> newItemParameters = showAddScreen(scanner);
        addNewItem(newItemParameters);
        System.out.println(NEW_ITEM_SAVED);
    }

    private void updateItemsFlow(Scanner scanner) {
        Multimap<String, String> filterMap = showFilterScreen(scanner, FILTERS_HEADER_FOR_UPDATE);
        Set<String> itemIds = selectItemIds(filterMap);
        String updateQuestion = String.format(UPDATE_QUESTION, itemIds.size());
        YesNoValue answer = showYesNoScreen(updateQuestion, scanner);
        if (answer == YesNoValue.YES) {
            Map<String, String> parametersToUpdate = showUpdateScreen(scanner);
            int updatedCount = updateItems(itemIds, parametersToUpdate);
            System.out.printf(ITEMS_UPDATED, updatedCount);
        }
    }

    private void deleteItemsFlow(Scanner scanner) {
        Multimap<String, String> filterMap = showFilterScreen(scanner, FILTERS_HEADER_FOR_DELETE);
        Set<String> itemIds = selectItemIds(filterMap);
        String deleteQuestion = String.format(DELETE_QUESTION, itemIds.size());
        YesNoValue answer = showYesNoScreen(deleteQuestion, scanner);
        if (answer == YesNoValue.YES) {
            int deletedCount = deleteItems(itemIds);
            System.out.printf(ITEMS_DELETED, deletedCount);
        }
    }

    private StockAction showStockScreen(Scanner scanner) {
        StockScreen stockScreen = new StockScreen(scanner);
        stockScreen.interact();
        return stockScreen.getChoice();
    }

    private Multimap<String, String> showFilterScreen(Scanner scanner, String additionalHeader) {
        FilterScreen filterScreen = new FilterScreen(scanner, additionalHeader);
        filterScreen.interact();
        return filterScreen.getMultiParameters();
    }
    private Map<String, String> showAddScreen(Scanner scanner) {
        AddScreen addScreen = new AddScreen(scanner);
        addScreen.interact();
        return addScreen.getParameters();
    }

    private Map<String, String> showUpdateScreen(Scanner scanner) {
        UpdateScreen updateScreen = new UpdateScreen(scanner);
        updateScreen.interact();
        return updateScreen.getParameters();
    }

    private UserAction showWelcomeScreen(Scanner scanner) {
        WelcomeScreen welcomeScreen = new WelcomeScreen(scanner);
        welcomeScreen.interact();
        return welcomeScreen.getChoice();
    }

    private UserAccess showUserScreen(UserAction userAction, Scanner scanner) {
        Map<String, String> userParameters = switch (userAction) {
            case SIGN_UP -> {
                SignUpScreen userScreen = new SignUpScreen(scanner);
                userScreen.interact();
                yield userScreen.getParameters();
            }
            case SIGN_IN -> {
                SignInScreen userScreen = new SignInScreen(scanner);
                userScreen.interact();
                yield userScreen.getParameters();
            }
            default -> Map.of();
        };

        return processUserData(userAction, userParameters);
    }

    private YesNoValue showYesNoScreen(String question, Scanner scanner) {
        YesNoScreen yesNoScreen = new YesNoScreen(question, scanner);
        yesNoScreen.interact();
        return yesNoScreen.getAnswer();
    }

    private UserAccess processUserData(UserAction userAction, Map<String, String> userParameters) {
        UserService userService = new UserService();
        try {
            if (userAction == UserAction.SIGN_UP) {
                userService.registerNewUser(userParameters);
            } else if (userAction == UserAction.SIGN_IN) {
                return userService.authenticateUser(userParameters);
            }
        } catch (UserException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private Set<?> selectItems(Multimap<String, String> itemData) {
        StockService stockService = new StockService();
        return stockService.findItems(itemData);
    }

    private Set<String> selectItemIds(Multimap<String, String> itemData) {
        StockService stockService = new StockService();
        return stockService.findItemIds(itemData);
    }

    private void addNewItem(Map<String, String> parameters) {
        StockService stockService = new StockService();
        try {
            stockService.addNewItem(parameters);
        } catch (ItemException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private int updateItems(Set<String> itemIds, Map<String, String> parametersToUpdate) {
        StockService stockService = new StockService();
        try {
            return stockService.updateItems(itemIds, parametersToUpdate);
        } catch (ItemException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }

    private int deleteItems(Set<String> itemIds) {
        StockService stockService = new StockService();
        try {
            return stockService.deleteItems(itemIds);
        } catch (ItemsNotDeletedException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }
}
