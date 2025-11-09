package org.tasks.service.user;

import org.tasks.UserAccess;
import org.tasks.UserException;
import org.tasks.model.User;
import org.tasks.service.user.errors.*;
import org.tasks.storage.StorageFactory;
import org.tasks.storage.UserStorage;

import java.util.Map;

import static org.tasks.service.user.UserConstants.*;

public class UserService {
    private final UserStorage userStorage;

    public UserService() {
        this.userStorage = StorageFactory.getUserStorage();
    }

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

    private void validateUserName(String name) throws UserNameNotValidException {
        if (name == null || name.length() < 4) {
            throw new UserNameNotValidException();
        }
    }

    private void validatePassword(String password1, String password2) throws PasswordNotValidException {
        if (password1 == null || password1.isEmpty()) {
            throw new PasswordNotValidException();
        }
        if (!password1.equals(password2)) {
            throw new PasswordNotValidException("Passwords do not match");
        }
    }

    private void saveUser(User user) {
        userStorage.addNewDataObject(user);
    }

    private User findUserByLogin(String login) {
        return userStorage.findDataObjectByPrimary(login);
    }
}
