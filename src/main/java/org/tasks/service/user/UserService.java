package org.tasks.service.user;

import org.tasks.UserAccess;
import org.tasks.UserException;
import org.tasks.model.User;
import org.tasks.service.user.errors.*;
import org.tasks.storage.StorageFactory;
import org.tasks.storage.UserStorage;

import java.io.IOException;
import java.util.Map;

import static org.tasks.service.user.UserConstants.*;

/**
 * UserService provides business logic for managing user's accounts. It validates data from UI and requests
 * the internal storage to extract and manipulate accounts parameters and states. Then it returns results to UI.
 * In case of validation or internal errors the service throws {@link UserException}.
 */
public class UserService {

    /** Storage for user accounts */
    private final UserStorage userStorage;

    public UserService() {
        this.userStorage = StorageFactory.getUserStorage();
    }

    /**
     * Method validates parameters for a new user account and, if there is no errors, requests the internal storage
     * to store the new account.
     * @param userData user account parameters
     * @throws UserException - if account parameters are invalid or account storing failed
     */
    public void registerNewUser(Map<String, String> userData) throws UserException {
        String login = userData.get(LOGIN);
        String password1 = userData.get(PASSWORD);
        String password2 = userData.get(PASSWORD_AGAIN);

        validateUserName(login);
        validatePassword(password1, password2);

        if (findUserByLogin(login) != null) {
            throw new UserAlreadyExistsException(login);
        }

        User newUser = new User(login, password1, UserAccess.CHANGE);
        saveUser(newUser);
    }

    /**
     * Method requests the internal storage to find an existing account by its parameters. If the account is found,
     * method validates that the password matches and return the account rights to UI.
     * @param userData user account parameters
     * @return user account rights
     * @throws UserException - if account is not found or password is invalid
     */
    public UserAccess authenticateUser(Map<String, String> userData) throws UserException {
        String login = userData.get(LOGIN);
        String password = userData.get(PASSWORD);

        User user = findUserByLogin(login);
        if (user == null) {
            throw new UserNotFoundException(login);
        }

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationFailedException();
        }

        return user.getAccess();
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
            userStorage.addNewDataObject(user);
        } catch (IOException ex) {
            throw new UserNotSavedException(ex);
        }
    }

    /**
     * Method requests the internal storage to find an existing account by its name.
     * @param login user account name
     * @return user account
     */
    private User findUserByLogin(String login) {
        return userStorage.findDataObjectByPrimary(login);
    }
}
