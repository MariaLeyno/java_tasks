package org.tasks.service.user;

import com.google.common.collect.Multimap;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.tasks.errors.DatabaseException;
import org.tasks.model.UserAccess;
import org.tasks.errors.UserException;
import org.tasks.database.UserRepository;
import org.tasks.errors.db.DbManagerException;
import org.tasks.model.User;
import org.tasks.errors.user.AuthenticationFailedException;
import org.tasks.errors.user.PasswordNotValidException;
import org.tasks.errors.user.UserAlreadyExistsException;
import org.tasks.errors.user.UserNameNotValidException;
import org.tasks.errors.user.UserNotFoundException;
import org.tasks.service.security.CryptService;
import org.tasks.service.security.TokenService;
import org.tasks.web.dto.NewUserDTO;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class UserServiceTest {
    private static final String hashedPassword = "$2a$10$7TaEjk7cpdbtqTPNmg/vpeRGzw3jUr37OpCZbriOhaXDJw3vpWzG6";

    private static UserRepository userRepository;
    private static CryptService cryptService;
    private static TokenService tokenService;

    private static UserService userService;

    @BeforeAll
    public static void init() throws DatabaseException {
        userRepository = mock(UserRepository.class);
        cryptService = mock(CryptService.class);
        tokenService = mock(TokenService.class);

        when(userRepository.findUsersByParameters(any(Multimap.class))).thenAnswer(invocation -> {
            String login = ((Multimap<String, String>) invocation.getArgument(0))
                    .get("LOGIN").iterator().next();
            return switch (login) {
                case "existing_user" -> List.of(new User("existing_user", hashedPassword, UserAccess.CHANGE));
                case "unlucky_user" -> throw new DbManagerException(new RuntimeException());
                default -> Collections.emptyList();
            };
        });
        userService = new UserService(userRepository, cryptService, tokenService);
    }

    @BeforeEach
    public void resetMocks() {
        Mockito.reset(cryptService, tokenService);
    }

    @DisplayName("01. User name should not be empty or too short")
    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "111" })
    public void testRegisterNewUser_invalidLogin(String login) {
        NewUserDTO newUserDTO = new NewUserDTO(login, "password", "password");
        UserNameNotValidException exception
                = assertThrowsExactly(UserNameNotValidException.class, () -> userService.registerNewUser(newUserDTO));
        assertEquals("User name should be at least 4 symbols", exception.getMessage());
    }

    @DisplayName("02. User password should not be empty or too short")
    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "11111" })
    public void testRegisterNewUser_invalidPassword(String password)  {
        NewUserDTO newUserDTO = new NewUserDTO("login", "", password);
        PasswordNotValidException exception
                = assertThrowsExactly(PasswordNotValidException.class, () -> userService.registerNewUser(newUserDTO));

        assertEquals("Password should be at least 6 symbols", exception.getMessage());
    }

    @DisplayName("03. User passwords should match")
    @Test
    public void testRegisterNewUser_PasswordsNotMatch()  {
        NewUserDTO newUserDTO = new NewUserDTO("login", "111111", "222222");
        PasswordNotValidException exception
                = assertThrowsExactly(PasswordNotValidException.class, () -> userService.registerNewUser(newUserDTO));

        assertEquals("Passwords do not match", exception.getMessage());
    }

    @DisplayName("04. User name should not be already used")
    @Test
    public void testRegisterNewUser_alreadyUsedLogin() {
        String login = "existing_user";
        NewUserDTO newUserDTO = new NewUserDTO(login, "password", "password");
        UserAlreadyExistsException exception
                = assertThrowsExactly(UserAlreadyExistsException.class, () -> userService.registerNewUser(newUserDTO));
        assertEquals(String.format("User with name '%s' already exists", login), exception.getMessage());
    }

    @DisplayName("05. User registration fails in case of database error")
    @Test
    public void testRegisterNewUser_databaseException() {
        NewUserDTO newUserDTO = new NewUserDTO("unlucky_user", "password", "password");
        UserNotFoundException exception
                = assertThrowsExactly(UserNotFoundException.class, () -> userService.registerNewUser(newUserDTO));
        assertThat(exception.getMessage()).contains("DbManagerException");
    }

    @DisplayName("06. User with valid login and passwords should be successfully saved")
    @Test
    public void testRegisterNewUser_userSaved() {
        NewUserDTO newUserDTO = new NewUserDTO("new_user", "password", "password");
        assertDoesNotThrow(() -> userService.registerNewUser(newUserDTO));
    }

    @DisplayName("07. User should be already registered at the database")
    @Test
    public void testAuthenticateUser_userNotFound() {
        String login = "user";
        UserNotFoundException exception
                = assertThrowsExactly(UserNotFoundException.class, () -> userService.authenticateUser(login, "password"));
        assertEquals(String.format("User with name '%s' does not exist", login), exception.getMessage());
    }

    @DisplayName("08. Authentication fails if password is wrong")
    @Test
    public void testAuthenticateUser_wrongPassword() {
        AuthenticationFailedException exception
                = assertThrowsExactly(AuthenticationFailedException.class, () -> userService.authenticateUser("existing_user", "password111"));
        assertEquals("Authentication failed", exception.getMessage());
    }

    @DisplayName("09. Authentication fails in case of database error")
    @Test
    public void testAuthenticateUser_databaseException() {
        UserNotFoundException exception
                = assertThrowsExactly(UserNotFoundException.class, () -> userService.authenticateUser("unlucky_user", "password"));
        assertThat(exception.getMessage()).contains("DbManagerException");
    }

    @DisplayName("10. Authentication succeed if login and password are correct")
    @Test
    public void testAuthenticateUser_authenticationSucceed() throws UserException {
        when(cryptService.checkPassword(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateToken(anyString(), any(UserAccess.class))).thenReturn(UUID.randomUUID().toString());

        String token = userService.authenticateUser("existing_user", "cmVzdQ==");
        assertNotNull(token);
    }
}
