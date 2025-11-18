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
import org.tasks.service.user.UserService;

import java.util.*;

import static org.tasks.console_ui.MarketConstants.*;

/**
 * Market is a base class for Console Marketplace Application. It manages all interaction flows between users
 * and application services and also handles internal errors and reports them to users.
 */
public class Market {

    /**
     * Method displays into the user's console various screens, that corresponds to the user's decisions
     * and application's business logic. An infinite cycle in the method can be terminated by typing 'exit'
     * at any moment, that leads to the termination of the application.
     */
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

    /**
     * Method displays a screen to specify filters for items. Then it sends them to the application
     * service to obtain requested items and outputs them to the console.
     * @param scanner is a stream reader that collects inputs from user console line by line
     */
    private void selectItemsFlow(Scanner scanner) {
        Multimap<String, String> filterMap = showFilterScreen(scanner, FILTERS_HEADER_FOR_SELECT);
        List<?> items = selectItems(filterMap);
        System.out.printf(SEARCH_RESULT, items.size());
        items.forEach(System.out::println);
    }

    /**
     * Method displays a screen to specify parameters values for a new item. Then it sends them
     * to the application service to save the item to the storage.
     * @param scanner is a stream reader that collects inputs from user console line by line
     */
    private void addItemFlow(Scanner scanner) {
        Map<String, String> newItemParameters = showAddScreen(scanner);
        addNewItem(newItemParameters);
        System.out.println(NEW_ITEM_SAVED);
    }

    /**
     * Method displays a screen to specify filters for items to update. Then it displays to the user
     * a number of found items, and if the user wants to update all of them, the method displays him a screen
     * to specify new parameters values. Then the method sends the items identifiers and new parameters values
     * to the application service to update the items into the storage.
     * @param scanner is a stream reader that collects inputs from user console line by line
     */
    private void updateItemsFlow(Scanner scanner) {
        Multimap<String, String> filterMap = showFilterScreen(scanner, FILTERS_HEADER_FOR_UPDATE);
        Set<String> itemIds = selectItemIds(filterMap);
        if (itemIds.isEmpty()) {
            System.out.println("No items were found to update");
        } else {
            String updateQuestion = String.format(UPDATE_QUESTION, itemIds.size());
            YesNoValue answer = showYesNoScreen(updateQuestion, scanner);
            if (answer == YesNoValue.YES) {
                Map<String, String> parametersToUpdate = showUpdateScreen(scanner);
                int updatedCount = updateItems(itemIds, parametersToUpdate);
                System.out.printf(ITEMS_UPDATED, updatedCount);
            }
        }
    }

    /**
     * Method displays a screen to specify filters for items to delete. Then it displays to the user
     * a number of found items, and if the user wants to delete all of them, the method sends the items identifiers
     * to the application service to delete the items from the storage.
     * @param scanner is a stream reader that collects inputs from user console line by line
     */
    private void deleteItemsFlow(Scanner scanner) {
        Multimap<String, String> filterMap = showFilterScreen(scanner, FILTERS_HEADER_FOR_DELETE);
        Set<String> itemIds = selectItemIds(filterMap);
        if (itemIds.isEmpty()) {
            System.out.println("No items were found to delete");
        } else {
            String deleteQuestion = String.format(DELETE_QUESTION, itemIds.size());
            YesNoValue answer = showYesNoScreen(deleteQuestion, scanner);
            if (answer == YesNoValue.YES) {
                int deletedCount = deleteItems(itemIds);
                System.out.printf(ITEMS_DELETED, deletedCount);
            }
        }
    }

    /**
     * Method offers to the user to choose what he wants to do with the catalog items: to browse them,
     * to update or delete existing items or to add a new one.
     * @param scanner is a stream reader that collects inputs from user console line by line
     * @return the user's choice: one of the catalog actions
     */
    private StockAction showStockScreen(Scanner scanner) {
        StockScreen stockScreen = new StockScreen(scanner);
        stockScreen.interact();
        return stockScreen.getChoice();
    }

    /**
     * Method offers to the user to specify fields values to filter the catalog items.
     * @param scanner is a stream reader that collects inputs from user console line by line
     * @param additionalHeader is a title that displays to the user before filters section
     * @return a collection of fields and their required values
     */
    private Multimap<String, String> showFilterScreen(Scanner scanner, String additionalHeader) {
        FilterScreen filterScreen = new FilterScreen(scanner, additionalHeader);
        filterScreen.interact();
        return filterScreen.getMultiParameters();
    }

    /**
     * Method offers to the user to specify fields values for an item to create.
     * @param scanner is a stream reader that collects inputs from user console line by line
     * @return a collection of fields and their values
     */
    private Map<String, String> showAddScreen(Scanner scanner) {
        AddScreen addScreen = new AddScreen(scanner);
        addScreen.interact();
        return addScreen.getParameters();
    }

    /**
     * Method offers to the user to specify fields values to update into the chosen items.
     * @param scanner is a stream reader that collects inputs from user console line by line
     * @return a collection of fields and their values
     */
    private Map<String, String> showUpdateScreen(Scanner scanner) {
        UpdateScreen updateScreen = new UpdateScreen(scanner);
        updateScreen.interact();
        return updateScreen.getParameters();
    }

    /**
     * Method offers to the user to sign in the application or to register a new user account.
     * @param scanner is a stream reader that collects inputs from user console line by line
     * @return the user's choice: signing in or signing up the application
     */
    private UserAction showWelcomeScreen(Scanner scanner) {
        WelcomeScreen welcomeScreen = new WelcomeScreen(scanner);
        welcomeScreen.interact();
        return welcomeScreen.getChoice();
    }

    /**
     * According to the previous user's choice method offers to the user to fill registration or login account data.
     * @param userAction is a previous user's choice: to sing up or sign in the application
     * @param scanner is a stream reader that collects inputs from user console line by line
     * @return user account rights
     */
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

    /**
     * Method offers to the user to answer to a yes-no question.
     * @param question that requires the user's answer
     * @param scanner is a stream reader that collects inputs from user console line by line
     * @return the user's answer: yes or no
     */
    private YesNoValue showYesNoScreen(String question, Scanner scanner) {
        YesNoScreen yesNoScreen = new YesNoScreen(question, scanner);
        yesNoScreen.interact();
        return yesNoScreen.getAnswer();
    }

    /**
     * According to the previous user's choice method sends account data to the application service to create
     * a new account or to sign in the user as an existing account.
     * @param userAction is the previous user's choice to sign up or to sign in the application
     * @param userParameters is an account data
     * @return user account rights
     */
    private UserAccess processUserData(UserAction userAction, Map<String, String> userParameters) {
        try {
            UserService userService = new UserService();
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

    /**
     * Method calls the application service to extract items by the specified parameters filters
     * @param itemData item parameters filters
     * @return a collection of items
     */
    private List<?> selectItems(Multimap<String, String> itemData) {
        try {
            StockService stockService = new StockService();
            return stockService.findItems(itemData);
        } catch (ItemException ex) {
            System.out.println(ex.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Method calls the application service to extract items identifiers with the specified parameters filters.
     * @param itemData item parameters filters
     * @return a collection of items identifiers
     */
    private Set<String> selectItemIds(Multimap<String, String> itemData) {
        try {
            StockService stockService = new StockService();
            return stockService.findItemIds(itemData);
        } catch (ItemException ex) {
            System.out.println(ex.getMessage());
        }
        return Collections.emptySet();
    }

    /**
     * Method calls the application service to create a new item with the specified parameters values.
     * @param parameters item parameters values
     */
    private void addNewItem(Map<String, String> parameters) {
        try {
            StockService stockService = new StockService();
            stockService.addNewItem(parameters);
        } catch (ItemException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Method calls the application service to update the selected items with the specified parameters values
     * @param itemIds a collection of items identifiers
     * @param parametersToUpdate item parameters values
     * @return a number of updated items
     */
    private int updateItems(Set<String> itemIds, Map<String, String> parametersToUpdate) {
        try {
            StockService stockService = new StockService();
            return stockService.updateItems(itemIds, parametersToUpdate);
        } catch (ItemException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }

    /**
     * Method calls the application service to delete the selected items.
     * @param itemIds a collection of items identifiers
     * @return a number of deleted items
     */
    private int deleteItems(Set<String> itemIds) {
        try {
            StockService stockService = new StockService();
            return stockService.deleteItems(itemIds);
        } catch (ItemException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }
}
