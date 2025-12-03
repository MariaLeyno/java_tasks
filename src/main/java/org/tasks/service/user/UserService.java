package org.tasks.service.user;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.tasks.DatabaseException;
import org.tasks.UserAccess;
import org.tasks.UserException;
import org.tasks.database.UserRepository;
import org.tasks.model.User;
import org.tasks.model.UserField;
import org.tasks.service.security.CryptService;
import org.tasks.service.security.TokenService;
import org.tasks.service.user.errors.*;

import java.util.List;
import java.util.Map;

/**
 * UserService provides business logic for managing user's accounts. It validates data from UI and requests
 * the internal storage to extract and manipulate accounts parameters and states. Then it returns results to UI.
 * In case of validation or internal errors the service throws {@link UserException}.
 */
public class UserService {

    /** Storage for user accounts */
    private final UserRepository userRepository;
    /** Service to hash and check passwords */
    private final CryptService cryptService;
    /** Service to generate and validate authorization tokens */
    private final TokenService tokenService;

    public UserService() throws UserServiceIsNotInstantiatedException {
        try {
            this.userRepository = new UserRepository();
        } catch (DatabaseException ex) {
            throw new UserServiceIsNotInstantiatedException(ex);
        }
        this.cryptService = new CryptService();
        this.tokenService = new TokenService();
    }

    /**
     * Method validates parameters for a new user account and, if there is no errors, requests the internal storage
     * to store the new account.
     * @param userData user account parameters
     * @throws UserException - if account parameters are invalid or account storing failed
     */
    public void registerNewUser(Map<String, String> userData) throws UserException {
        String login = null;
        String password1 = null;
        String password2 = null;
        for (UserField field : UserField.values()) {
            String value = userData.get(field.name());
            switch (field) {
                case LOGIN -> login = value;
                case PASSWORD -> password1 = value;
                case PASSWORD_AGAIN -> password2 = value;
            }
        }

        validateUserName(login);
        validatePassword(password1, password2);

        if (findUserByLogin(login) != null) {
            throw new UserAlreadyExistsException(login);
        }

        String hashedPassword = cryptService.getHashedPassword(password1);

        User newUser = new User(login, hashedPassword, UserAccess.CHANGE);
        saveUser(newUser);
    }

    /**
     * Method requests the internal storage to find an existing account by its parameters. If the account is found,
     * method validates that the password matches and return the account rights to UI.
     * @param userData user account parameters
     * @return user account rights
     * @throws UserException - if account is not found or password is invalid
     */
    public String authenticateUser(Map<String, String> userData) throws UserException {
        String login = userData.get(UserField.LOGIN.name());
        String password = userData.get(UserField.PASSWORD.name());

        User user = findUserByLogin(login);
        if (user == null) {
            throw new UserNotFoundException(login);
        }

        if (!cryptService.checkPassword(password, user.getPassword())) {
            throw new AuthenticationFailedException();
        }

        return tokenService.generateToken(user.getLogin(), user.getAccess());
    }

    /**
     * Method validates the account name for a new user account.
     * @param name user account name
     * @throws UserNameNotValidException - if name is empty or too short
     */
    private void validateUserName(String name) throws UserNameNotValidException {
        if (name == null || name.length() < 4) {
            throw new UserNameNotValidException();
        }
    }

    /**
     * Method validates the account password for a new account.
     * @param password1 user account password
     * @param password2 user account password entered a second time
     * @throws PasswordNotValidException - if password is empty or twice entered passwords do not match
     */
    private void validatePassword(String password1, String password2) throws PasswordNotValidException {
        if (password1 == null || password1.isEmpty()) {
            throw new PasswordNotValidException();
        }
        if (!password1.equals(password2)) {
            throw new PasswordNotValidException("Passwords do not match");
        }
    }

    /**
     * Method requests the internal storage to store a new account.
     * @param user new account
     * @throws UserNotSavedException - if storing failed
     */
    private void saveUser(User user) throws UserNotSavedException {
        try {
            userRepository.addNewUser(user);
        } catch (DatabaseException ex) {
            throw new UserNotSavedException(ex);
        }
    }

    /**
     * Method requests the internal storage to find an existing account by its name.
     * @param login user account name
     * @return user account
     */
    private User findUserByLogin(String login) throws UserNotFoundException {
        Multimap<String, String> loginFilter = TreeMultimap.create();
        loginFilter.put(UserField.LOGIN.name(), login);
        try {
            List<User> found = userRepository.findUsersByParameters(loginFilter);
            return found == null || found.isEmpty() ? null : found.get(0);
        } catch (DatabaseException ex) {
            throw new UserNotFoundException(ex);
        }
    }
}
