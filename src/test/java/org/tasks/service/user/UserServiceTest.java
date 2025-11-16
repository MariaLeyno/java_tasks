package org.tasks.service.user;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.tasks.database.UserRepository;
import org.tasks.service.user.errors.PasswordNotValidException;
import org.tasks.service.user.errors.UserNameNotValidException;
import org.tasks.service.user.errors.UserServiceIsNotInstantiatedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private static UserService userService;

    @BeforeAll
    public static void init() throws UserServiceIsNotInstantiatedException {
        try (MockedConstruction<UserRepository> mockUserRepository
                     = Mockito.mockConstruction(UserRepository.class, ((mock, context) -> {}))) {
            userService = new UserService();
        }
    }

    @DisplayName("User name should not be empty or too short")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "111" })
    public void testRegisterNewUser_invalidLogin(String login) {
        Map<String, String> userMap = getUserMap(login, "password", "password");
        UserNameNotValidException exception
                = assertThrowsExactly(UserNameNotValidException.class, () -> userService.registerNewUser(userMap));
        assertEquals("User name should be at least 4 symbols", exception.getMessage());
    }

    @DisplayName("User password should not be empty or too short")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "11111" })
    public void testRegisterNewUser_invalidPassword(String password1)  {
        Map<String, String> userMap = getUserMap("login", password1, "password2");
        PasswordNotValidException exception
                = assertThrowsExactly(PasswordNotValidException.class, () -> userService.registerNewUser(userMap));

        assertEquals("Password should be at least 6 symbols", exception.getMessage());
    }

    @DisplayName("User passwords should match")
    @ParameterizedTest
    @CsvSource({ "111111, 222222" })
    public void testRegisterNewUser_PasswordsNotMatch(String password1, String password2)  {
        Map<String, String> userMap = getUserMap("login", password1, password2);
        PasswordNotValidException exception
                = assertThrowsExactly(PasswordNotValidException.class, () -> userService.registerNewUser(userMap));

        assertEquals("Passwords do not match", exception.getMessage());
    }

    private Map<String, String> getUserMap(String login, String password, String passwordAgain) {
        Map<String, String> map = new HashMap<>();
        map.put("LOGIN", login);
        map.put("PASSWORD", password);
        map.put("PASSWORD_AGAIN", passwordAgain);
        return map;
    }
}
