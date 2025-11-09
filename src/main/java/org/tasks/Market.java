package org.tasks;

import com.google.common.collect.Multimap;
import org.tasks.console_ui.signin.SignInScreen;
import org.tasks.console_ui.signup.SignUpScreen;
import org.tasks.console_ui.stock.FilterScreen;
import org.tasks.console_ui.stock.StockAction;
import org.tasks.console_ui.stock.StockScreen;
import org.tasks.console_ui.welcome.UserAction;
import org.tasks.console_ui.welcome.WelcomeScreen;
import org.tasks.service.stock.StockService;
import org.tasks.service.user.UserService;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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
                    Multimap<String, String> filterMap = showFilterScreen(scanner);
                    Set<?> items = processItemData(stockAction, filterMap);
                    System.out.printf("%n%nSearch result: %d items%n", items.size());
                    items.forEach(System.out::println);
                } else {
                    System.out.println("Sorry, the section is in progress");
                }
            }
        }
    }

    private StockAction showStockScreen(Scanner scanner) {
        StockScreen stockScreen = new StockScreen(scanner);
        stockScreen.interact();
        return stockScreen.getChoice();
    }

    private Multimap<String, String> showFilterScreen(Scanner scanner) {
        FilterScreen filterScreen = new FilterScreen(scanner);
        filterScreen.interact();
        return filterScreen.getMultiParameters();
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

    private Set<?> processItemData(StockAction stockAction, Multimap<String, String> itemData) {
        StockService stockService = new StockService();
        return stockService.findItems(itemData);
    }
}
