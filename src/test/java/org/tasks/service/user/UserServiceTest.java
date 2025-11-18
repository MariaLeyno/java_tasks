package org.tasks.service.user;

import com.google.common.collect.Multimap;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.tasks.UserAccess;
import org.tasks.UserException;
import org.tasks.database.UserRepository;
import org.tasks.database.utility.DbManagerException;
import org.tasks.model.User;
import org.tasks.service.user.errors.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class UserServiceTest {

    private static UserService userService;

    @BeforeAll
    public static void init() throws UserServiceIsNotInstantiatedException {
        try (MockedConstruction<UserRepository> mockUserRepository
                     = Mockito.mockConstruction(UserRepository.class, ((mock, context) -> {
                         when(mock.findUsersByParameters(any(Multimap.class))).thenAnswer(invocation -> {
                             String login = ((Multimap<String, String>) invocation.getArgument(0))
                                     .get("LOGIN").iterator().next();
                             return switch (login) {
                                 case "existing_user" -> List.of(new User("existing_user", "password", UserAccess.CHANGE));
                                 case "unlucky_user" -> throw new DbManagerException(new RuntimeException());
                                 case null, default -> Collections.emptyList();
                             };
                         });
        }))) {
            userService = new UserService();
        }
    }

    @DisplayName("01. User name should not be empty or too short")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "111" })
    public void testRegisterNewUser_invalidLogin(String login) {
        Map<String, String> userMap = getUserMap(login, "password", "password");
        UserNameNotValidException exception
                = assertThrowsExactly(UserNameNotValidException.class, () -> userService.registerNewUser(userMap));
        assertEquals("User name should be at least 4 symbols", exception.getMessage());
    }

    @DisplayName("02. User password should not be empty or too short")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "11111" })
    public void testRegisterNewUser_invalidPassword(String password)  {
        Map<String, String> userMap = getUserMap("login", password, password);
        PasswordNotValidException exception
                = assertThrowsExactly(PasswordNotValidException.class, () -> userService.registerNewUser(userMap));

        assertEquals("Password should be at least 6 symbols", exception.getMessage());
    }

    @DisplayName("03. User passwords should match")
    @Test
    public void testRegisterNewUser_PasswordsNotMatch()  {
        Map<String, String> userMap = getUserMap("login", "111111", "222222");
        PasswordNotValidException exception
                = assertThrowsExactly(PasswordNotValidException.class, () -> userService.registerNewUser(userMap));

        assertEquals("Passwords do not match", exception.getMessage());
    }

    @DisplayName("04. User name should not be already used")
    @Test
    public void testRegisterNewUser_alreadyUsedLogin() {
        String login = "existing_user";
        Map<String, String> userMap = getUserMap(login, "password", "password");
        UserAlreadyExistsException exception
                = assertThrowsExactly(UserAlreadyExistsException.class, () -> userService.registerNewUser(userMap));
        assertEquals(String.format("User with name '%s' already exists", login), exception.getMessage());
    }

    @DisplayName("05. User registration fails in case of database error")
    @Test
    public void testRegisterNewUser_databaseException() {
        Map<String, String> userMap = getUserMap("unlucky_user", "password", "password");
        UserNotFoundException exception
                = assertThrowsExactly(UserNotFoundException.class, () -> userService.registerNewUser(userMap));
        assertThat(exception.getMessage()).contains("DbManagerException");
    }

    @DisplayName("06. User with valid login and passwords should be successfully saved")
    @Test
    public void testRegisterNewUser_userSaved() {
        Map<String, String> userMap = getUserMap("new_user", "password", "password");
        assertDoesNotThrow(() -> userService.registerNewUser(userMap));
    }

    @DisplayName("07. User should be already registered at the database")
    @Test
    public void testAuthenticateUser_userNotFound() {
        String login = "user";
        Map<String, String> userMap = getUserMap(login, "password", null);
        UserNotFoundException exception
                = assertThrowsExactly(UserNotFoundException.class, () -> userService.authenticateUser(userMap));
        assertEquals(String.format("User with name '%s' does not exist", login), exception.getMessage());
    }

    @DisplayName("08. Authentication fails if password is wrong")
    @Test
    public void testAuthenticateUser_wrongPassword() {
        Map<String, String> userMap = getUserMap("existing_user", "password111", null);
        AuthenticationFailedException exception
                = assertThrowsExactly(AuthenticationFailedException.class, () -> userService.authenticateUser(userMap));
        assertEquals("Authentication failed", exception.getMessage());
    }

    @DisplayName("09. Authentication fails in case of database error")
    @Test
    public void testAuthenticateUser_databaseException() {
        Map<String, String> userMap = getUserMap("unlucky_user", "password", null);
        UserNotFoundException exception
                = assertThrowsExactly(UserNotFoundException.class, () -> userService.authenticateUser(userMap));
        assertThat(exception.getMessage()).contains("DbManagerException");
    }

    @DisplayName("10. Authentication succeed if login and password are correct")
    @Test
    public void testAuthenticateUser_authenticationSucceed() throws UserException {
        Map<String, String> userMap = getUserMap("existing_user", "password", null);
        UserAccess access = userService.authenticateUser(userMap);
        assertEquals(UserAccess.CHANGE, access);
    }

    private Map<String, String> getUserMap(String login, String password, String passwordAgain) {
        Map<String, String> map = new HashMap<>();
        map.put("LOGIN", login);
        map.put("PASSWORD", password);
        if (passwordAgain != null) {
            map.put("PASSWORD_AGAIN", passwordAgain);
        }
        return map;
    }
}
